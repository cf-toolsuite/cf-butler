package io.pivotal.cfapp.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.io.File;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MavenPomReader {

    private String group;
    private boolean omitInheritedVersions;

    public MavenPomReader(String group) {
        this.group = group;
    }

    public MavenPomReader(String group, boolean omitInheritedVersions) {
        this.group = group;
        this.omitInheritedVersions = omitInheritedVersions;
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
        Set<String> dependenciesInfo = new HashSet<>();
        document.getDocumentElement().normalize();

        // Check for parent artifact
        NodeList parents = document.getElementsByTagName("parent");
        extractDependencyInfo(parents, dependenciesInfo);

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
                if (omitInheritedVersions) {
                    if ((version != null && !version.isEmpty())) {
                        dependenciesInfo.add(groupId + ":" + artifactId + ":" + version);
                    }
                } else {
                    dependenciesInfo.add(groupId + ":" + artifactId + ":" + version);
                }
            }
        }
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    private boolean containsDependency(String groupId) {
        return groupId.contains(this.group);
    }

    public static void main(String[] args) {
        MavenPomReader reader = new MavenPomReader("org.springframework", true);
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
