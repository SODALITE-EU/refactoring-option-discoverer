package nl.jads.refactoringod.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import kb.dto.Node;
import kb.repository.KB;
import nl.jads.refactoringod.RefactoringOptionDiscovererKBApi;
import nl.jads.refactoringod.dto.FindNodeInput;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Path("/refactoringoptions")
public class RefactoringOptionDiscovererService {
    @Context
    ServletContext servletContext;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/computenodes")
    public Response findComputeNodeInstances(FindNodeInput findNodeInput) throws IOException {
        String actualPath = servletContext.getRealPath("/WEB-INF/classes");
        RefactoringOptionDiscovererKBApi kbApi = new RefactoringOptionDiscovererKBApi(getKB(findNodeInput));
        Set<Node> computeNodeInstances = kbApi.getComputeNodeInstances(findNodeInput);
        return Response.ok(computeNodeInstances).header(
                "Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Origin", "POST").build();
    }

    @Path("/componetnodes")
    public Response findSoftwareComponentNodeInstances(FindNodeInput findNodeInput) throws IOException {
        String actualPath = servletContext.getRealPath("/WEB-INF/classes");
        RefactoringOptionDiscovererKBApi kbApi = new RefactoringOptionDiscovererKBApi(getKB(findNodeInput));
        Set<Node> computeNodeInstances = kbApi.getSoftwareComponentNodeInstances(findNodeInput);
        return Response.ok(computeNodeInstances).header(
                "Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Origin", "POST").build();
    }

    private KB getKB(FindNodeInput findBugInput) {
        String server = findBugInput.getServer();
        if (server == null || "".equals(server.trim())) {
            server = KB.SERVER_URL;
        }
        String repo = findBugInput.getRepository();
        if (repo == null || "".equals(repo.trim())) {
            repo = KB.REPOSITORY;
        }
        return new KB(server, repo);
    }
}
