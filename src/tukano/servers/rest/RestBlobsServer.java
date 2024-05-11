package tukano.servers.rest;

import java.net.URI;
import java.net.InetAddress;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import javax.net.ssl.SSLContext;

import tukano.servers.grpc.GrpcBlobsServerStub;
import tukano.utils.Discovery;

public class RestBlobsServer {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "blobs";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";

	public static void main(String[] args) {
		try {

			ResourceConfig config = new ResourceConfig();
			// config.register(RestBlobsResource.class);
			if (args[0].toLowerCase().equals("true"))
				config.register(new RestBlobsResource(true));
			else
				config.register(new RestBlobsResource(false));

			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Discovery discovery = Discovery.getInstance();
			discovery.announce(SERVICE, serverURI.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
