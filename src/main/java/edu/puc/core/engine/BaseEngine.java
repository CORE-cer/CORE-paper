package edu.puc.core.engine;

import edu.puc.core.engine.executors.ExecutorManager;
import edu.puc.core.engine.rmi.RemoteCOREInterface;
import edu.puc.core.engine.streams.StreamManager;
import edu.puc.core.execution.BaseExecutor;
import edu.puc.core.execution.callback.MatchCallback;
import edu.puc.core.execution.callback.MatchCallbackType;
import edu.puc.core.execution.structures.output.CDSComplexEventGrouping;
import edu.puc.core.parser.plan.Stream;
import edu.puc.core.runtime.events.Event;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseEngine implements RemoteCOREInterface {
    public static final Logger LOGGER = Logger.getGlobal();
    public static final String jvmId = ManagementFactory.getRuntimeMXBean().getName();
    public static boolean fastRun = false;
    static Registry createdRegistry;

    protected ExecutorManager executorManager;
    protected StreamManager streamManager;

    public static Engine newEngine(ExecutorManager executorManager, StreamManager streamManager) throws Exception {
        return newEngine(executorManager, streamManager, false, false, false);
    }

    public static Engine newEngine(ExecutorManager executorManager, StreamManager streamManager, boolean logMetrics, boolean fastRun, boolean offline)
            throws Exception {
        BaseEngine.fastRun = fastRun;

        Engine engine;

        if (logMetrics) {
            LOGGER.info("Initializing MeasureEngine");
            engine = new MeasureEngine(executorManager, streamManager);
        } else {
            LOGGER.info("Initializing Engine");
            engine = new Engine(executorManager, streamManager);
        }

        if (!offline) {
            LOGGER.info("Initializing RMI Server...");
            try {
                // Create Remote stub
                createdRegistry = LocateRegistry.createRegistry(1098);
                RemoteCOREInterface stub = (RemoteCOREInterface) UnicastRemoteObject.exportObject(engine, 1100);

                // Bind the remote object's stub in the registry
                createdRegistry.bind(RemoteCOREInterface.RMI_NAME, stub);

                LOGGER.info("RMI Server ready");
            } catch (Exception e) {
                LOGGER.severe("Server exception: " + e.toString());
                e.printStackTrace();
                throw e;
            }
        } else {
            LOGGER.info("Initializing in offline mode");
        }

        return engine;
    }

    public abstract void sendEvent(Event event);

    public abstract void setMatchCallback(Consumer<CDSComplexEventGrouping> callback);

    public abstract void start();

    public abstract Event nextEvent() throws InterruptedException;

    public abstract Map<String, BaseExecutor> getExecutors();


    /************* REMOTE INTERFACE IMPLEMENTATION *************/

    /** ExecutorManager **/
    // Return null if name exists. TODO: raise QueryExistsException
    public boolean addQuery(String name, String query, MatchCallbackType callbackType, Object... args) throws RemoteException {
        LOGGER.info("addQuery() with name " + name);
        return executorManager.newExecutor(name, query, new MatchCallback(callbackType, args)) != null;
    }

    public boolean addQuery(String name, String query) throws RemoteException {
        return executorManager.newExecutor(name, query) != null;
    }

    // Return empty String if name doesn't exist. TODO: raise QueryNotExistsException
    public String getQuery(String name) throws RemoteException {
        BaseExecutor executor = executorManager.getExecutors().get(name);

        return executor != null ? executor.toJSON().toString() : "";
    }

    public List<String> listQueries() throws RemoteException {
        Map<String, BaseExecutor> executorsCopy = executorManager.getExecutors();
        List< String> out = new ArrayList<>();

        executorsCopy.forEach( (name, executor) -> {
            JSONObject json = executor.toJSON();
            json.put("name", name);
            out.add(json.toString());
        } );

        return out;
    }

    // Return null if name doesn't exist. TODO: raise QueryNotExistsException
    public boolean removeQuery(String name) throws RemoteException {
        LOGGER.info("removeQuery() with name " + name);
        return executorManager.removeExecutor(name) != null;
    }


    /** StreamManager **/
    public List<String> listStreams() throws RemoteException {
        Collection<Stream> streams = Stream.getAllStreams().values();
        return streams.stream()
                .map(Stream::toJSON)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

}
