import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import Enums.SubsystemType;

public class FloorHandler {

	private static FloorHandler handlerInstance = null;//singleton instance of this class

	private static final int MAX_FLOORS = 10;//number of floors in building

	private Floor[] floors =  new Floor[MAX_FLOORS];//list of floors within the building

	private DatagramPacket receivePacket;//packets and socket used to receive information
	private DatagramSocket receiveSocket;

	private static final int DIRECTION_BYTE = 1;//location of direction byte in incoming message
	private static final int FLOOR_NUM_BYTE = 0;//location of floor number bytes in incoming message

	private boolean listening = true;//whether the FloorHandler is listening for incoming messages

	private FloorHandler() {//private constructor used because this class follows singleton design pattern

		for (int i=0; i<floors.length; i++) {//create each floor object
			floors[i] = new Floor(MAX_FLOORS, i+1);//send the floor constructor the max floor and floor number
		}

		try {//create the receive socket on port 32
			receiveSocket = new DatagramSocket(32);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return the only instance of the FloorHandler, creating one if none exist
	 */
	public static FloorHandler getHandler(){ 
		if (handlerInstance == null) 
			handlerInstance = new FloorHandler(); 
		return handlerInstance; 
	}

	/**
	 * @return All Floors
	 */
	public Floor[] getFloors() {
		return floors;
	}

	public void createRequest(String[] input) {
		for (int i=0;i<floors.length;i++) {
			if (floors[i].getFloorNumber() == Integer.parseInt(input[1])) {
				floors[i].newRequest(input);
			}
		}
	}

	private void listen() {
		while(listening) {
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
			//System.out.println("(Bytes) " + Arrays.toString(data));

			DataPacket input = new DataPacket(data);

			System.out.println("DATAPACKET: " + input.toString() + "\n");

			if (input.getSubSystem() == SubsystemType.FLOORLAMP) {
				Floor targetFloor = floors[input.getStatus()[FLOOR_NUM_BYTE]];
				System.out.println("FLOOR " + targetFloor.getFloorNumber() + " is toggling its " + targetFloor.getFloorLamps()[input.getStatus()[DIRECTION_BYTE]-1].getDirection() + " lamp.");
				targetFloor.getFloorLamps()[input.getStatus()[DIRECTION_BYTE]-1].toggle();
				System.out.println("FLOOR " + targetFloor.getFloorNumber() + "'s " + targetFloor.getFloorLamps()[input.getStatus()[DIRECTION_BYTE]-1].getDirection() + " lamp is now " + targetFloor.getFloorLamps()[input.getStatus()[DIRECTION_BYTE]-1].getStateString());

			}


		}
	}

	public static void main(String args[]){
		FloorHandler fh = FloorHandler.getHandler();

		fh.listen();
	}

}
