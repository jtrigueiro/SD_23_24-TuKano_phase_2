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
	String BLOB_ID = "blobId";
 
 	@POST
 	@Path("{" + BLOB_ID +"}")
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	void upload(@PathParam(BLOB_ID) String blobId, byte[] bytes, @QueryParam(VERIFIER) String verifier, @QueryParam(TIMESTAMP) String timestamp);

 	@GET
 	@Path("{" + BLOB_ID +"}") 	
 	@Produces(MediaType.APPLICATION_OCTET_STREAM)
 	byte[] download(@PathParam(BLOB_ID) String blobId, @QueryParam(VERIFIER) String verifier, @QueryParam(TIMESTAMP) String timestamp);

	@DELETE
	@Path("{" + BLOB_ID +"}")
	void delete(@PathParam(BLOB_ID) String blobId);
}
