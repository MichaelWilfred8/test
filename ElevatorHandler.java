import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class ElevatorHandler implements Runnable{
	private static ElevatorHandler instance = null;

	DatagramPacket receivePacket;
	DatagramSocket receiveSocket;

	private Elevator [] elevatorList;

	private ElevatorHandler(int numElevators) {
		elevatorList = new Elevator[numElevators];
		for (int i=0;i<numElevators;i++) {
			elevatorList[i] = new Elevator(10);
		}

		// Construct a datagram socket and bind it to port 5000 
		// on the local host machine. This socket will be used to
		// receive UDP Datagram packets.
		try {
			receiveSocket = new DatagramSocket(69);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// to test socket timeout (2 seconds)
		//receiveSocket.setSoTimeout(2000);
	}

	// static method to create instance of class 
	public static ElevatorHandler getHandler(int numFloors){ 
		if (instance == null) 
			instance = new ElevatorHandler(numFloors); 
		return instance; 
	}

	/**
	 * listen for incoming requests, listens on port 69
	 */
	private void listen(){

		boolean notDone = true;
		while(notDone) {
			// Construct a DatagramPacket for receiving packets up 
			// to 100 bytes long (the length of the byte array).
			byte data[] = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);

			try {
				// Block until a datagram is received via sendReceiveSocket.
				System.out.println("ElevatorHandler: Waiting for Packet.\n");
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n");
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("ElevatorHandler: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			int len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			// Form a String from the byte array.
			System.out.println("(Bytes) " + Arrays.toString(data) + "\n");

			if(data[0]==-1) {
				notDone = false;
			} else {
				DataPacket dp = new DataPacket(data);
				
				for (int i = 0; i<elevatorList.length;i++) {
					if(elevatorList[i].getId() == dp.getId()) {
						try {
							elevatorList[i].receiveAndEcho(dp, receivePacket);//give the data to the elevator
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	/**
	 * Thread run class
	 */
	@Override
	public void run() {
		listen();

	}



}
