package tukano.servers.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.pac4j.scribe.builder.api.DropboxApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import tukano.utils.dropbox.msgs.DeleteFileV2Args;
import tukano.utils.dropbox.msgs.DownloadFileArgs;
import tukano.utils.dropbox.msgs.UploadFileArgs;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.clients.ClientFactory;

public class BlobServer implements Blobs {
    private static final String apiKey = "b994yeq62paqye8";
    private static final String apiSecret = "8w3xrdm9bzh6veo";
    private static final String accessTokenStr = "sl.B0l8qlyGVoSbPRC8r_Lk036EvPesG76ChO_j-S-fJmfeUuiat5ZVE1O4t5H4EfRbQCMxBB4Y7J_7ZrzU2U-0vS-PAK6GaOFWUg0GLjOAS8yN9hYz-_55ADdq4WxnTSxgRZbrGOEaLh6m";

    private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";

    private static final int HTTP_SUCCESS = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";
    private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg:";
    private static final String DROPBOX_API_ARG_DOWNLOAD_PATH_BUILDER_HDR = "{\"path\": \"/%s\"}";
    private static final String DROPBOX_API_ARG_UPLOAD_PATH_BUILDER_HDR = "{\"autorename\": false,\"mode\": \"add\",\"mute\": false,\"path\": \"/%s\",\"strict_conflict\": false}";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    // private final Path storagePath;

    public BlobServer() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);

        // storagePath = Paths.get("src/tukano/servers/java/blobs");

        // try {
        // Files.createDirectories(storagePath);

        // } catch (IOException e) {
        // throw new RuntimeException(e);

        // }
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Shorts client = ClientFactory.getShortsClient();
        Result<Void> bCheck = client.checkBlobId(blobId);

        // Check if the blobId is verified
        if (!bCheck.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);

        // Path filePath = storagePath.resolve(blobId);

        // if (Files.exists(filePath)) {
        // byte[] fileBytes;

        // try {
        // fileBytes = Files.readAllBytes(filePath);

        // } catch (IOException e) {
        // return Result.error(Result.ErrorCode.CONFLICT);

        // }

        // if (fileBytes.length != bytes.length)
        // Result.error(Result.ErrorCode.CONFLICT);

        // for (int i = 0; i < fileBytes.length; i++) {

        // // Check for bytes mismatch
        // if (fileBytes[i] != bytes[i])
        // return Result.error(Result.ErrorCode.CONFLICT);
        // }

        // } else {
        // try {
        // Files.write(filePath, bytes);

        // } catch (IOException e) {
        // return Result.error(Result.ErrorCode.CONFLICT);

        // }
        // }

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
            uploadFile.addHeader(DROPBOX_API_ARG_HDR, String.format(DROPBOX_API_ARG_UPLOAD_PATH_BUILDER_HDR, blobId));
            uploadFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);

            uploadFile.setPayload(bytes);

            service.signRequest(accessToken, uploadFile);

            try {
                Response r = service.execute(uploadFile);
                if (r.getCode() != HTTP_SUCCESS)
                    return Result.error(Result.ErrorCode.NOT_FOUND);
            } catch (Exception e) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }
        }

        return Result.ok();

    }

    @Override
    public Result<byte[]> download(String blobId) {
        // Path filePath = storagePath.resolve(blobId);

        // if (Files.exists(filePath)) {
        // try {
        // byte[] data = Files.readAllBytes(filePath); // Read bytes from file
        // return Result.ok(data);

        // } catch (IOException e) {
        // return Result.error(Result.ErrorCode.CONFLICT);

        // }
        // } else
        // return Result.error(Result.ErrorCode.NOT_FOUND);

        var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
        downloadFile.addHeader(DROPBOX_API_ARG_HDR, String.format(DROPBOX_API_ARG_DOWNLOAD_PATH_BUILDER_HDR, blobId));

        // downloadFile.setPayload(json.toJson(new DownloadFileArgs(blobId)));

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
        // Path filePath = storagePath.resolve(blobId);

        // if (filePath.toFile().exists()) {
        // try {
        // Files.delete(filePath);

        // } catch (IOException e) {
        // return Result.error(Result.ErrorCode.CONFLICT);

        // }
        // } else
        // return Result.error(Result.ErrorCode.NOT_FOUND);

        var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFile.setPayload(json.toJson(new DeleteFileV2Args(blobId)));

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

}
