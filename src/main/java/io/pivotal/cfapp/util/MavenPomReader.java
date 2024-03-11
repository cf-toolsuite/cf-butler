package io.pivotal.cfapp.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MavenPomReader implements JavaArtifactReader {

    private Logger log = LoggerFactory.getLogger(MavenPomReader.class);

    private Set<String> groups = new HashSet<>();
    private boolean omitInheritedVersions;

    public MavenPomReader(Set<String> groups) {
        this.groups = groups;
    }

    public MavenPomReader(Set<String> groups, boolean omitInheritedVersions) {
        this.groups = groups;
        this.omitInheritedVersions = omitInheritedVersions;
    }

    public Set<String> read(String pomFile) {
        try {
            return readPOM(pomFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.warn("Could not determine Spring dependencies", e);
            return Collections.emptySet();
        }
    }

    public Set<String> readPOM(File pomFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(pomFile);
        return processDocument(document);
    }

    public Set<String> readPOM(String pomContent) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(pomContent)));
        return processDocument(document);
    }

    private Set<String> processDocument(Document document) {
        Set<String> dependenciesInfo = new LinkedHashSet<>();
        document.getDocumentElement().normalize();

        // Check for parent artifact
        NodeList parents = document.getElementsByTagName("parent");
        extractDependencyInfo(parents, dependenciesInfo);

        // Check for managed dependencies
        NodeList managedDependencies = document.getElementsByTagName("dependencyManagement");
        extractDependencyInfo(managedDependencies, dependenciesInfo);

        // Check for dependencies
        NodeList dependencies = document.getElementsByTagName("dependency");
        extractDependencyInfo(dependencies, dependenciesInfo);

        return dependenciesInfo;
    }

    private void extractDependencyInfo(NodeList nodes, Set<String> dependenciesInfo) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            String artifactId = getTagValue("artifactId", element);
            String groupId = getTagValue("groupId", element);

            if (containsDependency(groupId)) {
                String version = getTagValue("version", element);

                if (omitInheritedVersions && StringUtils.isNotBlank(version)) {
                    addDependencyInfo(dependenciesInfo, groupId, artifactId, version);
                } else if (!omitInheritedVersions) {
                    version = StringUtils.isBlank(version) ? getManagedVersion(dependenciesInfo, groupId) : version;
                    addDependencyInfo(dependenciesInfo, groupId, artifactId, version);
                }
            }
        }
    }

    private void addDependencyInfo(Set<String> dependenciesInfo, String groupId, String artifactId, String version) {
        dependenciesInfo.add(String.format("%s:%s:%s", groupId, artifactId, version));
    }

    private String getManagedVersion(Set<String> dependenciesInfo, String groupId) {
        return dependenciesInfo.stream()
            .filter(d -> d.startsWith(groupId))
            .filter(d -> d.split(":").length == 3)
            .findFirst()
            .map(d -> d.split(":")[2])
            .orElse("");
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return "";
        }

        Node targetNode = nodes.item(0);
        String value = targetNode.getTextContent();

        String parentName = getNodeName(targetNode.getParentNode());
        String grandParentName = getNodeName(getParentNode(targetNode, 2));
        String greatGrandParentName = getNodeName(getParentNode(targetNode, 3));

        if (isValidHierarchy(parentName, grandParentName, greatGrandParentName)) {
            return value;
        }
        return "";
    }

    private String getNodeName(Node node) {
        return Optional.ofNullable(node).map(Node::getNodeName).orElse("");
    }

    private Node getParentNode(Node node, int level) {
        Node parentNode = node;
        for (int i = 0; i < level && parentNode != null; i++) {
            parentNode = parentNode.getParentNode();
        }
        return parentNode;
    }

    private boolean isValidHierarchy(String parentName, String grandParentName, String greatGrandParentName) {
        if ("parent".equals(parentName)) {
            return "project".equals(grandParentName);
        } else if ("dependency".equals(parentName)) {
            return ("dependencies".equals(grandParentName) && "project".equals(greatGrandParentName)) ||
                ("dependencies".equals(grandParentName) && "dependencyManagement".equals(greatGrandParentName));
        }
        return false;
    }

    private boolean containsDependency(String groupId) {
        boolean exactlyOneMatch = groups.stream().anyMatch(group -> groupId.startsWith(group)) &&
                          groups.stream().noneMatch(group ->
                              !group.equals(groups.stream().filter(g -> groupId.startsWith(g)).findFirst().orElse(null)) &&
                              groupId.startsWith(group));
        return exactlyOneMatch;
    }

    public String mode() {
        return "unpack-pom-contents-in-droplet";
    }

    public static void main(String[] args) {
        MavenPomReader reader = new MavenPomReader(Set.of("org.springframework", "io.pivotal.spring.cloud"), true);
        try {
            //Set<String> dependenciesInfo = reader.readPOM(new File("/home/cphillipson/Documents/development/pivotal/cf/cf-butler/pom.xml")); // or reader.readPOM(pomString);
            Set<String> dependenciesInfo = reader.readPOM(new File(args[0]));
            for (String info : dependenciesInfo) {
                System.out.println("Found dependency: " + info);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
