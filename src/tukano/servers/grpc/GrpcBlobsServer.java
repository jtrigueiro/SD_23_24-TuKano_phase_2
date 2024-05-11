package tukano.servers.grpc;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContextBuilder;

import tukano.utils.Discovery;

public class GrpcBlobsServer {
    public static final int PORT = 8080;
    public static final String SERVICE = "blobs";
    private static final String SERVER_URI_FMT = "grpc://%s:%s%s";
    private static final String GRPC_CTX = "/gprc";

    public static void main(String[] args) throws Exception {

        var keyStore = System.getProperty("javax.net.ssl.keyStore");
        var keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

        var keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (var in = new FileInputStream(keyStore)) {
            keystore.load(in, keyStorePassword.toCharArray());
        }

        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, keyStorePassword.toCharArray());

        GrpcBlobsServerStub stub;
        if (args[0].toLowerCase().equals("true"))
            stub = new GrpcBlobsServerStub(true);
        else
            stub = new GrpcBlobsServerStub(false);

        var sslContext = GrpcSslContexts.configure(SslContextBuilder.forServer(keyManagerFactory)).build();
        var server = NettyServerBuilder.forPort(PORT).addService(stub).sslContext(sslContext).build();
        var serverURI = String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

        Discovery discovery = Discovery.getInstance();
        discovery.announce(SERVICE, serverURI);
        server.start();
        server.awaitTermination();
    }
}
