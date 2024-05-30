package tukano.clients.grpc;

import java.net.URI;

import com.google.protobuf.ByteString;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsGrpc.BlobsBlockingStub;

public class GrpcBlobsClient extends GrpcClient implements Blobs {

    private final BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        super(serverURI);
        stub = BlobsGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> delete(String blobId) {
        return toJavaResult(() -> {
            stub.delete(DeleteArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return toJavaResult(() -> {
            stub.upload(UploadArgs.newBuilder()
                    .setBlobId(blobId)
                    .setBlobIdBytes(ByteString.copyFrom(bytes))
                    .build());
            return null;
        });
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return toJavaResult(() -> {
            stub.download(DownloadArgs.newBuilder()
                    .setBlobId(blobId)
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

}
