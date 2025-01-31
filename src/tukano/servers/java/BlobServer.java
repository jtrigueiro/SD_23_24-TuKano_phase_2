package tukano.servers.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import tukano.utils.Token;
import tukano.utils.Args;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

public class BlobServer implements Blobs {

    private final Path storagePath;
    private final String privateKey, serverURI;

    public BlobServer() {
        Token.set(Args.valueOf("-token", ""));
        this.privateKey = Args.valueOf("-secret", "");
        this.serverURI = Args.valueOf("-serverURI", "");

        storagePath = Paths.get("").toAbsolutePath();

        try {
            Files.createDirectories(storagePath);

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Path filePath = storagePath.resolve(blobId);

        if (Files.exists(filePath)) {
            byte[] fileBytes;

            try {
                fileBytes = Files.readAllBytes(filePath);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

            }

            if (fileBytes.length != bytes.length)
                Result.error(Result.ErrorCode.CONFLICT);

            for (int i = 0; i < fileBytes.length; i++) {

                // Check for bytes mismatch
                if (fileBytes[i] != bytes[i])
                    return Result.error(Result.ErrorCode.CONFLICT);
            }

        } else {
            try {
                Files.write(filePath, bytes);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

            }
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Path filePath = storagePath.resolve(blobId);

        if (Files.exists(filePath)) {
            try {
                byte[] data = Files.readAllBytes(filePath);
                return Result.ok(data);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

            }
        } else
            return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<Void> delete(String blobId) {
        Path filePath = storagePath.resolve(blobId);

        if (filePath.toFile().exists()) {
            try {
                Files.delete(filePath);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

            }
        } else
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    public Result<Void> validateOperation(String blobId, String timestamp, String verifier) {
        long time = Long.parseLong(timestamp);
        if (Math.abs(System.currentTimeMillis() - time) > 10000)
            return Result.error(Result.ErrorCode.FORBIDDEN);

        String toHash = org.apache.commons.codec.digest.DigestUtils
                .sha256Hex(serverURI + RestBlobs.PATH + "/" + blobId + timestamp + privateKey);

        if (!verifier.equals(toHash)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> validateOperation(String token) {
        if (Token.matches(token))
            return Result.ok();
        else
            return Result.error(Result.ErrorCode.FORBIDDEN);
    }

    // ------------------- Unimplemented Methods -------------------

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upload'");
    }

    @Override
    public Result<byte[]> download(String blobId, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'download'");
    }

    @Override
    public Result<Void> delete(String blobId, String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}