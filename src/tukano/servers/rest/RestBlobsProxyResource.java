package tukano.servers.rest;

import jakarta.inject.Singleton;

import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.BlobProxyServer;

@Singleton
public class RestBlobsProxyResource extends RestResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsProxyResource(boolean cleanState) {
        this.impl = new BlobProxyServer(cleanState);
    }

    @Override
    public void upload(String blobId, byte[] bytes) {
        resultOrThrow(impl.upload(blobId, bytes));
    }

    @Override
    public byte[] download(String blobId) {
        return resultOrThrow(impl.download(blobId));
    }

    @Override
    public void delete(String blobId) {
        resultOrThrow(impl.delete(blobId));
    }

}