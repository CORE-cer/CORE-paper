package edu.puc.core.engine.executors;

import edu.puc.core.util.CliParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

public abstract class QuerySupplier {

    public static Supplier<String> FILE(String filePath) throws IOException {
        String queryDelimiter = ";\n";
        Iterator<String> queries;
        queries = Arrays.asList(
                new String(Files.readAllBytes(Paths.get(filePath))).split(queryDelimiter)
        ).iterator();

        return () -> queries.hasNext() ? queries.next() : null;
    }

    public static Supplier<String> CLI() {
        CliParser cliParser = new CliParser();

        return cliParser::scan;
    }
}
