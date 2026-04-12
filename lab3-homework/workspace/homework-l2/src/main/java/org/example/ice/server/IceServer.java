package org.example.ice.server;

import com.zeroc.Ice.*;

import java.lang.Exception;

public class IceServer {
	public void t1(String[] args) {
		int status = 0;
		Communicator communicator = null;

		try {
			communicator = Util.initialize(args);
			ObjectAdapter adapter;

			try {
				adapter = communicator.createObjectAdapter("Adapter1");
			} catch (InitializationException e) {
				System.out.println("(using a hard-coded configuration)");
				adapter = communicator.createObjectAdapterWithEndpoints(
					"Adapter1",
					"tcp -h 0.0.0.0 -p 10010 -z : udp -h 0.0.0.0 -p 10010 -z"
				);
			}

			SharedCounterI sharedServant = new SharedCounterI();
			adapter.addDefaultServant(sharedServant, "shared");

			DedicatedCounterLocator dedicatedLocator = new DedicatedCounterLocator();
			adapter.addServantLocator(dedicatedLocator, "dedicated");

			adapter.activate();

			System.out.println("Server ready.");
			System.out.println("Shared objects category: shared/<name>");
			System.out.println("Dedicated objects category: dedicated/<name>");

			communicator.waitForShutdown();

		} catch (Exception e) {
			e.printStackTrace(System.err);
			status = 1;
		}
		if (communicator != null) {
			try {
				communicator.destroy();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				status = 1;
			}
		}
		System.exit(status);
	}


	public static void main(String[] args) {
		IceServer app = new IceServer();
		app.t1(args);
	}
}