import kb.dto.Node;
import kb.repository.KB;
import kb.repository.SodaliteRepository;
import nl.jads.refactoringod.RefactoringOptionDiscovererKBApi;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class RefOptionDiscovererlTest {
    private static SodaliteRepository repositoryManager;
    private static Repository repository;
    private static KB kb;

    @BeforeAll
    static void beforeAll() {
        repositoryManager = new SodaliteRepository(".", "/config.ttl");
        kb = new KB(repositoryManager, "TOSCA");

        repository = repositoryManager.getRepository("TOSCA");

        RepositoryConnection repositoryConnection = repository.getConnection();
        // add the RDF data from the inputstream directly to our database
        try {
            InputStream input =
                    RefOptionDiscovererlTest.class.getResourceAsStream("/import/DUL.rdf");
            repositoryConnection.add(input, "", RDFFormat.RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // add the RDF data from the inputstream directly to our database
        try {
            InputStream input =
                    RefOptionDiscovererlTest.class.getResourceAsStream("/core/sodalite-metamodel.ttl");
            repositoryConnection.add(input, "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // add the RDF data from the inputstream directly to our database
        try {
            InputStream input =
                    RefOptionDiscovererlTest.class.getResourceAsStream("/core/tosca-builtins.ttl");
            repositoryConnection.add(input, "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // add the RDF data from the inputstream directly to our database
        try {
            InputStream input =
                    RefOptionDiscovererlTest.class.getResourceAsStream("/snow/snow_tier1.ttl");
            repositoryConnection.add(input, "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // add the RDF data from the inputstream directly to our database
        try {
            InputStream input =
                    RefOptionDiscovererlTest.class.getResourceAsStream("/snow/snow_tier2.ttl");
            repositoryConnection.add(input, "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Set<Node> nodes = kbApi.getComputeNodeInstances(strings,
                "( ?flavor = \"m1.small\" ) && ( ?image = \"centos7\" )");
        for (Node node : nodes) {
            System.out.println(node.getUri());
        }
        List<String> strings1 = new ArrayList<>();
        strings1.add("image_name");
        strings1.add("exposed_ports");
        Set<Node> nodes1 = kbApi.getSoftwareComponentNodeInstances(strings1,
                "( ?image_name = \"snow-skyline-extractor\" ) && ( ?exposed_ports = \"8080\" )");
        for (Node node : nodes1) {
            assertNotNull(node);
            System.out.println(node.getUri());
        }
    }

}
