package tukano.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tukano.utils.Args;
import tukano.utils.Discovery;


public class RestShortsRepServer {
	public static final int PORT = 8080;
	public static final String SERVICE = "shorts";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";
	private static Logger Log = Logger.getLogger(RestShortsRepServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	public static void main(String[] args) {
        Args.use(args);

		try {

			ResourceConfig config = new ResourceConfig();
			config.register(new RestShortsRepResource().getClass());

			String ip = InetAddress.getLocalHost().getHostName();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);

			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Discovery discovery = Discovery.getInstance();
			discovery.announce(SERVICE, serverURI.toString());
			Log.info(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));


		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}