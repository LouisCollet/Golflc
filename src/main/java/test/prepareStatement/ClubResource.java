
package test.prepareStatement;

import entite.Club;
import static interfaces.Log.LOG;
import jakarta.annotation.security.DenyAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author Louis Collet
 */
@Path("/clubs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@DenyAll // security audit 2026-03-09 — test endpoint disabled in production
public class ClubResource {

    @Inject
    ClubService service;

    @POST
    public Response save(Club club) {
        try {
            service.save(club);
        }
        catch (Exception ex) {
            LOG.debug("error == " + ex.getMessage());
        }
        return Response.ok().build();
    }
}
