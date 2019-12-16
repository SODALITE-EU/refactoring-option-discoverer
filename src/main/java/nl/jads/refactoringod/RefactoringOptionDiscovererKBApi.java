package nl.jads.refactoringod;

import kb.KBApi;
import kb.dto.Attribute;
import kb.dto.Node;
import kb.dto.Property;
import kb.utils.MyUtils;
import kb.utils.QueryUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RefactoringOptionDiscovererKBApi extends KBApi {

    String PREFIXES = "PREFIX tosca: <https://www.sodalite.eu/ontologies/tosca/> \r\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \r\n" +
            "PREFIX soda: <https://www.sodalite.eu/ontologies/sodalite-metamodel/> \r\n" +
            "PREFIX DUL: <http://www.loa-cnr.it/ontologies/DUL.owl#> \r\n" +
            "PREFIX snow: <https://www.sodalite.eu/ontologies/snow-blueprint-containerized-OS/> \r\n" +
            "PREFIX dcterms: <http://purl.org/dc/terms/> \r\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> \r\n";

    public static String DCTERMS = "http://purl.org/dc/terms/";
    public static String DUL = "http://www.loa-cnr.it/ontologies/DUL.owl#";
    public static String TOSCA = "https://www.sodalite.eu/ontologies/tosca/";
    public static String SODA = "https://www.sodalite.eu/ontologies/sodalite-metamodel/";

    public RefactoringOptionDiscovererKBApi() {
        super();
    }

    public void shutDown() {
        super.shutDown();
    }

    public Set<Attribute> getAllAttributes() throws IOException {
        Set<Attribute> attributes = new HashSet<>();
        String sparql = MyUtils.fileToString("sparql/getAllAttributes.sparql");
        String query = PREFIXES + sparql;
        TupleQueryResult result = QueryUtil.evaluateSelectQuery(kb.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            IRI attr = (IRI) bindingSet.getBinding("attribute").getValue();
            IRI concept = (IRI) bindingSet.getBinding("p").getValue();

            Attribute a = new Attribute(attr);
            a.setClassifiedBy(concept);

            attributes.add(a);
        }
        result.close();
        return attributes;
    }

    public Set<Property> getProperties() throws IOException {
        Set<Property> properties = new HashSet<>();
        String sparql = MyUtils.fileToString("sparql/getAllProperties.sparql");
        String query = PREFIXES + sparql;
        TupleQueryResult result = QueryUtil.evaluateSelectQuery(kb.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            IRI p1 = (IRI) bindingSet.getBinding("property").getValue();
            IRI concept = (IRI) bindingSet.getBinding("concept").getValue();
            Value _value = bindingSet.hasBinding("value") ? bindingSet.getBinding("value").getValue() : null;

            Property a = new Property(p1);
            a.setClassifiedBy(concept);
            if (_value != null)
                a.setValue(_value, this);

            properties.add(a);
        }
        result.close();
        return properties;
    }

    public Set<Node> getComputeNodeInstances(List<String> parameters, String expr) {
        return getNodeInstances("tosca:tosca.nodes.Compute", parameters, expr);
    }

    public Set<Node> getSoftwareComponentNodeInstances(List<String> parameters, String expr) {
        return getNodeInstances("tosca:tosca.nodes.SoftwareComponent", parameters, expr);
    }

    private Set<Node> getNodeInstances(String nodeType, List<String> parameters, String expr) {
        Set<Node> nodes = new HashSet<>();
        StringBuilder query = new StringBuilder(PREFIXES + "select DISTINCT ?node ?description ?nodetype\n" +
                "where {\n" +
                "\t?nodetype rdfs:subClassOf " + nodeType + " .\n" +
                "\t?node rdf:type ?nodetype .\n" +
                "\tOPTIONAL {?node dcterms:description ?description .}\n" +
                "\tFILTER (?nodetype != " + nodeType + " ) .\n" +
                "\tFILTER (?node != owl:Nothing) .\n" +
                "\t?node soda:hasContext ?context .");
        int i = 0;
        for (String name : parameters) {
            query.append("{\n").append("    ?context tosca:properties ?concept").append(i).append(" .\n")
                    .append("    OPTIONAL {?concept").append(i).append(" DUL:classifies snow:").append(name)
                    .append(" .}\n").append("    OPTIONAL {?concept").append(i).append(" tosca:hasDataValue ?")
                    .append(name).append(" .}\n").append("    }");
            i++;
        }
        query.append(" Filter (").append(expr).append(")}");
//        System.out.println(query);
        TupleQueryResult result = QueryUtil.evaluateSelectQuery(kb.getConnection(), query.toString());

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            IRI node = (IRI) bindingSet.getBinding("node").getValue();
            String description = bindingSet.hasBinding("description")
                    ? bindingSet.getBinding("description").getValue().stringValue()
                    : null;
            IRI superclass = (IRI) bindingSet.getBinding("nodetype").getValue();

            Node n = new Node(node);
            n.setDescription(description);
            n.setType(superclass);

            nodes.add(n);
        }
        result.close();
        return nodes;
    }

    public static void main(String[] args) {
        RefactoringOptionDiscovererKBApi kbApi = new RefactoringOptionDiscovererKBApi();
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
            System.out.println(node.getUri());
        }
    }
}
