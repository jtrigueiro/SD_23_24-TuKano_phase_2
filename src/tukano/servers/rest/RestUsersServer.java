package tukano.servers.rest;

import java.net.URI;
import java.net.InetAddress;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import javax.net.ssl.SSLContext;
import tukano.utils.Discovery;

public class RestUsersServer {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";

	public static void main(String[] args) {

		String token = args[0];

		try {

			ResourceConfig config = new ResourceConfig();
			config.register(new RestUsersResource(token));

			String ip = InetAddress.getLocalHost().getHostName();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Discovery discovery = Discovery.getInstance();
			discovery.announce(SERVICE, serverURI.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
