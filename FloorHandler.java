import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class FloorHandler implements Runnable {

	Floor[] floors;
	Scheduler scheduler;

	DatagramPacket receivePacket; //packets and socket used to send information
	DatagramSocket receiveSocket;

	FloorHandler(Scheduler scheduler){
		this.scheduler = scheduler;
		floors = new Floor[scheduler.getTopFloor()];
		for(int i=0;i<scheduler.getTopFloor();i++) {
			floors[i] = new Floor(scheduler, i+1);
		}

		try {
			receiveSocket = new DatagramSocket(32);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return All Floors
	 */
	public Floor[] getFloors() {
		return floors;
	}

	/**
	 * listen for incoming requests, listens on port 32
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
				System.out.println("FloorHandler: Waiting for Packet.\n");
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n");
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("FloorHandler: Packet received:");
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
				for (int i = 0; i<floors.length;i++) {
					if(floors[i].getFloorNumber() == data[15]) {
						floors[i].elevatorArrived(data[19]);
					}
				}
			}
		}

	}

	/**
	 * Thread run class
	 */
	public void run() {
		listen();

	}

}