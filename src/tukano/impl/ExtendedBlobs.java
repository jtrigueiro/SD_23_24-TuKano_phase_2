package tukano.impl;

import tukano.api.java.Blobs;
import tukano.api.java.Result;

public interface ExtendedBlobs extends Blobs {

    Result<Void> upload(String blobId, byte[] bytes, String timestamp, String verifier);

    Result<byte[]> download(String blobId, String timestamp, String verifier);

}
