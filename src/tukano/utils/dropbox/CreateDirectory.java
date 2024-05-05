package tukano.utils.dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tukano.utils.dropbox.msgs.CreateFolderV2Args;

public class CreateDirectory {

	private static final String apiKey = "b994yeq62paqye8";
	private static final String apiSecret = "8w3xrdm9bzh6veo";
	private static final String accessTokenStr = "sl.B0l8qlyGVoSbPRC8r_Lk036EvPesG76ChO_j-S-fJmfeUuiat5ZVE1O4t5H4EfRbQCMxBB4Y7J_7ZrzU2U-0vS-PAK6GaOFWUg0GLjOAS8yN9hYz-_55ADdq4WxnTSxgRZbrGOEaLh6m";

	private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";

	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public CreateDirectory() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}

	public void execute(String directoryName) throws Exception {

		var createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
		createFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		createFolder.setPayload(json.toJson(new CreateFolderV2Args(directoryName, false)));

		service.signRequest(accessToken, createFolder);

		Response r = service.execute(createFolder);
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(String.format("Failed to create directory: %s, Status: %d, \nReason: %s\n",
					directoryName, r.getCode(), r.getBody()));
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("usage: java CreateDirectory <dir>");
			System.exit(0);
		}

		var directory = args[0];
		var cd = new CreateDirectory();

		cd.execute(directory);
		System.out.println("Directory '" + directory + "' created successfuly.");
	}

}
