package edu.puc.core.engine.rmi;

import edu.puc.core.execution.callback.MatchCallbackType;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import static edu.puc.core.engine.rmi.RemoteCOREInterface.RMI_NAME;


public class RemoteCOREClient {
    private final RemoteCOREInterface stub;


    public RemoteCOREClient(String[] args) throws RemoteException, NotBoundException {
        String host = (args.length < 1) ? null : args[0];
        System.out.println("Buscando Registry en " + host);

        Registry registry = LocateRegistry.getRegistry(host, 1099);

        System.out.println("Registry encontrado: " + registry);
        System.out.println("Buscando stub");

        stub = (RemoteCOREInterface) registry.lookup(RMI_NAME);

        System.out.println("Stub encontrado: " + stub);
    }

    public boolean addQuery(String name, String query, MatchCallbackType callbackType, Object... args) {
        try {
            return stub.addQuery(name, query, callbackType, args);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            return false;
        }
    }

    public boolean addQuery(String name, String query) {
        try {
            return stub.addQuery(name, query);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            return false;
        }
    }

    public boolean removeQuery(String name) {
        try {
            return stub.removeQuery(name);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            return false;
        }
    }

    public String getQuery(String name) {
        try {
            return stub.getQuery(name);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            return "";
        }
    }

    public List<String> listQueries() {
        try {
            return stub.listQueries();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            return null;
        }
    }

    public List<String> listStreams() {
        try {
            return stub.listStreams();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            return null;
        }
    }
}