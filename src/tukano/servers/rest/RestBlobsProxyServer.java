package tukano.servers.rest;

import java.net.URI;
import java.net.InetAddress;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import javax.net.ssl.SSLContext;

import tukano.utils.Discovery;

public class RestBlobsProxyServer {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "blobs";
    private static final String SERVER_URI_FMT = "https://%s:%s/rest";

    public static void main(String[] args) {
        try {

            Boolean toClean = Boolean.parseBoolean(args[0]);
            // int index = args[1]
            String privateKey = args[2];

            ResourceConfig config = new ResourceConfig();

            String host = InetAddress.getLocalHost().getHostName();
            String serverURI = String.format(SERVER_URI_FMT, host, PORT);

            config.register(new RestBlobsProxyResource(toClean, privateKey, serverURI));
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

            Discovery discovery = Discovery.getInstance();
            discovery.announce(SERVICE, serverURI.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
