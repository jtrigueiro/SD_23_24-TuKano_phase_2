package tukano.servers.rest;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;
import tukano.utils.Args;
import tukano.utils.Hibernate;

public class PosOperationShorts {
    
    public static Result<Short> createShort(String shortId, String ownerId, String blobUrl, long timestamp, int likes) {
        Short s = new Short(shortId, ownerId, blobUrl, timestamp, likes);
        Hibernate.getInstance().persist(s);

        String blobs[] = s.getBlobUrl().split("\\|");
        for (int i = 0; i < blobs.length; i++)
            blobs[i] += RestBlobs.PATH + "/" + s.getShortId();

        return Result.ok(constructUrl(blobs, s));
    }

    private static Short constructUrl(String[] blobs, Short s) {
        long timestamp = System.currentTimeMillis();
        String[] urls = blobs;
        String[] verifiers = new String[blobs.length];

        for (int i = 0; i < blobs.length; i++) {
            verifiers[i] = org.apache.commons.codec.digest.DigestUtils.sha256Hex(urls[i] + timestamp + Args.valueOf("-secret", ""));
            urls[i] += "?verifier=" + verifiers[i] + "&timestamp=" + timestamp;
        }

        String finalURL = urls[0];
        if (urls.length > 1)
            finalURL += "|" + urls[1];

        s.setBlobUrl(finalURL);
        return s;
    }
}
