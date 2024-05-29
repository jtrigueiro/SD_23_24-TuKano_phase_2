package tukano.servers.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;
import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.BlobServer;

@Singleton
@Provider
public class RestBlobsResource extends RestResource implements RestBlobs {

    final Blobs impl;
    final String privateKey;

    public RestBlobsResource(String privateKey) {
        this.privateKey = privateKey;
        this.impl = new BlobServer(privateKey);
    }

    @Override
    public void upload(String blobId, byte[] bytes, String timestamp, String verifier) {
        resultOrThrow(impl.validateOperation(blobId, timestamp, verifier));
        resultOrThrow(impl.upload(blobId, bytes));
    }

    @Override
    public byte[] download(String blobId, String timestamp, String verifier) {
        resultOrThrow(impl.validateOperation(blobId, timestamp, verifier));
        return resultOrThrow(impl.download(blobId));
    }

    @Override
    public void delete(String blobId) {
        resultOrThrow(impl.delete(blobId));
    }

}