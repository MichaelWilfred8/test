package floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import Enums.SubsystemType;
import shared.*;

public class FloorHandler{

	private static FloorHandler handlerInstance = null;//singleton instance of this class

	private static final int MAX_FLOORS = 10;//number of floors in building

	private Floor[] floors =  new Floor[MAX_FLOORS];//list of floors within the building

	private static final int DIRECTION_BYTE = 0;//location of direction byte in incoming message

	//private static final SocketAddress FLOOR_PORT_NUMBER = new InetSocketAddress(32);//Floor port number
	//private static final SocketAddress ELEVATOR_PORT_NUMBER = new InetSocketAddress(69);//Elevator port number
	//
	
	private static final SocketAddress FLOOR_PORT_NUMBER = new InetSocketAddress(SocketPort.FLOOR_LISTENER.getValue());//Floor port number
	private static final SocketAddress ELEVATOR_PORT_NUMBER = new InetSocketAddress(SocketPort.ELEVATOR_LISTENER.getValue());//Elevator port number
	private static final SocketAddress SCHEDULER_PORT_NUMBER = new InetSocketAddress(SocketPort.SCHEDULER_LISTENER.getValue());//Scheduler port number
	
	private boolean listening = true;//whether the FloorHandler is listening for incoming messages

	GenericThreadedSender floorSender;		// Sender thread that sends all processed DataPackets to their destination
	GenericThreadedListener floorListener;	// Listener thread that listens for DataPackets and places them in the rawInputBuffer

	BlockingQueue<DataPacket> inputBuffer;
	//BlockingQueue<DatagramPacket> inputBuffer;
	BlockingQueue<DataPacket> outputBuffer;

	private FloorHandler() {//private constructor used because this class follows singleton design pattern

		this.inputBuffer = new ArrayBlockingQueue<DataPacket>(21);
		this.outputBuffer = new ArrayBlockingQueue<DataPacket>(21);

		floorSender = new GenericThreadedSender(outputBuffer, ELEVATOR_PORT_NUMBER, SCHEDULER_PORT_NUMBER, FLOOR_PORT_NUMBER);		
		floorListener = new GenericThreadedListener(inputBuffer, new InetSocketAddress(SocketPort.FLOOR_LISTENER.getValue()).getPort());

		Thread sender = new Thread(floorSender);
		Thread receiver = new Thread(floorListener);



		for (int i=0; i<floors.length; i++) {//create each floor object
			floors[i] = new Floor(MAX_FLOORS, i+1, outputBuffer);//send the floor constructor the max floor and floor number
		}
		
		sender.start();
		receiver.start();
		
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
		System.out.println("Input = " + Arrays.toString(input));
		for (int i=0;i<floors.length;i++) {
			if (floors[i].getFloorNumber() == Integer.parseInt(input[1])) {
				floors[i].newRequest(input);
				
			}
		}
	}

	public void listen() {
		
		while(listening) {
			
			DataPacket input = inputBuffer.poll();

			if (input != null && input.getSubSystem() == SubsystemType.FLOORLAMP) {
				System.out.println("DATAPACKET: " + input.toString() + "\n");
				
				Floor targetFloor = floors[input.getId()-1];
				targetFloor.elevatorArrived(input.getStatus()[DIRECTION_BYTE]);
				
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	public static void main(String args[]){
		FloorHandler fh = FloorHandler.getHandler();

		fh.listen();
	}

}
