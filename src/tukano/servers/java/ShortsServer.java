package tukano.servers.java;

import java.net.URI;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.Comparator;

import tukano.api.User;
import tukano.api.Likes;
import tukano.api.Short;
import tukano.utils.Args;
import tukano.api.Follows;
import tukano.utils.Token;
import tukano.api.java.Users;
import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.utils.Discovery;
import tukano.utils.Hibernate;
import tukano.clients.ClientFactory;

public class ShortsServer implements Shorts {

    // Queries
    private static String shortByShortId = "SELECT s FROM Short s WHERE s.shortId = '%s'";
    private static String shortIdByOwnerId = "SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'";
    private static String shortsByOwnerId = "SELECT s FROM Short s WHERE s.ownerId = '%s'";
    private static String followsByUserIds = "SELECT f FROM Follows f WHERE f.userId1 = '%s' AND f.userId2 = '%s'";
    private static String followersByUserId = "SELECT f.userId1 FROM Follows f WHERE f.userId2 = '%s'";
    private static String followingIdByUserId = "SELECT f.userId2 FROM Follows f WHERE f.userId1 = '%s'";
    private static String likeByUserIdAndShortId = "SELECT l FROM Likes l WHERE l.userId = '%s' AND l.shortId = '%s'";
    private static String likesIdByShortId = "SELECT l.userId FROM Likes l WHERE l.shortId = '%s'";
    private static String likesByShortId = "SELECT l FROM Likes l WHERE l.shortId = '%s'";
    private static String likesByUserId = "SELECT l FROM Likes l WHERE l.userId = '%s'";
    private static String followsByUserId = "SELECT f FROM Follows f WHERE f.userId2 = '%s' OR f.userId1 = '%s'";

    private Map<URI, Integer> blobLoad = new ConcurrentHashMap<>();
    private static final int REPLICATION_CHECK_INTERVAL = 5000;

    private final String privateKey;

    public ShortsServer() {
        Token.set(Args.valueOf("-token", ""));
        this.privateKey = Args.valueOf("-secret", "");

        replicationCheck();
    }

