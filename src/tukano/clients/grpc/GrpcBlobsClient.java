package tukano.clients.grpc;

import java.net.URI;

import com.google.protobuf.ByteString;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;

import tukano.impl.grpc.generated_java.BlobsProtoBuf.ServerDownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.ServerUploadArgs;

import tukano.impl.grpc.generated_java.BlobsGrpc.BlobsBlockingStub;

public class GrpcBlobsClient extends GrpcClient implements Blobs {

    private final BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        super(serverURI);
        stub = BlobsGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> delete(String blobId, String token) {
        return toJavaResult(() -> {
            stub.delete(DeleteArgs.newBuilder()
                    .setBlobId(blobId)
                    .setToken(token)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String token) {
        return toJavaResult(() -> {
            stub.serverUpload(ServerUploadArgs.newBuilder()
                    .setBlobId(blobId)
                    .setBlobIdBytes(ByteString.copyFrom(bytes))
                    .setToken(token)
                    .build());
            return null;
        });
    }

    @Override
    public Result<byte[]> download(String blobId, String token) {
        return toJavaResult(() -> {
            stub.serverDownload(ServerDownloadArgs.newBuilder()
                    .setBlobId(blobId)
                    .setToken(token)
                    .build());
            return null;
        });
    }

    // ----------------- Unimplemented methods -----------------

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
