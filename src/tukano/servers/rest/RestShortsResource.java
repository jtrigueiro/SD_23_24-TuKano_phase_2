package tukano.servers.rest;

import java.util.List;

import jakarta.inject.Singleton;
import tukano.api.Short;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;
import tukano.servers.java.ShortsServer;

@Singleton
public class RestShortsResource extends RestResource implements RestShorts {

    final Shorts impl;

    public RestShortsResource() {
        this.impl = new ShortsServer();
    }

    @Override
    public Short createShort(long version, String userId, String password) {
        return resultOrThrow(impl.createShort(userId, password));
    }

    @Override
    public void deleteShort(long version, String shortId, String password) {
        resultOrThrow(impl.deleteShort(shortId, password));
    }

    @Override
    public Short getShort(long version, String shortId) {
        return resultOrThrow(impl.getShort(shortId));
    }

    @Override
    public List<String> getShorts(long version, String userId) {
        return resultOrThrow(impl.getShorts(userId));
    }

    @Override
    public void follow(long version, String userId1, String userId2, boolean isFollowing, String password) {
        resultOrThrow(impl.follow(userId1, userId2, isFollowing, password));
    }

    @Override
    public List<String> followers(long version, String userId, String password) {
        return resultOrThrow(impl.followers(userId, password));
    }

    @Override
    public void like(long version, String shortId, String userId, boolean isLiked, String password) {
        resultOrThrow(impl.like(shortId, userId, isLiked, password));
    }

    @Override
    public List<String> likes(long version, String shortId, String password) {
        return resultOrThrow(impl.likes(shortId, password));
    }

    @Override
    public List<String> getFeed(long version, String userId, String password) {
        return resultOrThrow(impl.getFeed(userId, password));
    }

    @Override
    public void deleteUserShorts(long version, String userId, String token) {
        resultOrThrow(impl.deleteUserShorts(userId, token));
    }

}
