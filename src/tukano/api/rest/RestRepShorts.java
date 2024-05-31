package tukano.api.rest;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tukano.api.Short;

/**
 * 
 * Interface for the Replicated Shorts service.
 * 
 * This service allows existing users to create or delete short videos.
 * Users can follow other users to gain access to their short videos.
 * User can add or remove likes to short videos.
 * 
 * @author dl, jt
 *
 */
@Path(RestRepShorts.PATH)
public interface RestRepShorts {

	String NAME = "repShorts";
    String PATH = "/shorts";
    public static final String HEADER_VERSION = "X-SHORTS-version";

    String USER_ID = "userId";
	String USER_ID1 = "userId1";
	String USER_ID2 = "userId2";
	String SHORT_ID = "shortId";
	String BLOB_ID = "blobId";

	String PWD = "pwd";
	String FEED = "/feed";
	String LIKES = "/likes";
	String SHORTS = "/shorts";
	String FOLLOWERS = "/followers";
	String DELETES = "/delete";
	String CHECK = "/check";
	String TOKEN = "token";

	/**
	 * Creates a new short, generating its unique identifier.
	 * The result short will include the blob storage location where the media
	 * should be uploaded.
	 * 
	 * @param userId   - the owner of the new short
	 * @param password - the password of owner of the new short
	 * @return (OK, Short) if the short was created;
	 *         NOT FOUND, if the owner of the short does not exist;
	 *         FORBIDDEN, if the password is not correct;
	 *         BAD_REQUEST, otherwise.
	 */
	@POST
	@Path("/{" + USER_ID + "}")
	@Produces(MediaType.APPLICATION_JSON)
	Short createShort(@HeaderParam(HEADER_VERSION) long version, @PathParam(USER_ID) String userId, @QueryParam(PWD) String password);

	/**
	 * Deletes a given Short.
	 * 
	 * @param shortId the unique identifier of the short to be deleted
	 * @return (OK,void),
	 *         NOT_FOUND if shortId does not match an existing short
	 *         FORBIDDEN, if the password is not correct;
	 */
	@DELETE
	@Path("/{" + SHORT_ID + "}")
	void deleteShort(@HeaderParam(HEADER_VERSION) long version, @PathParam(SHORT_ID) String shortId, @QueryParam(PWD) String password);

	/**
	 * Retrieves a given Short.
	 * 
	 * @param shortId the unique identifier of the short to be retrieved
	 * @return (OK,Short),
	 *         NOT_FOUND if shortId does not match an existing short
	 */
    @GET
    @Path("/{" + SHORT_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
	Short getShort(@HeaderParam(HEADER_VERSION) long version, @PathParam(RestShorts.SHORT_ID) String shortId);

	/**
	  * Retrieves the list of identifiers of the shorts created by the given user.
	 * 
	 * @param userId the user that owns the requested shorts
	 * @return (OK, List<String>|empty list) or NOT_FOUND if the user does not exist
	 */
	@GET
	@Path("/{" + USER_ID + "}" + SHORTS)
	@Produces(MediaType.APPLICATION_JSON)
	List<String> getShorts(@HeaderParam(HEADER_VERSION) long version, @PathParam(USER_ID) String userId);


	/**
	 * Causes a user to follow the shorts of another user.
	 * 
	 * @param userId1     the user that will follow or cease to follow the
	 *                    followed user
	 * @param userId2     the followed user
	 * @param isFollowing flag that indicates the desired end status of the
	 *                    operation
	 * @param password    the password of the follower
	 * @return (OK,),
	 *         NOT_FOUND if any of the users does not exist
	 *         FORBIDDEN if the password is incorrect
	 */
	@POST
	@Path("/{" + USER_ID1 + "}/{" + USER_ID2 + "}" + FOLLOWERS)
	@Consumes(MediaType.APPLICATION_JSON)
	void follow(@HeaderParam(HEADER_VERSION) long version, @PathParam(USER_ID1) String userId1, @PathParam(USER_ID2) String userId2, boolean isFollowing,
			@QueryParam(PWD) String password);

	/**
	 * Retrieves the list of users following a given user
	 * 
	 * @param userId   - the followed user
	 * @param password - the password of the followed user
	 * @return (OK, List<String>|empty list) the list of users that follow another
	 *         user, or empty if the user has no followers
	 *         NOT_FOUND if the user does not exists
	 *         FORBIDDEN if the password is incorrect
	 */
	@GET
	@Path("/{" + USER_ID + "}" + FOLLOWERS)
	@Produces(MediaType.APPLICATION_JSON)
	List<String> followers(@HeaderParam(HEADER_VERSION) long version, @PathParam(USER_ID) String userId, @QueryParam(PWD) String password);

	/**
	 * Adds or removes a like to a short
	 * 
	 * @param shortId - the identifier of the post
	 * @param userId  - the identifier of the user
	 * @param isLiked - a flag with true to add a like, false to remove the like
	 * @return (OK,void) if the like was added/removed;
	 *         NOT_FOUND if either the short or the like being removed does not
	 *         exist,
	 *         CONFLICT if the like already exists.
	 *         FORBIDDEN if the password of the user is incorrect
	 *         BAD_REQUEST, otherwise
	 */
	@POST
	@Path("/{" + SHORT_ID + "}/{" + USER_ID + "}" + LIKES)
	@Consumes(MediaType.APPLICATION_JSON)
	void like(@HeaderParam(HEADER_VERSION) long version, @PathParam(SHORT_ID) String shortId, @PathParam(USER_ID) String userId, boolean isLiked,
			@QueryParam(PWD) String password);

	/**
	 * Returns all the likes of a given short
	 * 
	 * @param shortId  the identifier of the short
	 * @param password the password of the owner of the short
	 * @return (OK,List<String>|empty list), 
	 *         NOT_FOUND if there is no Short with the given shortId
	 *         FORBIDDEN if the password is incorrect
	 */
	@GET
	@Path("/{" + SHORT_ID + "}" + LIKES)
	@Produces(MediaType.APPLICATION_JSON)
	List<String> likes(@HeaderParam(HEADER_VERSION) long version, @PathParam(SHORT_ID) String shortId, @QueryParam(PWD) String password);

	/**
	 * Returns the feed of the user, sorted by age. The feed is the list of shorts
	 * made by
	 * the users followed by the user.
	 * 
	 * @param userId   user of the requested feed
	 * @param password the password of the user
	 *  @return (OK,List<String>|empty list)
	 *         NOT_FOUND if the user does not exists
	 *         FORBIDDEN if the password is incorrect
	 */
	@GET
	@Path("/{" + USER_ID + "}" + FEED)
	List<String> getFeed(@HeaderParam(HEADER_VERSION) long version, @PathParam(USER_ID) String userId, @QueryParam(PWD) String password);

	/**
	 * Deletes all the shorts of a given user
	 * 
	 * @param userId the user that owns the shorts to be deleted
	 * @return (OK,void),
	 *         NOT_FOUND if the blobId of a short owned by the user does not match an existing blob
	 */
	@DELETE
	@Path("/{" + USER_ID + "}" + DELETES)
	void deleteUserShorts(@HeaderParam(HEADER_VERSION) long version, @PathParam(USER_ID) String userId, @QueryParam(TOKEN) String token);
}
