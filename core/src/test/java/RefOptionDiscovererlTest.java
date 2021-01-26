import kb.dto.Node;
import kb.repository.KB;
import kb.repository.SodaliteRepository;
import nl.jads.refactoringod.RefactoringOptionDiscovererKBApi;
import nl.jads.refactoringod.dto.FindNodeInput;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

public class RefOptionDiscovererlTest {
    private static SodaliteRepository repositoryManager;
    private static Repository repository;
    private static KB kb;
    private static final Logger log = Logger.getLogger(RefOptionDiscovererlTest.class.getName());

    @BeforeAll
    static void beforeAll() throws IOException {
        repositoryManager = new SodaliteRepository(".", "/config.ttl");
        kb = new KB(repositoryManager, "TOSCA");
        repository = repositoryManager.getRepository("TOSCA");
        RepositoryConnection repositoryConnection = repository.getConnection();
        repositoryConnection.add(RefOptionDiscovererlTest.class.getResourceAsStream("/import/DUL.rdf"), "", RDFFormat.RDFXML);
        repositoryConnection.add(RefOptionDiscovererlTest.class.getResourceAsStream("/core/sodalite-metamodel.ttl"), "", RDFFormat.TURTLE);
        repositoryConnection.add(RefOptionDiscovererlTest.class.getResourceAsStream("/core/tosca-builtins.ttl"), "", RDFFormat.TURTLE);
        repositoryConnection.add(RefOptionDiscovererlTest.class.getResourceAsStream("/snow/snow_tier1.ttl"), "", RDFFormat.TURTLE);
        repositoryConnection.add(RefOptionDiscovererlTest.class.getResourceAsStream("/snow/snow_tier2.ttl"), "", RDFFormat.TURTLE);
        repositoryConnection.close();
    }

    @AfterAll
    static void afterAll() {
        repository.shutDown();
        repositoryManager.removeRepository("TOSCA");
        repositoryManager.shutDown("TEST");
    }

    @Test
    void testOptionDiscovery() {
        RefactoringOptionDiscovererKBApi kbApi = new RefactoringOptionDiscovererKBApi(kb);
        List<String> strings = new ArrayList<>();
        strings.add("flavor");
        strings.add("image");
        FindNodeInput findNodeInput = new FindNodeInput();
        findNodeInput.setExpr(
                "( ?flavor = \"m1.small\" ) && ( ?image = \"centos7\" )");
        findNodeInput.setVars(strings);
        Set<Node> nodes = kbApi.getComputeNodeInstances(findNodeInput);
        for (Node node : nodes) {
            log.info(node.getUri());
        }
        List<String> strings1 = new ArrayList<>();
        strings1.add("image_name");
        strings1.add("exposed_ports");
        FindNodeInput findNodeInput1 = new FindNodeInput();
        findNodeInput1.setVars(strings1);
        findNodeInput1.setExpr("( ?image_name = \"snow-skyline-extractor\" ) && ( ?exposed_ports = \"8080\" )");
        Set<Node> nodes1 = kbApi.getSoftwareComponentNodeInstances(findNodeInput1);
        for (Node node : nodes1) {
            assertNotNull(node);
            log.info(node.getUri());
        }
    }
}
