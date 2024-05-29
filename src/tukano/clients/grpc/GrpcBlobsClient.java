package tukano.clients.grpc;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.TrustManagerFactory;

import com.google.protobuf.ByteString;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsGrpc.BlobsBlockingStub;

public class GrpcBlobsClient extends GrpcClient implements Blobs {

    private final BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
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

            var channel = NettyChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort())
                    .sslContext(sslContext).build();

            stub = BlobsGrpc.newBlockingStub(channel);
        } catch (Exception x) {
            x.printStackTrace();
            throw new RuntimeException(x);
        }
    }

    @Override
    public Result<Void> delete(String blobId) {
        return toJavaResult(() -> {
            stub.delete(DeleteArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return toJavaResult(() -> {
            stub.upload(UploadArgs.newBuilder()
                    .setBlobId(blobId)
                    .setBlobIdBytes(ByteString.copyFrom(bytes))
                    .build());
            return null;
        });
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return toJavaResult(() -> {
            stub.download(DownloadArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }

    // ----------------- Unimplemented methods -----------------

    @Override
    public Result<Void> validateOperation(String blobId, String timestamp, String verifier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateOperation'");
    }

}
