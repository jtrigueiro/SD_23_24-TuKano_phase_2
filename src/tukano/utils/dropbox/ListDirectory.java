package tukano.utils.dropbox;

import java.util.ArrayList;
import java.util.List;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tukano.utils.dropbox.msgs.ListFolderArgs;
import tukano.utils.dropbox.msgs.ListFolderContinueArgs;
import tukano.utils.dropbox.msgs.ListFolderReturn;

public class ListDirectory {

	private static final String apiKey = "b994yeq62paqye8";
	private static final String apiSecret = "8w3xrdm9bzh6veo";
	private static final String accessTokenStr = "sl.B0l8qlyGVoSbPRC8r_Lk036EvPesG76ChO_j-S-fJmfeUuiat5ZVE1O4t5H4EfRbQCMxBB4Y7J_7ZrzU2U-0vS-PAK6GaOFWUg0GLjOAS8yN9hYz-_55ADdq4WxnTSxgRZbrGOEaLh6m";

	private static final String LIST_FOLDER_URL = "https://api.dropboxapi.com/2/files/list_folder";
	private static final String LIST_FOLDER_CONTINUE_URL = "https://api.dropboxapi.com/2/files/list_folder/continue";

	private static final int HTTP_SUCCESS = 200;

	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public ListDirectory() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}

	public List<String> execute(String directoryName) throws Exception {
		var directoryContents = new ArrayList<String>();

		var listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_URL);
		listDirectory.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		listDirectory.setPayload(json.toJson(new ListFolderArgs(directoryName)));

		service.signRequest(accessToken, listDirectory);

		Response r = service.execute(listDirectory);
		;
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(String.format("Failed to list directory: %s, Status: %d, \nReason: %s\n",
					directoryName, r.getCode(), r.getBody()));

		var reply = json.fromJson(r.getBody(), ListFolderReturn.class);
		reply.getEntries().forEach(e -> directoryContents.add(e.toString()));

		while (reply.has_more()) {
			listDirectory = new OAuthRequest(Verb.POST, LIST_FOLDER_CONTINUE_URL);
			listDirectory.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

			// In this case the arguments is just an object containing the cursor that was
			// returned in the previous reply.
			listDirectory.setPayload(json.toJson(new ListFolderContinueArgs(reply.getCursor())));
			service.signRequest(accessToken, listDirectory);

			r = service.execute(listDirectory);

			if (r.getCode() != HTTP_SUCCESS)
				throw new RuntimeException(String.format("Failed to list directory: %s, Status: %d, \nReason: %s\n",
						directoryName, r.getCode(), r.getBody()));

			reply = json.fromJson(r.getBody(), ListFolderReturn.class);
			reply.getEntries().forEach(e -> directoryContents.add(e.toString()));
		}

		return directoryContents;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("usage: java ListDirectory <dir>");
			System.exit(0);
		}

		var directory = args[0];
		var ld = new ListDirectory();

		System.out.println("Directory " + directory + ":");
		for (String entry : ld.execute(directory))
			System.out.println(entry);
	}

}
