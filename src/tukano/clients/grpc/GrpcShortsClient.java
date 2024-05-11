package tukano.clients.grpc;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsGrpc.ShortsBlockingStub;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.CheckBlobIdArgs;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteUserShortsArgs;

public class GrpcShortsClient extends GrpcClient implements Shorts {

    final ShortsBlockingStub stub;

    public GrpcShortsClient(URI serverURI) {
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

            stub = ShortsGrpc.newBlockingStub(channel);
        } catch (Exception x) {
            x.printStackTrace();
            throw new RuntimeException(x);
        }
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        return toJavaResult(() -> {
            stub.deleteUserShorts(DeleteUserShortsArgs.newBuilder()
                    .setUserId(userId)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Void> checkBlobId(String blobId) {
        return toJavaResult(() -> {
            stub.checkBlobId(CheckBlobIdArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }

    // ----------------- Unimplemented methods -----------------

    @Override
    public Result<Short> createShort(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createShort'");
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShort'");
    }

    @Override
    public Result<Short> getShort(String shortId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShort'");
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShorts'");
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'followers'");
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'like'");
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'likes'");
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFeed'");
    }

}