    private void replicationCheck() {
        // Inicializar o mapa de carga de blobs
        for (URI uri : Discovery.getInstance().knownUrisOf("blobs", 1))
            blobLoad.put(uri, 0);

        // Iniciar thread
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(REPLICATION_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Set<URI> blobs = Set.of(Discovery.getInstance().knownUrisOf("blobs", 1));
                if (blobLoad.size() > blobs.size()) {
                    for (URI uri : blobLoad.keySet()) {
                        if (!blobs.contains(uri)) {
                            blobLoad.remove(uri);
                            replicate(uri);
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    private void replicate(URI blobURI) { // FORMAT = HTTPS://HOSTNAME:PORT/REST
        List<Short> shorts = Hibernate.getInstance().jpql("SELECT s FROM Short s", Short.class);
        Map<URI, Blobs> clients = new ConcurrentHashMap<>();

        // Inicializar os clientes
        for (URI uri : blobLoad.keySet())
            clients.put(uri, ClientFactory.getBlobsClient(uri));

        for (Short s : shorts) {

            if (s.getBlobUrl().contains(blobURI.toString())) {

                for (String url : getShortenBlobURL(s)) { // [BLOBAPAGADO, BLOBDOWNLOAD] OU [BLOBORIGEM, BLOBAPAGADO]
                    if (!blobURI.toString().equals(url)) {

                        Result<byte[]> bytes = clients.get(URI.create(url)).download(s.getShortId(), Token.get());

                        if (bytes.isOK()) {
                            for (URI uri : clients.keySet()) {

                                // Se for diferente do blob apagado e do blob de origem
                                if (!uri.toString().equals(url) && !uri.toString().equals(blobURI.toString())) {

                                    Result<Void> upload = clients.get(uri).upload(s.getShortId(), bytes.value(),
                                            Token.get());

                                    if (upload.isOK()) {
                                        // Atualizar a carga do blob
                                        blobLoad.put(uri, blobLoad.get(uri) + 1);

                                        // Atualizar o url do short
                                        String newUrl = url + RestBlobs.PATH + "/" + s.getShortId() + "|"
                                                + uri.toString() + RestBlobs.PATH + "/" + s.getShortId();
                                        s.setBlobUrl(newUrl);
                                        Hibernate.getInstance().update(s);
                                        break;
                                    }
                                }
                            } // Saida do break
                        }
                    }
                }
            }
        }
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        String[] blobs = minLoad();
        Short s = new Short(userId, blobs); // blobs FORMAT: https://hostname:port/rest/blobs/blobID ou
                                            // grpc://hostname:port/grpc/blobs/blobID

        // Atualizar a carga do blob
        for (int i = 0; i < blobs.length; i++)
            blobLoad.put(URI.create(blobs[i]), blobLoad.get(URI.create(blobs[i])) + 1);

        Hibernate.getInstance().persist(s); // URL1?verifier=xxxxx&timestamp=now|URL2?verifier=yyyyy&timestamp=now

        for (int i = 0; i < blobs.length; i++)
            blobs[i] += RestBlobs.PATH + "/" + s.getShortId();

        return Result.ok(constructUrl(blobs, s));
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);

        // Check if the short exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Short s = shorts.get(0);

        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(s.getOwnerId(), password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        String[] shortenURL = getShortenBlobURL(s);

        for (String url : shortenURL) {
            Blobs client2 = ClientFactory.getBlobsClient(URI.create(url));
            Result<Void> deleteBlob = client2.delete(s.getShortId(), Token.get());

            // Check if the blob was deleted
            if (!deleteBlob.isOK() && !deleteBlob.error().equals(Result.ErrorCode.NOT_FOUND))
                return Result.error(deleteBlob.error());

            // Atualizar a carga do blob
            blobLoad.put(URI.create(url), blobLoad.get(URI.create(url)) - 1);
        }

        List<Likes> likes = Hibernate.getInstance().jpql(String.format(likesByShortId, s.getShortId()), Likes.class);

        for (Likes l : likes)
            Hibernate.getInstance().delete(l);

        Hibernate.getInstance().delete(s);
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);

        // Check if the short exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Short s = shorts.get(0);
        String[] blobs = s.getBlobUrl().split("\\|");
        return Result.ok(constructUrl(blobs, s));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(userId, "WrOnGpAsSwOrD");

        // Check if the user exists
        if (uCheck.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        List<String> shorts = Hibernate.getInstance().jpql(String.format(shortIdByOwnerId, userId), String.class);

        return Result.ok(shorts);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Users client = ClientFactory.getUsersClient();
        Result<User> u1Check = client.getUser(userId1, password);

        // Check if the first user exists and if the password is correct
        if (!u1Check.isOK())
            return Result.error(u1Check.error());

        Result<User> u2Check = client.getUser(userId1, "WrOnGpAsSwOrD");

        // Check if the second user exists
        if (u2Check.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        List<Follows> follows = Hibernate.getInstance().jpql(String.format(followsByUserIds, userId1, userId2),
                Follows.class);

        if (isFollowing) {
            if (follows.isEmpty()) {
                Follows f = new Follows(userId1, userId2);
                Hibernate.getInstance().persist(f);

            } else
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            if (!follows.isEmpty())
                Hibernate.getInstance().delete(follows.get(0));
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        List<String> followers = Hibernate.getInstance().jpql(String.format(followersByUserId, userId), String.class);
        return Result.ok(followers);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);
        Short s = shorts.get(0);

        List<Likes> likes = Hibernate.getInstance().jpql(String.format(likeByUserIdAndShortId, userId, shortId),
                Likes.class);

        // Check if the user has already liked the short and if the user is trying to,
        // or vice versa
        if (likes.isEmpty() == !isLiked)
            return Result.error(Result.ErrorCode.CONFLICT);

        if (isLiked) {
            s.addLike();

            Likes l = new Likes(userId, shortId);
            Hibernate.getInstance().persist(l);

        } else {
            s.removeLike();

            Hibernate.getInstance().delete(likes.get(0));
        }

        Hibernate.getInstance().update(s);
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);

        // Check if the short exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Short s = shorts.get(0);

        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(s.getOwnerId(), password);

        // Check if the password is correct
        if (!uCheck.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);

        List<String> likes = Hibernate.getInstance().jpql(String.format(likesIdByShortId, shortId), String.class);
        return Result.ok(likes);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Users client = ClientFactory.getUsersClient();
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        List<String> following = Hibernate.getInstance().jpql(String.format(followingIdByUserId, userId), String.class);
        List<Short> feedShorts = new ArrayList<>();

        List<Short> ownShorts = Hibernate.getInstance().jpql(String.format(shortsByOwnerId, userId), Short.class);
        if (!ownShorts.isEmpty())
            feedShorts.addAll(ownShorts);

        if (!following.isEmpty()) {
            for (String f : following) {
                List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortsByOwnerId, f), Short.class);

                if (!shorts.isEmpty()) {
                    for (Short s : shorts)
                        feedShorts.add(s);
                }
            }
        }

        feedShorts.sort(Comparator.naturalOrder());
        List<String> feed = new ArrayList<>();

        for (Short s : feedShorts)
            feed.add(s.getShortId());

        return Result.ok(feed);
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortsByOwnerId, userId), Short.class);

        // Deleting shorts and its likes and blobs
        for (Short s : shorts) {
            for (String url : getShortenBlobURL(s)) {
                Blobs client = ClientFactory.getBlobsClient(URI.create(url));
                Result<Void> delete = client.delete(s.getShortId(), Token.get());

                // Check if the blob was deleted
                if (!delete.isOK())
                    return Result.error(delete.error());

                // Atualizar a carga do blob
                blobLoad.put(URI.create(url), blobLoad.get(URI.create(url)) - 1);
            }

            List<Likes> likes = Hibernate.getInstance().jpql(String.format(likesByShortId, s.getShortId()),
                    Likes.class);
            for (Likes l : likes)
                Hibernate.getInstance().delete(l);

            Hibernate.getInstance().delete(s);
        }

        List<Likes> ownLikes = Hibernate.getInstance().jpql(String.format(likesByUserId, userId), Likes.class);

        // Deleting user likes
        for (Likes l : ownLikes) {
            String shortId = l.getShortId();

            Short s = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class).get(0);
            s.removeLike();

            Hibernate.getInstance().update(s);
            Hibernate.getInstance().delete(l);
        }

        List<Follows> follows = Hibernate.getInstance().jpql(String.format(followsByUserId, userId, userId),
                Follows.class);

        // Deleting user following and followers
        for (Follows f : follows)
            Hibernate.getInstance().delete(f);

        return Result.ok();
    }

