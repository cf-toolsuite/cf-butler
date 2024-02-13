package io.pivotal.cfapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;

public class JarUtil {

    public static String extractFileContent(File jarFile, String filename) throws IOException {
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
            JarEntry entry;

            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(filename)) {
                    return readFromStream(jarStream);
                }
            }
        }
        return null; // pom.xml not found
    }

    private static String readFromStream(JarInputStream jarStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    // Example usage
    public static void main(String[] args) throws IOException {
        //File jarFile = new File("/home/cphillipson/Documents/development/pivotal/cf/cf-butler/target/cf-butler-1.0-SNAPSHOT.jar");
        File jarFile = new File(args[0]);
        String pomContents = extractFileContent(jarFile, "pom.xml");
        if (pomContents != null) {
            System.out.println(pomContents);
        } else {
            System.out.println("pom.xml not found in the jar file.");
        }
    }
}