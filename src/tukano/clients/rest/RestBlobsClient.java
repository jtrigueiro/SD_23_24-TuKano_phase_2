package tukano.clients.rest;

import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;
import tukano.impl.ExtendedBlobs;
import jakarta.ws.rs.client.Entity;

import java.net.URI;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

public class RestBlobsClient extends RestClient implements ExtendedBlobs {

    private final WebTarget target;

    public RestBlobsClient(URI serverURI) {
        super();
        target = client.target(serverURI).path(RestBlobs.PATH);
    }

    public Result<Void> clt_delete(String blobId) {
        return super.toJavaResult(
                target.path(blobId)
                        .request()
                        .delete(),
                Void.class);
    }

    public Result<Void> clt_upload(String blobId, byte[] bytes, String timestamp, String verifier) {
        return super.toJavaResult(
                target.path(blobId)
                        .queryParam(RestBlobs.TIMESTAMP, timestamp)
                        .queryParam(RestBlobs.VERIFIER, verifier)
                        .request()
                        .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)),
                Void.class);
    }

    public Result<byte[]> clt_download(String blobId, String timestamp, String verifier) {
        return super.toJavaResult(
                target.path(blobId)
                        .queryParam(RestBlobs.TIMESTAMP, timestamp)
                        .queryParam(RestBlobs.VERIFIER, verifier)
                        .request()
                        .get(),
                byte[].class);
    }

    @Override
    public Result<Void> delete(String blobId) {
        return super.reTry(() -> clt_delete(blobId));
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String timestamp, String verifier) {
        return super.reTry(() -> clt_upload(blobId, bytes, timestamp, verifier));
    }

    @Override
    public Result<byte[]> download(String blobId, String timestamp, String verifier) {
        return super.reTry(() -> clt_download(blobId, timestamp, verifier));
    }

    // ------------------- Unimplemented methods -------------------

    @Override
    public Result<Void> validateOperation(String blobId, String timestamp, String verifier) {
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

}
