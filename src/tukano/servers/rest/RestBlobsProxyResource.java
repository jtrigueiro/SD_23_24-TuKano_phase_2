package tukano.servers.rest;

import jakarta.inject.Singleton;

import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.BlobProxyServer;

@Singleton
public class RestBlobsProxyResource extends RestResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsProxyResource(boolean cleanState, String secret, String privateKey) {
        this.impl = new BlobProxyServer(cleanState, secret, privateKey);
    }

    @Override
    public void upload(String blobId, byte[] bytes, String verifier, String timestamp) {
        resultOrThrow(impl.validateOperation(blobId, timestamp, verifier));
        resultOrThrow(impl.upload(blobId, bytes));
    }

    @Override
    public byte[] download(String blobId, String verifier, String timestamp) {
        resultOrThrow(impl.validateOperation(blobId, timestamp, verifier));
        return resultOrThrow(impl.download(blobId));
    }

    @Override
    public void delete(String blobId, String token) {
        resultOrThrow(impl.validateToken(token));
        resultOrThrow(impl.delete(blobId));
    }

}