import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import Enums.*;

/**
 * @author craig
 *
 */
public class Scheduler {

	DatagramPacket sendPacket;
	static DatagramPacket receivePacket;
	DatagramSocket sendRecieveSocket, receiveSocket;
	FloorHandler floorHandler;

	private ElevatorStatus carStatus;	// Information about the status of an elevator car

	private static final int MAX_FLOOR = 10;
	private static final int MIN_FLOOR = 1;

	private ArrayList<Integer> upRequests;		// ArrayList for holding all requests from an elevator to move from its current position up
	private ArrayList<Integer> downRequests;	// ArrayList for holding all requests from an elevator to move from its current position down

	private Queue<ElevatorInputPacket> requestBuffer;	// Buffer Queue for all requests that have not been handled by the scheduler yet

	private SocketAddress[] listOfFloorAddresses;	// Holds addresses for each floor

	//TODO: create floorStatus class?


	public Scheduler() throws UnknownHostException{//TODO:make it a singleton?
		try {

			floorHandler = new FloorHandler(this);

			//floorHandler.run();
			// Construct a datagram socket and bind it to any available port on the local host machine
			// used to send and receive packets
			sendRecieveSocket = new DatagramSocket();


			// Construct a datagram socket and bind it to port 23 on the local host machine.
			// used to receive packets
			receiveSocket = new DatagramSocket(23);
			receiveSocket.setSoTimeout(10000);	//set intermediate host receive socket to timeout after 10 seconds of no input
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		this.upRequests = new ArrayList<Integer>();
		this.downRequests = new ArrayList<Integer>();
		this.requestBuffer = new ConcurrentLinkedQueue();

		this.carStatus = new ElevatorStatus(MIN_FLOOR, MotorState.OFF, DoorState.CLOSED, MAX_FLOOR, new InetSocketAddress(InetAddress.getLocalHost(), 5000));	// Have an elevator starting on the bottom floor of the building with the door closed and the motor off

		this.listOfFloorAddresses = new SocketAddress[MAX_FLOOR];

		// fill list of floor addresses for the elevator
		for (int i = 0; i < MAX_FLOOR; ++i){
			this.listOfFloorAddresses[i] = new InetSocketAddress(InetAddress.getLocalHost(), 3000+i);
		}


	}

	public void receiveAndForward(){
		while(true){ // Block until a datagram packet is received from receiveSocket.
			// Construct a DatagramPacket for receiving packets up
			// to 100 bytes long (the length of the byte array).
			byte data[] = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Intermediate Host: Waiting for Packet.\n");

			try {
				System.out.println("Waiting..."); // so we know we're waiting
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("Intermediate Host: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			int len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: " );

			//Form a String from the byte array.
			String received = new String(data,0,len);
			System.out.println("(String) " + received);
			System.out.println("(Bytes) " + Arrays.toString(data) + "\n");


			int clientPort = receivePacket.getPort();//stores client port #

			// Slow things down (wait 1 second)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}

			// Create a new datagram packet containing the string received from the client.
			sendPacket = new DatagramPacket(data, receivePacket.getLength(), receivePacket.getAddress(), 69);

			//Print outgoing packet
			System.out.println( "Intermediate Host: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			System.out.println("(String) " + new String(sendPacket.getData(),0,len));
			System.out.println("(Bytes) " + Arrays.toString(sendPacket.getData()) + "\n");

			try {// Send the datagram packet to the client via the send socket.
				sendRecieveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Intermediate Host: packet sent\n");

			// Block until a datagram packet is received from receiveSocket.
			try {
				System.out.println("Waiting..."); // so we know we're waiting
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("Intermediate Host: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: " );

			//Form a String from the byte array.
			received = new String(data,0,len);
			System.out.println("(String)" + received);
			System.out.println("(bytes)" + Arrays.toString(data) + "\n");


			// Create a new datagram packet containing the string received from the server.
			sendPacket = new DatagramPacket(data, receivePacket.getLength(),
					receivePacket.getAddress(), clientPort);

			DatagramSocket sendSocket = null;//instantiate new send socket
			try {
				sendSocket = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			};

			if (sendSocket == null) break;

			//Print outgoing packet
			System.out.println( "Intermediate Host: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			System.out.println("(String)" + new String(sendPacket.getData(),0,len));
			System.out.println("(bytes)" + Arrays.toString(sendPacket.getData()) + "\n");

			// Send the datagram packet to the client via the send socket.
			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		// We're finished, so close the sockets.
		sendRecieveSocket.close();
		receiveSocket.close();

	}


	/*
	 * Data that can be sent to/form elevator:
	 * Motor, Door, CarLamp, Location, request???
	 *
	 * Data that can be sent to/from floor
	 * FloorLamp, request?
	 *
	 */

	/**
	 * Process incoming requests from other subsystems
	 *
	 * @param b 	DataPacket from the incoming request
	 */
	private void parseIncomingRequest(DataPacket p){
		// If the Origin of the message was from the elevator
		if (p.getOrigin() == OriginType.ELEVATOR){
			switch(p.getSubSystem()){
			case MOTOR:
				this.carStatus.setMotorState(MotorState.convertFromByte(p.getStatus()[0]));	// set the motor state in the carStatus class to the motor state sent from the elevator
				break;
			case DOOR:
				this.carStatus.setDoorState(DoorState.convertFromByte(p.getStatus()[0]));	// set the door state in the carStatus class to the motor state sent from the elevator
				break;
			case CARLAMP:
				this.carStatus.toggleFloorButtonLight((int) p.getStatus()[0]);	// Toggle the status of the floor lamp in the elevator
				break;
			case LOCATION:
				this.carStatus.setPosition((int) p.getStatus()[0]);	// Set the location of the elevator to the one sent by the elevator
				break;
			default:
				System.out.println("INVALID CASE");
				break;

			}
		}

		// If the incoming request came from a floor subsystem
		else if (p.getOrigin() == OriginType.FLOOR) {
			switch(p.getSubSystem()){
			case FLOORLAMP:
				// TODO: handle status of floor lamp here
				break;
			case REQUEST:
				// TODO: handle incoming request from floor here. Change add to offer and surround with try catch
				this.requestBuffer.add(new ElevatorInputPacket(p.getStatus())); 	// Generate a new ElevatorInputPacket from the status byte array and add it to the request buffer
				break;
			default:
				System.out.println("INVALID SUBSYSTEM");
				break;
			}
		}
	}

	//TODO: fix this function to give the address of the subsystem with the specified origin and ID
	/**
	 * Get the SocketAddress of the subsystem with given OriginType and id
	 *
	 * @param o		Subsystem type
	 * @param id	id of the specific subsystem
	 * @return		SocketAddress of the subsystem
	 */
	private SocketAddress getAddressOfSubsystem(OriginType o, int id){
		SocketAddress addr = null;

		switch(o){
		case FLOOR:
			addr = this.listOfFloorAddresses[id];
			break;
		case ELEVATOR:
			addr = this.carStatus.getAddress();
			break;
		}
		return addr;
	}

	/**
	 * Schedule requests in the elevator queue. Assign requests to the best elevator
	 */
	private void scheduleElevator(){
		// TODO: Consider not having this as a loop as it may delay the functioning of the rest of the elevator
		while(!this.requestBuffer.isEmpty()){ // Loop until the requestBuffer has no more requests remaining

			// check if the request originated from outside the elevator
			if (this.requestBuffer.peek().getCarButton() == ElevatorInputPacket.REQUEST_FROM_FLOOR){
				// Assign the request to the best elevator
				this.carStatus.addFloor(this.requestBuffer.remove().getFloor()); 	// Assign the floor from the request buffer to the elevator and remove it from the buffer
			} else {	// request originated from within the elevator
				this.carStatus.addFloor(this.requestBuffer.remove().getCarButton()); // Assign the floor from the request buffer to the elevator and remove it from the buffer
			}
		}
	}


	/**
	 * Print what was received from a datagram packet to the console
	 *
	 * @param p		datagram packet that was received
	 * @param mode	String representing if a packet was sent ("sent") or received ("received")
	 */
	private static void printDatagramPacket(DatagramPacket p, String mode){
		if (mode == "sent"){
			System.out.println("Scheduler sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "received") {
			System.out.println("Scheduler received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		String data = new String(p.getData(), 0, p.getLength());				// Create new string from data in DatagramPacket
		System.out.println("Data (String): " + data); 							// Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}


	/**
	 * Receive a request from another subsystem
	 *
	 * @return 	A
	 */
	private DataPacket receiveRequest(){
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Scheduler: Waiting for Packet.\n");

		// Wait to receive a DatagramPacket
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		printDatagramPacket(receivePacket, "received");

		return new DataPacket(receivePacket.getData());
	}


	/**
	 * Send a request from the scheduler to the appropriate subsystem
	 *
	 * @param p					DataPacket containing the information to be sent
	 * @param destinationType	OriginType of the destination
	 * @param id				ID of the destination
	 */
	private void sendRequest(DataPacket p, OriginType destinationType, int id){

		byte data[] = p.getBytes();

		// Create a new datagram packet containing the string received from the server.
		sendPacket = new DatagramPacket(data, data.length, this.getAddressOfSubsystem(destinationType, id));

		DatagramSocket sendSocket = null;//instantiate new send socket
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		};

		if (sendSocket == null) return;

		// Try to send the DatagramPacket from the scheduler to its destination via the send socket
		try {
			sendRecieveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Scheduler: packet sent\n");

	}



	private void elevatorControlLoop(){
		while(true){
			// Wait to receive packet and parse it
			this.parseIncomingRequest(receiveRequest());

			// Handle the incoming request

			// if request is a floor number

		}
	}

	/**
	 * @return Top Level of building
	 */
	public int getTopFloor() {
		return MAX_FLOOR;
	}

	/**
	 * @return floorHandler
	 */
	public FloorHandler getFloorHandler() {
		return floorHandler;
	}

	/**
	 * @return socket for scheduler
	 */
	public int getSchedulerSocket() {
		return receiveSocket.getPort();
	}

}



