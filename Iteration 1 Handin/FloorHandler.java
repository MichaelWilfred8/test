import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class FloorHandler implements Runnable {
	
	private static FloorHandler instance = null;

	Floor[] floors;
	Scheduler scheduler;

	DatagramPacket receivePacket; //packets and socket used to send information
	DatagramSocket receiveSocket;
	
	private static final int DIRECTION_BYTE = 4;
	private static final int FLOOR_NUM_BYTE = 3;
	
	private FloorHandler(int numFloors){
		floors = new Floor[numFloors];
		for(int i=0;i<numFloors;i++) {
			//floors[i] = new Floor(scheduler, i+1);
			floors[i] = new Floor(numFloors, i+1);
		}

		try {
			receiveSocket = new DatagramSocket(32);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	// static method to create instance of class 
    public static FloorHandler getHandler(int numFloors){ 
        if (instance == null) 
            instance = new FloorHandler(numFloors); 
        return instance; 
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
		//Test t = new Test();
		//t.runTest();
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
					if(floors[i].getFloorNumber() == data[FLOOR_NUM_BYTE]) {
						floors[i].elevatorArrived(data[DIRECTION_BYTE]);
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
	
	public static void main(String args[]){
		FloorHandler fh = FloorHandler.getHandler(10);
		
		fh.run();
	}

}