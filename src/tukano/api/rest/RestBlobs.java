package tukano.api.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path(RestBlobs.PATH)
public interface RestBlobs {

	String PATH = "/blobs";
	String VERIFIER = "verifier";
	String TIMESTAMP = "timestamp";
	String TOKEN = "token";
	String BLOB_ID = "blobId";
	String SERVER = "server";

	@POST
	@Path("{" + BLOB_ID + "}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	void upload(@PathParam(BLOB_ID) String blobId, byte[] bytes, @QueryParam(TIMESTAMP) String timestamp,
			@QueryParam(VERIFIER) String verifier);

	@POST
	@Path("{" + BLOB_ID + "}" + SERVER)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	void svr_upload(@PathParam(BLOB_ID) String blobId, byte[] bytes, @QueryParam(TOKEN) String token);

	@GET
	@Path("{" + BLOB_ID + "}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] download(@PathParam(BLOB_ID) String blobId,
			@QueryParam(TIMESTAMP) String timestamp, @QueryParam(VERIFIER) String verifier);

	@GET
	@Path("{" + BLOB_ID + "}" + SERVER)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] svr_download(@PathParam(BLOB_ID) String blobId,
			@QueryParam(TOKEN) String token);

	@DELETE
	@Path("{" + BLOB_ID + "}")
	void delete(@PathParam(BLOB_ID) String blobId, @QueryParam(TOKEN) String token);
}
