package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import shared.*;

public class ElevatorHandler implements Runnable {


	DatagramPacket receivePacket;
	DatagramSocket receiveSocket;

	//private static List<Elevator> elevatorList = new ArrayList<Elevator>(3);
	private List<Elevator> elevatorList;
	private List<Thread> elevatorThreadList;
	private boolean stopController;

	//private static final ElevatorHandler instance = new ElevatorHandler();
	
	private int NUM_ELEVATORS = 1;		// Number of elevator cars in the building
	private int MAX_FLOOR = 22;			// Number of floors in the building
	
	private boolean printDebug = true;
	
	public ElevatorHandler(){
		
//		if(instance != null){
//			throw new IllegalStateException("Already instantiated");
//		}
		
		setStopController(false);


		// Construct a datagram socket and bind it to port 69 
		// on the local host machine. This socket will be used to
		// receive UDP Datagram packets. 
		try {
			receiveSocket = new DatagramSocket(SocketPort.ELEVATOR_LISTENER.getValue());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// to test socket timeout (2 seconds)
		//receiveSocket.setSoTimeout(2000);
		
		// Initialize each elevator
		this.elevatorList = new ArrayList<Elevator>(NUM_ELEVATORS);
		this.elevatorThreadList = new ArrayList<Thread>(NUM_ELEVATORS);
		
		for(int i = 0; i < NUM_ELEVATORS; i++){
			Elevator elevator = new Elevator(i, MAX_FLOOR, this.printDebug);
			Thread t = new Thread(elevator);
			//t.start();

			elevatorList.add(elevator);
			elevatorThreadList.add(t);
			
			try {
				elevatorList.get(i).setSchedulerAddress(new InetSocketAddress(InetAddress.getLocalHost(), SocketPort.SCHEDULER_LISTENER.getValue()));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	
	public ElevatorHandler(int numFloors, int numCars, boolean printDebug){
		
		setStopController(false);
		
		this.MAX_FLOOR = numFloors;
		this.NUM_ELEVATORS = numCars;

		// Construct a datagram socket and bind it to port 69 
		// on the local host machine. This socket will be used to
		// receive UDP Datagram packets. 
		try {
			receiveSocket = new DatagramSocket(SocketPort.ELEVATOR_LISTENER.getValue());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Initialize the debug
		this.printDebug = printDebug;
		
		
		// Initialize each elevator
		this.elevatorList = new ArrayList<Elevator>(NUM_ELEVATORS);
		this.elevatorThreadList = new ArrayList<Thread>(NUM_ELEVATORS);
		
		for(int i = 0; i < NUM_ELEVATORS; i++){
			Elevator elevator = new Elevator(i, MAX_FLOOR, this.printDebug);
			Thread t = new Thread(elevator);

			elevatorList.add(elevator);
			elevatorThreadList.add(t);
			
			try {
				elevatorList.get(i).setSchedulerAddress(new InetSocketAddress(InetAddress.getLocalHost(), SocketPort.SCHEDULER_LISTENER.getValue()));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
//	// static method to create instance of class 
//	public static ElevatorHandler getHandler(){ 
//		return instance; 
//	}
	
	
	/**
	 * Print a received datapacket to the console
	 * @param receivePacket
	 */
	private static void printDatagramPacket(DatagramPacket receivePacket) {
		DataPacket tempPacket = new DataPacket(receivePacket.getData());
		
		System.out.println("ElevatorHandler: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		System.out.println("Length: " + receivePacket.getLength());
		System.out.println("Containing: ");
		// Form a String from the byte array.
		System.out.println("(String) " + tempPacket.toString());
		System.out.println("(Bytes) " + Arrays.toString(receivePacket.getData()));
	}

	
	/**
	 * listen for incoming requests, listens on port 69
	 */
	private void listen(){
		boolean notDone = true;
		
		// Start all elevator threads
		for(int i = 0; i < NUM_ELEVATORS; ++i) {
			this.elevatorThreadList.get(i).start();
		}

		
		while(notDone) {
			// Construct a DatagramPacket for receiving packets up 
			// to 100 bytes long (the length of the byte array).
			byte data[] = new byte[5];
			receivePacket = new DatagramPacket(data, data.length);

			try {
				// Block until a datagram is received via sendReceiveSocket.
				if (this.printDebug) {
					System.out.println("ElevatorHandler: Waiting for Packet.\n");
				}
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n");
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			if (printDebug) {
				printDatagramPacket(receivePacket);
			}
			
			
			if(data[0]==-1) {
				notDone = false;
			} else {
				Elevator elevator = null;
				DataPacket dp = new DataPacket(data);
				int id = dp.getId();
				elevator = findElevator(id);
				System.out.println("\nCalling elevator " + id);
				
				// Add message to the queue
				//elevator.inputBuffer.add(dp);
				elevator.inputBuffer.add(receivePacket);
			}
		}
	}
	
	
	public synchronized Elevator selectElevator(int id) {
		Elevator elevator = null;

		elevator = findElevator(id);
		// So that elevators can start moving again.
		notifyAll();
		return elevator;
	}

	
	private Elevator findElevator(int id) {
		return elevatorList.get(id);
	}

	public void setStopController(boolean stop){
		this.stopController = stop;

	}
	
	
	@Override
	public void run() {
		listen();
		
	}
	
	
	public static void main(String args[]) throws InterruptedException {
		
		final int NUM_FLOORS = 22;
		final int NUM_ELEVATORS = 1;
		ElevatorHandler eh = new ElevatorHandler(NUM_FLOORS, NUM_ELEVATORS, false);
		
		eh.listen();
	}

}