    public Result<Void> checkBlobId(String blobId) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, blobId), Short.class);

        // Check if the short with the given blobId exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok();
    }

    private String[] minLoad() {
        int i = Math.min(2, blobLoad.size());
        String[] minLoad = new String[i];

        int min = Integer.MAX_VALUE;
        for (Map.Entry<URI, Integer> entry : blobLoad.entrySet()) {
            if (entry.getValue() < min) {
                min = entry.getValue();
                minLoad[0] = entry.getKey().toString();
            }
        }

        if (i == 2) {
            min = Integer.MAX_VALUE;
            for (Map.Entry<URI, Integer> entry : blobLoad.entrySet()) {
                if (entry.getValue() < min && !entry.getKey().toString().equals(minLoad[0])) {
                    min = entry.getValue();
                    minLoad[1] = entry.getKey().toString();
                }
            }
        }

        return minLoad;
    }

    private Short constructUrl(String[] blobs, Short s) {
        long timestamp = System.currentTimeMillis();
        String[] urls = blobs;
        String[] verifiers = new String[blobs.length];

        for (int i = 0; i < blobs.length; i++) {
            verifiers[i] = org.apache.commons.codec.digest.DigestUtils.sha256Hex(urls[i] + timestamp + privateKey);
            urls[i] += "?verifier=" + verifiers[i] + "&timestamp=" + timestamp;
        }

        String finalURL = urls[0];
        if (urls.length > 1)
            finalURL += "|" + urls[1];

        s.setBlobUrl(finalURL);
        return s;
    }

    private String[] getShortenBlobURL(Short s) {
        String[] shortenURLs = s.getBlobUrl().split(RestBlobs.PATH + "/" + s.getShortId());
        String[] finalURLs = new String[shortenURLs.length];

        for (int i = 0; i < shortenURLs.length; i++) {
            if (shortenURLs[i].charAt(0) == '|')
                finalURLs[i] = shortenURLs[i].substring(1);
            else
                finalURLs[i] = shortenURLs[i];
        }

        return finalURLs;
    }

}