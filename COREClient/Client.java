import edu.puc.core.engine.rmi.RemoteCOREClient;
import static edu.puc.core.execution.callback.MatchCallbackType.WRITE;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;


public class Client {

    public static void main(String[] args) throws RemoteException, NotBoundException, InterruptedException {

        String query = "SELECT BatteryVoltage\n"
                .concat("FROM S\n")
                .concat("WHERE ( BatteryVoltage )\n")
                .concat("FILTER ( BatteryVoltage[value < 4.3] )\n");

	String query2 = "SELECT RelativeAirHumidity\n"
		.concat("FROM S\n")
                .concat("WHERE ( RelativeAirHumidity )\n")
                .concat("FILTER ( RelativeAirHumidity[value < 27] )\n");

	System.out.println("Conectando a servidor...");
	RemoteCOREClient remoteClient = new RemoteCOREClient(args);
	System.out.println("Conectado!");

	remoteClient.addQuery("test", query, WRITE);
	remoteClient.addQuery("test2", query2, WRITE, "matias");
	System.out.println(remoteClient.listQueries());
	System.out.println(remoteClient.listStreams());
	System.out.println(remoteClient.getQuery("test"));
	Thread.sleep(5000);
	remoteClient.removeQuery("test2");
	System.out.println(remoteClient.listQueries());
	System.out.println(remoteClient.getQuery("test"));
    }
}
