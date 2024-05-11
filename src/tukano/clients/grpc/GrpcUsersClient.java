package tukano.clients.grpc;

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManagerFactory;

import tukano.api.java.Result;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.*;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;

import static tukano.impl.grpc.common.DataModelAdaptor.User_to_GrpcUser;
import static tukano.impl.grpc.common.DataModelAdaptor.GrpcUser_to_User;

public class GrpcUsersClient extends GrpcClient implements Users {

    final UsersGrpc.UsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
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

            stub = UsersGrpc.newBlockingStub(channel);
        } catch (Exception x) {
            x.printStackTrace();
            throw new RuntimeException(x);
        }
    }

    @Override
    public Result<String> createUser(User user) {
        return toJavaResult(() -> {
            var res = stub.createUser(CreateUserArgs.newBuilder()
                    .setUser(User_to_GrpcUser(user))
                    .build());
            return res.getUserId();
        });
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.getUser(GetUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        return toJavaResult(() -> {
            var res = stub.updateUser(UpdateUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .setUser(User_to_GrpcUser(user))
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.deleteUser(DeleteUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return toJavaResult(() -> {
            var res = stub.searchUsers(SearchUserArgs.newBuilder()
                    .setPattern(pattern)
                    .build());

            List<User> users = new ArrayList<>();
            while (res.hasNext()) {
                users.add(GrpcUser_to_User(res.next()));
            }
            return users;
        });
    }

}