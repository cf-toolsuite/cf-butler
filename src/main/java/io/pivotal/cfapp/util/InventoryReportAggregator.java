package io.pivotal.cfapp.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;


public class InventoryReportAggregator {

    // Assumes report file is just detail records without headers and without summary lines
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java io.pivotal.cfapps.util.InventoryReportAggregator file:///path/to/index columns file:///path/to/outputfile.csv");
            System.exit(1);
        }
        String indexFilename = args[0];
        int columns = Integer.valueOf(args[1]);
        String outputfile = args[2];
        URI uri = URI.create(indexFilename);
        Path path = Paths.get(uri);
        StringBuilder builder = new StringBuilder();
        try {
            Stream<String> index = Files.lines(path);
            index.forEach(i -> {
                String[] parts = i.split(",");
                if (parts.length == 2) {
                    String foundation = parts[0];
                    String report = parts[1];
                    try {
                        URI reportURI = URI.create(report);
                        Path reportPath = Paths.get(reportURI);
                        Stream<String> reportLines = Files.lines(reportPath);
                        reportLines.forEach(rl -> {
                            if (rl.length() - rl.replace(",", "").length() >= columns && !rl.startsWith("organization,space")) {
                                builder.append("\"" + foundation + "\"");
                                builder.append(",");
                                builder.append(rl);
                                builder.append("\n");
                            }
                        });
                        reportLines.close();
                    } catch (IOException e) {
                        System.out.println("Problem reading contents of " + report);
                    }
                } else {
                    System.out.println("Ignoring line with [" + i + "] in index file " + indexFilename + ". Each line must contain e.g., [ foundation,/path/to/report/file ].");
                }
            });
            try {
                Path out = Paths.get(URI.create(outputfile));
                String contents = builder.toString();
                Files.write(out, contents.getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
                System.out.println(contents);
            } catch (IOException e) {
                System.out.println("Problem writing to file " + outputfile);
            }
            index.close();
        } catch (IOException e) {
            System.out.println("Problem reading contents of " + indexFilename);
        }
    }
}
