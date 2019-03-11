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
import java.util.concurrent.LinkedBlockingQueue;

import Enums.DoorState;
import Enums.MotorState;
import Enums.OriginType;
import Enums.SubsystemType;
import scheduler.SchedulerHandler;
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
			Elevator elevator = new Elevator(i, MAX_FLOOR);
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

	
	public ElevatorHandler(int numFloors, int numCars){
		
//		if(instance != null){
//			throw new IllegalStateException("Already instantiated");
//		}
		
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

		// to test socket timeout (2 seconds)
		//receiveSocket.setSoTimeout(2000);
		
		// Initialize each elevator
		this.elevatorList = new ArrayList<Elevator>(NUM_ELEVATORS);
		this.elevatorThreadList = new ArrayList<Thread>(NUM_ELEVATORS);
		
		for(int i = 0; i < NUM_ELEVATORS; i++){
			Elevator elevator = new Elevator(i, MAX_FLOOR);
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
				System.out.println("ElevatorHandler: Waiting for Packet.\n");
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n");
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			printDatagramPacket(receivePacket);
			
			
			if(data[0]==-1) {
				notDone = false;
			} else {
				Elevator elevator = null;
				DataPacket dp = new DataPacket(data);
				int id = dp.getId();
				elevator = findElevator(id);
				System.out.println("calling elevator " + id + "\n");
				
				// Add message to the queue
				//elevator.inputBuffer.add(dp);
				elevator.inputBuffer.add(receivePacket);
				
				/*
				try {
					elevator.receiveAndEcho(dp, receivePacket);
				} catch (ClassNotFoundException | IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
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

//	private static void initializeElevators(){
//		for(int i = 0; i < NUM_ELEVATORS; i++){
//			Elevator elevator = new Elevator(i, MAX_FLOOR);
//			Thread t = new Thread(elevator);
//			t.start();
//
//			elevatorList.add(elevator);
//			try {
//				elevatorList.get(i).setSchedulerAddress(new InetSocketAddress(InetAddress.getLocalHost(), SocketPort.SCHEDULER_LISTENER.getValue()));
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	/**
	 * Right now, the findElevator is only based on id
	 * TODO in the future, findElevator will take (movingDirection, DestinationFloor, RequestFloor) as param
	 * 		so we can use hash map
	 * @param id
	 * @return selected elevator
	 */
//	private static Elevator findElevator(int id) {
//		Elevator elevator = null;
//		elevator =  elevatorList.get(id);
//		return elevator;
//	}
	
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
	
//	public static void main(String args[]){
//		instance.run();
//	}
	
	public static void main(String args[]) throws InterruptedException {
		
		final int NUM_FLOORS = 22;
		final int NUM_ELEVATORS = 1;
		ElevatorHandler eh = new ElevatorHandler(NUM_FLOORS, NUM_ELEVATORS);
		
		eh.listen();
		
		
//		LinkedBlockingQueue<DataPacket> inQueue = new LinkedBlockingQueue<DataPacket>();
//		LinkedBlockingQueue<DataPacket> outQueue = new LinkedBlockingQueue<DataPacket>();
//		DataPacket sendPacket = new DataPacket(OriginType.SCHEDULER, (byte) 0, null, null);
//		DataPacket getPacket = new DataPacket(OriginType.SCHEDULER, (byte) 0, null, null);
//		
//		ElevatorHandler eh = new ElevatorHandler();
//		Thread ehThread = new Thread(eh);
//		
//		GenericThreadedSender sender = new GenericThreadedSender(inQueue, SchedulerHandler.ELEVATOR_PORT_NUMBER, SchedulerHandler.SCHEDULER_PORT_NUMBER, SchedulerHandler.FLOOR_PORT_NUMBER, false);
//		Thread senderThread = new Thread(sender);
//		
//		GenericThreadedListener listener = new GenericThreadedListener(outQueue, SocketPort.SCHEDULER_LISTENER.getValue(), false);
//		Thread listenerThread = new Thread(listener);
//		
//		ehThread.start();
//		senderThread.start();
//		listenerThread.start();
//		
//		
//		
//		// Tell elevator to open doors
//		sendPacket.setSubSystem(SubsystemType.DOOR);
//		sendPacket.setStatus(new byte[] {DoorState.OPEN.getByte()});
//		
//		inQueue.add(sendPacket);
//		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("output from elevator = " + outQueue.toString());
//		try {
//			getPacket = outQueue.take();
//		} catch (InterruptedException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		
//		
//		// Tell elevator to close doors
//		sendPacket.setSubSystem(SubsystemType.DOOR);
//		sendPacket.setStatus(new byte[] {DoorState.CLOSED.getByte()});
//		
//		inQueue.add(sendPacket);
//		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("output from elevator = " + outQueue.toString());
//		try {
//			getPacket = outQueue.take();
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		
//		// Tell elevator to move upwards
//		sendPacket.setSubSystem(SubsystemType.MOTOR);
//		sendPacket.setStatus(new byte[] {MotorState.UP.getByte()});
//		
//		inQueue.add(sendPacket);
//		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("output from elevator = " + outQueue.toString());
//		getPacket = outQueue.take();
//		
//		while(true){
//			try {
//				getPacket = outQueue.take();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if ((getPacket.getSubSystem() == SubsystemType.LOCATION) && (getPacket.getStatus()[0] == (byte) 4)){
//				// Tell elevator to stop
//				sendPacket.setSubSystem(SubsystemType.MOTOR);
//				sendPacket.setStatus(new byte[] {MotorState.OFF.getByte()});
//				
//				inQueue.add(sendPacket);
//				break;
//			}
//		}
//		
//		Thread.sleep(3000);
//		
//		// Tell elevator to move upwards
//		sendPacket.setSubSystem(SubsystemType.MOTOR);
//		sendPacket.setStatus(new byte[] {MotorState.DOWN.getByte()});
//		
//		inQueue.add(sendPacket);
//		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("output from elevator = " + outQueue.toString());
//		getPacket = outQueue.take();
//		
//		while(true){
//			try {
//				getPacket = outQueue.take();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if ((getPacket.getSubSystem() == SubsystemType.LOCATION) && (getPacket.getStatus()[0] == (byte) 2)){
//				// Tell elevator to stop
//				sendPacket.setSubSystem(SubsystemType.MOTOR);
//				sendPacket.setStatus(new byte[] {MotorState.OFF.getByte()});
//				
//				inQueue.add(sendPacket);
//				break;
//			}
//		}
		
	}

}
