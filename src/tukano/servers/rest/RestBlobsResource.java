package tukano.servers.rest;

import jakarta.inject.Singleton;
import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.BlobServer;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsResource() {
        this.impl = new BlobServer();
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
    public void delete(String blobId, String token) {
        resultOrThrow(impl.validateOperation(token));
        resultOrThrow(impl.delete(blobId));
    }

    @Override
    public void upload(String blobId, byte[] bytes, String token) {
        resultOrThrow(impl.upload(blobId, bytes));
    }

    @Override
    public byte[] download(String blobId, String token) {
        return resultOrThrow(impl.download(blobId));
    }

}