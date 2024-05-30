package tukano.clients.rest;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

import java.net.URI;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

public class RestBlobsClient extends RestClient implements Blobs {

    public RestBlobsClient(URI serverURI) {
        super(serverURI, RestBlobs.PATH);
    }

    public Result<Void> svr_delete(String blobId, String token) {
        return super.toJavaResult(
                target.path(blobId + RestBlobs.SERVER)
                        .queryParam(RestBlobs.TOKEN, token)
                        .request()
                        .delete(),
                Void.class);
    }

    public Result<Void> svr_upload(String blobId, byte[] bytes, String token) {
        return super.toJavaResult(
                target.path(blobId + RestBlobs.SERVER)
                        .queryParam(RestBlobs.TOKEN, token)
                        .request()
                        .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)),
                Void.class);
    }

    public Result<byte[]> svr_download(String blobId, String token) {
        return super.toJavaResult(
                target.path(blobId + RestBlobs.SERVER)
                        .queryParam(RestBlobs.TOKEN, token)
                        .request()
                        .get(),
                byte[].class);
    }

    @Override
    public Result<Void> delete(String blobId, String token) {
        return super.reTry(() -> svr_delete(blobId, token));
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String token) {
        return super.reTry(() -> svr_upload(blobId, bytes, token));
    }

    @Override
    public Result<byte[]> download(String blobId, String token) {
        return super.reTry(() -> svr_download(blobId, token));
    }

    // ------------------- Unimplemented methods -------------------

    @Override
    public Result<Void> validateOperation(String blobId, String timestamp, String verifier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateOperation'");
    }

    @Override
    public Result<Void> validateOperation(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateOperation'");
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upload'");
    }

    @Override
    public Result<byte[]> download(String blobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'download'");
    }

    @Override
    public Result<Void> delete(String blobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
