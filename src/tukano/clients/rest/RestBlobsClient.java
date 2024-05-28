package tukano.clients.rest;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

import jakarta.ws.rs.client.Entity;

import java.net.URI;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

public class RestBlobsClient extends RestClient implements Blobs {

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

    public Result<Void> clt_upload(String blobId, byte[] bytes) {
        return super.toJavaResult(
            target.path(blobId)
                    .request()
                    .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)),
                        Void.class);
    }

    public Result<byte[]> clt_download(String blobId) {
        return super.toJavaResult(
            target.path(blobId)
                    .request()
                    .get(),
            byte[].class);
    }

    @Override
    public Result<Void> delete(String blobId) {
        return super.reTry(() -> clt_delete(blobId));
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return super.reTry(() -> clt_upload(blobId, bytes));
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return super.reTry(() -> clt_download(blobId));
    }
    
}
