package tukano.servers.grpc;

import com.google.protobuf.ByteString;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.servers.java.BlobServer;

import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteResult;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DownloadResult;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadArgs;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.UploadResult;

public class GrpcBlobsServerStub implements BlobsGrpc.AsyncService, BindableService {
    final Blobs impl;

    public GrpcBlobsServerStub() {
        this.impl = new BlobServer();
    }

    @Override
    public ServerServiceDefinition bindService() {
        return BlobsGrpc.bindService(this);
    }

    @Override
    public void upload(UploadArgs request, StreamObserver<UploadResult> responseObserver) {
        if (!validateOperation(request.getBlobId()))
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.FORBIDDEN));

        var res = impl.upload(request.getBlobId().split("\\?")[0], request.getData().toByteArray());
        if (!res.isOK())
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(UploadResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void download(DownloadArgs request, StreamObserver<DownloadResult> responseObserver) {
        if (!validateOperation(request.getBlobId()))
            responseObserver.onError(errorCodeToStatus(Result.ErrorCode.FORBIDDEN));

        var res = impl.download(request.getBlobId().split("\\?")[0]);
        if (!res.isOK())
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(DownloadResult.newBuilder().setChunk(ByteString.copyFrom(res.value())).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void delete(DeleteArgs request, StreamObserver<DeleteResult> responseObserver) {
        var res = impl.delete(request.getBlobId());
        if (!res.isOK())
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext(DeleteResult.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    private boolean validateOperation(String blobUrl) {
        try { // FORMAT: grpc://hostname:8080/grpc/blobs/blobID?verifier=xxxxx&timestamp=xxxxx
              // URI uri = new URI(blobUrl);
              // String[] params = uri.getQuery().split("&");

            String[] parts = blobUrl.split("\\?");
            String[] params = parts[1].split("&");
            String blobId = parts[0];
            String verifier = params[0].split("=")[1];
            String timestamp = params[1].split("=")[1];

            var res = impl.validateOperation(blobId, timestamp, verifier);
            return res.isOK();

        } catch (Exception e) {
            return false;
        }
    }

    protected static Throwable errorCodeToStatus(Result.ErrorCode error) {
        var status = switch (error) {
            case NOT_FOUND -> io.grpc.Status.NOT_FOUND;
            case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
            case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
            case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
            default -> io.grpc.Status.INTERNAL;
        };

        return status.asException();
    }
}
