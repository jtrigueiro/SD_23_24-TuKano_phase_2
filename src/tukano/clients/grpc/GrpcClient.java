package tukano.clients.grpc;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.function.Supplier;

import javax.net.ssl.TrustManagerFactory;

import jakarta.ws.rs.ProcessingException;

import tukano.utils.Sleep;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;

import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class GrpcClient {
    
    protected static final int MAX_RETRIES = 3;
	protected static final int RETRY_SLEEP = 1000;

    protected ManagedChannel channel;

	public GrpcClient(URI serverURI) {
        try {
            var trustStore = System.getProperty("javax.net.ssl.trustStore");
            var trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");

            var keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (var in = new FileInputStream(trustStore)) {
                keystore.load(in, trustStorePassword.toCharArray());
            }

            var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            var sslContext = GrpcSslContexts.configure(SslContextBuilder.forClient().trustManager(trustManagerFactory))
                    .build();

            channel = NettyChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort())
                    .sslContext(sslContext).build();

        } catch (Exception x) {
            x.printStackTrace();
            throw new RuntimeException(x);
        }
    }

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (ProcessingException x) {
				Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				x.printStackTrace();
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}
		return Result.error(Result.ErrorCode.TIMEOUT);
	}

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return ok(func.get());
        } catch (StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if (code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED)
                throw sre;
            return error(statusToErrorCode(sre.getStatus()));
        }
    }


    static ErrorCode statusToErrorCode(Status status) {
        return switch (status.getCode()) {
            case OK -> ErrorCode.OK;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

}
