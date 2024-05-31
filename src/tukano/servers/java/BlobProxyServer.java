package tukano.servers.java;

import org.pac4j.scribe.builder.api.DropboxApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tukano.utils.dropbox.msgs.CreateFolderV2Args;
import tukano.utils.dropbox.msgs.DeleteFileV2Args;
import tukano.utils.dropbox.msgs.DownloadFileArgs;
import tukano.utils.dropbox.msgs.UploadFileArgs;

import tukano.utils.Token;
import tukano.utils.Args;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestBlobs;
import tukano.clients.ClientFactory;

public class BlobProxyServer implements Blobs {
    private static final String apiKey = "b994yeq62paqye8";
    private static final String apiSecret = "8w3xrdm9bzh6veo";
    private static final String accessTokenStr = "sl.B2OG8jNM69PJUKETf6kjabsqAxAO8Hv1A-eXWPziPDUK3b8_rpa6eEr5lLby4e_N1w1R5VYeeXr2-PM5-E38uj2XNDzilcTw5kRZ-770HF0hnv26m_EfJ7R2X83EKyKK06JAxUFzIqLc";

    private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
    private static final String CREATE_FOLDER_V2_URL = "https://api.dropboxapi.com/2/files/create_folder_v2";

    private static final int HTTP_SUCCESS = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";
    private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg";

    private static final String ROOT_FOLDER = "/blobs";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
    private final String privateKey, serverURI;

    public BlobProxyServer() {
        Token.set(Args.valueOf("-token", ""));
        this.privateKey = Args.valueOf("-secret", "");
        this.serverURI = Args.valueOf("-serverURI", "");
        Boolean cleanState = Boolean.parseBoolean(Args.valueOf("-cleanState", ""));

        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
        if (cleanState)
            deleteFolder(ROOT_FOLDER);
        createFolder(ROOT_FOLDER);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Shorts client = ClientFactory.getShortsClient();
        Result<Void> bCheck = client.checkBlobId(blobId);

        // Check if the blobId is verified
        if (!bCheck.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);

        Result<byte[]> check = download(blobId);

        if (check.isOK()) {
            byte[] fileBytes = check.value();
            if (fileBytes.length != bytes.length)
                Result.error(Result.ErrorCode.CONFLICT);

            for (int i = 0; i < fileBytes.length; i++) {

                // Check for bytes mismatch
                if (fileBytes[i] != bytes[i])
                    return Result.error(Result.ErrorCode.CONFLICT);
            }
        } else {
            var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_URL);
            uploadFile.addHeader(DROPBOX_API_ARG_HDR,
                    json.toJson(new UploadFileArgs(false, "add", false, ROOT_FOLDER + "/" + blobId, false)));
            uploadFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);

            uploadFile.setPayload(bytes);

            service.signRequest(accessToken, uploadFile);

            try {
                Response r = service.execute(uploadFile);
                if (r.getCode() != HTTP_SUCCESS)
                    return Result.error(Result.ErrorCode.CONFLICT);
            } catch (Exception e) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }
        }

        return Result.ok();

    }

    @Override
    public Result<byte[]> download(String blobId) {

        var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
        downloadFile.addHeader(DROPBOX_API_ARG_HDR, json.toJson(new DownloadFileArgs(ROOT_FOLDER + "/" + blobId)));
        downloadFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);

        service.signRequest(accessToken, downloadFile);

        try {
            Response r = service.execute(downloadFile);
            if (r.getCode() == HTTP_SUCCESS)
                return Result.ok(r.getBody().getBytes());
            else
                return Result.error(Result.ErrorCode.NOT_FOUND);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.CONFLICT);
        }
    }

    @Override
    public Result<Void> delete(String blobId) {

        var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFile.setPayload(json.toJson(new DeleteFileV2Args(ROOT_FOLDER + "/" + blobId)));

        service.signRequest(accessToken, deleteFile);

        try {
            Response r = service.execute(deleteFile);
            if (r.getCode() != HTTP_SUCCESS)
                return Result.error(Result.ErrorCode.NOT_FOUND);
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok();
    }

    public void createFolder(String folderDir) {
        var createFolder = new OAuthRequest(Verb.POST, CREATE_FOLDER_V2_URL);
        createFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        createFolder.setPayload(json.toJson(new CreateFolderV2Args(folderDir, false)));

        service.signRequest(accessToken, createFolder);

        try {
            service.execute(createFolder);
        } catch (Exception e) {
        }
    }

    public void deleteFolder(String folderDir) {
        var deleteFolder = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
        deleteFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFolder.setPayload(json.toJson(new DeleteFileV2Args(folderDir)));

        service.signRequest(accessToken, deleteFolder);

        try {
            service.execute(deleteFolder);
        } catch (Exception e) {
        }
    }

    @Override
    public Result<Void> validateOperation(String blobId, String timestamp, String verifier) {
        String toHash = org.apache.commons.codec.digest.DigestUtils
                .sha256Hex(serverURI + RestBlobs.PATH + "/" + blobId + timestamp + privateKey);

        if (!verifier.equals(toHash)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> validateOperation(String token) {
        if (Token.matches(token))
            return Result.ok();
        else
            return Result.error(Result.ErrorCode.FORBIDDEN);
    }

    // ------------------- Unimplemented methods -------------------

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upload'");
    }

    @Override
    public Result<byte[]> download(String blobId, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'download'");
    }

    @Override
    public Result<Void> delete(String blobId, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
