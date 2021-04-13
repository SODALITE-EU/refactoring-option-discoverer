package nl.jads.refactoringod;

import kb.dto.Attribute;
import kb.dto.Node;
import kb.dto.Property;
import kb.repository.KB;
import kb.utils.MyUtils;
import kb.utils.QueryUtil;
import nl.jads.refactoringod.dto.FindNodeInput;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RefactoringOptionDiscovererKBApi {

    public static String DCTERMS = "http://purl.org/dc/terms/";
    public static String DUL = "http://www.loa-cnr.it/ontologies/DUL.owl#";
    public static String TOSCA = "https://www.sodalite.eu/ontologies/tosca/";
    public static String SODA = "https://www.sodalite.eu/ontologies/sodalite-metamodel/";
    String PREFIXES = "PREFIX tosca: <https://www.sodalite.eu/ontologies/tosca/> \r\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \r\n" +
            "PREFIX soda: <https://www.sodalite.eu/ontologies/sodalite-metamodel/> \r\n" +
            "PREFIX DUL: <http://www.loa-cnr.it/ontologies/DUL.owl#> \r\n" +
            "PREFIX dcterms: <http://purl.org/dc/terms/> \r\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#> \r\n";
    private KB kb;

    public RefactoringOptionDiscovererKBApi(KB kb) {
        this.kb = kb;
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
            Value value = bindingSet.hasBinding("value") ? bindingSet.getBinding("value").getValue() : null;
            Property a = new Property(p1);
            a.setClassifiedBy(concept);
            a.setValue(value, this.kb);
            properties.add(a);
        }
        result.close();
        return properties;
    }

    public Set<Node> getComputeNodeInstances(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca:tosca.nodes.Compute", findNodeInput.getVars(),
                findNodeInput.getExpr(), "tosca:properties", findNodeInput.getAadm());
    }

    public Set<Node> getSoftwareComponentNodeInstances(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca:tosca.nodes.SoftwareComponent",
                findNodeInput.getVars(), findNodeInput.getExpr(), "tosca:properties",
                findNodeInput.getAadm());
    }

    public Set<Node> getPolicies(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca.policies.Root", findNodeInput.getVars(),
                findNodeInput.getExpr(), "tosca:properties", findNodeInput.getAadm());
    }

    public Set<Node> getNodeMatchingRequirements(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca:tosca.nodes.Compute", findNodeInput.getVars(),
                findNodeInput.getExpr(), "tosca:requirements", findNodeInput.getAadm());
    }

    public Set<Node> getNodeMatchingCapabilities(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca:tosca.nodes.Compute", findNodeInput.getVars(),
                findNodeInput.getExpr(), "tosca:capabilities", findNodeInput.getAadm());
    }

    public Set<Node> getSoftwareComponentNodeMatchingRequirements(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca:tosca.nodes.Compute", findNodeInput.getVars(),
                findNodeInput.getExpr(), "tosca:requirements", findNodeInput.getAadm());
    }

    public Set<Node> getSoftwareComponentNodeMatchingCapabilities(FindNodeInput findNodeInput) {
        return getNodeInstances("tosca:tosca.nodes.Compute", findNodeInput.getVars(),
                findNodeInput.getExpr(), "tosca:capabilities", findNodeInput.getAadm());
    }

    private Set<Node> getNodeInstances(String nodeType, List<String> parameters, String expr,
                                       String propType, String aadm) {
        Set<Node> nodes = new HashSet<>();
        StringBuilder query = new StringBuilder(PREFIXES + "select DISTINCT ?node ?description ?nodetype\n" +
                "where {\n" +
                "\t?nodetype rdfs:subClassOf " + nodeType + " .\n" +
                "\t?node rdf:type ?nodetype .\n");
        if (aadm != null && !aadm.isEmpty()) {
            query.append("\t\t?aadm soda:includesTemplate ?node .\n" +
                    "\t\tFILTER (contains(str(?aadm), \"").append(aadm).append("\")).\n");
        }
        query.append("\tOPTIONAL {?node dcterms:description ?description .}\n" +
                "\tFILTER (?nodetype != ").append(nodeType).append(" ) .\n")
                .append("\tFILTER (?node != owl:Nothing) .\n").append("\t?node soda:hasContext ?context .");
        int i = 0;
        for (String name : parameters) {
            query.append("{\n").append("    ?context ").append(propType).append(" ?concept").append(i).append(" .\n")
                    .append("?concept").append(i).append(" DUL:classifies ?classifier").append(i).append(
                    " FILTER (strends(str(?classifier").append(i).append("), \"/").append(name).append("\")).")
                    .append("    OPTIONAL {?concept").append(i).append(" tosca:hasDataValue ?")
                    .append(name).append(" .}\n").append("    }");
            i++;
        }
        query.append(" Filter (").append(expr).append(")}");
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

    public Set<Node> getNodeMatchingReq(String req, String aadmstring) {
        Set<Node> nodes = new HashSet<>();
        String query = PREFIXES + "select DISTINCT ?node ?description ?nodetype ?v\n" +
                "where {\n" +
                "    ?aadm soda:includesTemplate ?node .\n" +
                "    ?node sesame:directType ?nodetype ;\n" +
                "    rdf:type soda:SodaliteSituation .\n" +
                "    FILTER NOT EXISTS {\n" +
                "      ?node rdfs:subClassOf tosca:tosca.entity.Root .\n" +
                "    }\n" +
                "    FILTER (?nodetype != rdfs:Resource && ?nodetype != DUL:Region)\n" +
                "\tOPTIONAL {?node dcterms:description ?description .}\n" +
                "\tFILTER (?node != owl:Nothing) .\n" +
                "\t?node soda:hasContext/tosca:requirements ?requirement .\n" +
                "    ?requirement DUL:classifies ?classifier .\n" +
                "    ?requirement DUL:hasParameter [DUL:classifies ?r_i; tosca:hasObjectValue ?v] ." +
                "    Filter (STRENDS(str(?v), " + "\""+
                req + "\""+ "))}";
        TupleQueryResult result = QueryUtil.evaluateSelectQuery(kb.getConnection(), query,
                new SimpleBinding("aadm", kb.getFactory().createIRI(aadmstring)));

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

    public KB getKb() {
        return kb;
    }
}
