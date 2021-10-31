package edu.puc.core.engine.executors;

import edu.puc.core.engine.BaseEngine;
import edu.puc.core.execution.BaseExecutor;
import edu.puc.core.execution.callback.MatchCallback;
import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.parser.CompoundStatementParser;
import edu.puc.core.parser.DeclarationParser;
import edu.puc.core.parser.QueryParser;
import edu.puc.core.parser.plan.LogicalPlan;
import edu.puc.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExecutorManager {
    private final Map<String, BaseExecutor> executors = new HashMap<>();
    private final CompoundStatementParser parser;
    private final Supplier<String> querySupplier;
    private Consumer<CDSComplexEventGrouping> defaultMatchCallback = MatchCallback.getDefault();

    public ExecutorManager(CompoundStatementParser parser) throws IOException {
        this(parser, null);
    }

    public ExecutorManager(CompoundStatementParser parser, Supplier<String> querySupplier) throws IOException {
        this.parser = parser;
        this.querySupplier = querySupplier;
    }

    public static ExecutorManager fromCOREFile(BufferedReader queryFile) throws IOException {
        String line;
        line = queryFile.readLine();

        // Build parser
        CompoundStatementParser parser = new CompoundStatementParser(new DeclarationParser(), new QueryParser());
        String[] streamData = line.split(":", 2);
        String declarations;
        switch (streamData[0]) {
            case "FILE":
                declarations = StringUtils.readFile(streamData[1]);
                break;
            case "CLI":
                throw new Error("Type not implemented yet: " + streamData[0]);
            default:
                throw new Error("Invalid declaration file type: " + streamData[0]);
        }

        // Parse Event and Stream declarations, initialize parser
        parser.parse(declarations);

        // Build Executor Manager
        line = queryFile.readLine();
        Supplier<String> querySupplier = null;
        if (line != null) {
            // Has initial query supplier
            String[] querySource = line.split(":", 2);

            switch (querySource[0]) {
                case "FILE":
                    querySupplier = QuerySupplier.FILE(querySource[1]);
                    break;
                case "CLI":
                    querySupplier = QuerySupplier.CLI();
                    break;
                default:
                    throw new Error("Invalid query file type: " + querySource[0]);
            }
        }

        return new ExecutorManager(parser, querySupplier);
    }

    public void start() {
        if (querySupplier != null) {
            BaseEngine.LOGGER.info("Starting up initial queries");
            this.consumeQuerySupplier();
//            new Thread(this::consumeQuerySupplier).start();
        }
    }

    private void consumeQuerySupplier() {
        String query;
        while (!Thread.currentThread().isInterrupted()) {
            query = querySupplier.get();
            if (query == null) return;
            newExecutor(query);
        }
    }

    public BaseExecutor newExecutor(String query) {
        return newExecutor(query, defaultMatchCallback);
    }

    public BaseExecutor newExecutor(String name, String query) {
        return newExecutor(name, query, defaultMatchCallback);
    }

    public BaseExecutor newExecutor(String query, Consumer<CDSComplexEventGrouping> callback) {
        return newExecutor(String.valueOf(executors.size() + 1), query, callback);
    }

    public BaseExecutor newExecutor(String name, String query, Consumer<CDSComplexEventGrouping> callback) {
        if (executors.containsKey(name)) {
            return null;
        }

        LogicalPlan plan = parser.parse(query).get(0);
        BaseExecutor executor = BaseExecutor.fromPlan(plan);

        executor.setMatchCallback(callback);
        executor.setQuery(query);


        BaseEngine.LOGGER.info("newExecutor() with name " + name);

        executors.put(name, executor);
        return executor;
    }

    // Revisar si mandar executors o si una shallow copy del hashmap
    public Map<String, BaseExecutor> getExecutors() {
        return new HashMap<>(executors);
    }

    public BaseExecutor removeExecutor (String name) {
        BaseEngine.LOGGER.info("removeExecutor() with name " + name);
        return executors.remove(name);
    }

    public void setDefaultMatchCallback(Consumer<CDSComplexEventGrouping> callback) {
        defaultMatchCallback = callback;
    }
}
