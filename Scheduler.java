import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import Enums.*;

/**
 * @author craig
 *
 */
public class Scheduler {

	DatagramPacket sendPacket;
	static DatagramPacket receivePacket;
	DatagramSocket sendReceiveSocket, receiveSocket;

	private ElevatorStatus carStatus;	// Information about the status of an elevator car

	private static final int MAX_FLOOR = 10;
	private static final int MIN_FLOOR = 1;
	private static final int ARRAY_LEN = 100;
	private static final int DELAY_NANOS = 250000000;	// Delay 0.25 of a second

	private ArrayList<Integer> upRequests;		// ArrayList for holding all requests from an elevator to move from its current position up
	private ArrayList<Integer> downRequests;	// ArrayList for holding all requests from an elevator to move from its current position down

	private Queue<ElevatorInputPacket> requestBuffer;	// Buffer Queue for all requests that have not been handled by the scheduler yet

	private SocketAddress floorHandlerAddress;	// Holds addresses for each floor

	//TODO: create floorStatus class?


	public Scheduler() throws UnknownHostException{//TODO:make it a singleton?
		try {
			// floorHandler.run();
			// Construct a datagram socket and bind it to any available port on the local host machine
			// used to send and receive packets as echos
			sendReceiveSocket = new DatagramSocket(2300);


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

		this.carStatus = new ElevatorStatus(MIN_FLOOR, MotorState.OFF, DoorState.CLOSED, MAX_FLOOR, new InetSocketAddress(InetAddress.getLocalHost(), 69), 1);	// Have an elevator starting on the bottom floor of the building with the door closed and the motor off

		this.floorHandlerAddress = new InetSocketAddress(InetAddress.getLocalHost(), 32);
	}



	/**
	 * @return Top Level of building
	 */
	public int getTopFloor() {
		return MAX_FLOOR;
	}

	/**
	 * @return socket for scheduler
	 */
	public int getSchedulerSocket() {
		return receiveSocket.getPort();
	}


	/**
	 * Determine the next state the motor must be in
	 *
	 * @return	MotorState the state that the motor has to be in
	 */
	private MotorState findNextMotorState(){
		if (this.carStatus.getNextDestination() == this.carStatus.getPosition()){
			return MotorState.OFF;
		} else if (this.carStatus.getNextDestination() > this.carStatus.getPosition()){
			return MotorState.UP;
		} else if (this.carStatus.getNextDestination() < this.carStatus.getPosition()){
			return MotorState.OFF;
		} else {
			return MotorState.OFF;
		}
	}

	/*
	 * Data that can be sent to/form elevator:
	 * Motor, Door, CarLamp, Location, request???
	 *
	 * Data that can be sent to/from floor
	 * FloorLamp, request?
	 *
	 */
	
	
	// TODO: determine if this method can be removed
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
			addr = this.floorHandlerAddress;
			break;
		case ELEVATOR:
			addr = this.carStatus.getAddress();
			break;
		default:
			System.out.println("Invalid Origin Type");
			System.exit(0);
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
	 * @param mode	String representing if a packet was sent ("s") or received ("r")
	 */
	private static void printDatagramPacket(DatagramPacket p, String mode){
		if (mode == "s"){
			System.out.println("Scheduler sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "r") {
			System.out.println("Scheduler received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		String data = new String(p.getData(), 0, p.getLength());				// Create new string from data in DatagramPacket
		System.out.println("Data (String): " + new DataPacket(p.getData()).toString()); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}


	/**
	 * Receive a request from another subsystem
	 *
	 * @return 	A
	 */
	private DataPacket receiveRequest(){
		byte data[] = new byte[ARRAY_LEN];
		this.receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Scheduler: Waiting for Packet.\n");

		// Wait to receive a DatagramPacket
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			this.sendReceiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("sendReceive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		printDatagramPacket(receivePacket, "r");

		return new DataPacket(receivePacket.getData());
	}


	/**
	 * Receive a request from another subsystem and add it to the queue of requests
	 *
	 * 
	 */
	//	private void receiveRequest(){
	//		byte data[] = new byte[ARRAY_LEN];
	//		receivePacket = new DatagramPacket(data, data.length);
	//
	//		System.out.println("Scheduler: Waiting for Packet.\n");
	//
	//		// Wait to receive a DatagramPacket
	//		try {
	//			System.out.println("Waiting..."); // so we know we're waiting
	//			receiveSocket.receive(receivePacket);
	//		} catch (IOException e) {
	//			System.out.print("IO Exception: likely:");
	//			System.out.println("Receive Socket Timed Out.\n" + e);
	//			e.printStackTrace();
	//			System.exit(1);
	//		}
	//
	//		printDatagramPacket(receivePacket, "received");
	//		
	//		DataPacket p = new DataPacket(receivePacket.getData());
	//		
	//		if (p.getSubSystem() == SubsystemType.REQUEST){
	//			this.requestBuffer.add(new ElevatorInputPacket(p.getStatus()));
	//		}
	//		
	//		return new DataPacket(receivePacket.getData());
	//	}

	/**
	 * Send a request through the sendReceiveSocket from the scheduler to the appropriate subsystem
	 *
	 * @param p					DataPacket containing the information to be sent
	 * @param destinationType	OriginType of the destination
	 * @param id				ID of the destination
	 */
	private void sendRequest(DataPacket p, OriginType destinationType, int id){

		byte data[] = p.getBytes();
		//System.out.println("byte array = " + Arrays.toString(p.getBytes()));



		// Create a new datagram packet containing the string received from the server.
		sendPacket = new DatagramPacket(data, data.length, this.getAddressOfSubsystem(destinationType, id));

		printDatagramPacket(sendPacket, "s");

		// Try to send the DatagramPacket from the scheduler to its destination via the sendReceive socket
		try {
			this.sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Scheduler: packet sent\n");

	}
	
	
	/**
	 * Start elevator moving from the floor. Ends when elevator is moving towards next destination floor
	 */
	private void startElevator(){
		// Check if elevator has the next destination. returns if no new floor to visit
		if (this.carStatus.getNextDestination() == this.carStatus.getPosition()){
			return;
		}
		
		// Create packet to tell elevator to Close doors
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		
		// Turn off lamp
		// TODO: add function to turn on lamp
		
		// Find direction to send elevator in
		// Create DataPacket to request location from elevator
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.LOCATION, new byte[] {0x00});
		
		// send packet
		this.sendRequest(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		
		// get location from elevator
		p = this.receiveRequest();
		
		// update carStatus with the position of the elevator
		this.carStatus.update(p);
		
		
		// If the next destination is below this current floor, move elevator down
		if (this.carStatus.getNextDestination() < this.carStatus.getPosition()){	// Turn on motor downwards
			// Create packet to tell elevator to Turn on motor down
			p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.DOWN.getByte()});
			// send packet
			this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		} else if (this.carStatus.getNextDestination() > this.carStatus.getPosition()){	// Turn on motor upwards
			// Create packet to tell elevator to Turn on motor up
			p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});
			// send packet
			this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		}
	}
	
	
	
	/**
	 * Stop elevator at current position. Turn on floor lamp and open the doors.
	 */
	private void stopElevator(){
		// Create packet to tell elevator to Stop Motor
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.OFF.getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		
		
		// Create packet to tell floor to turn on direction lamp
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.getPosition(), SubsystemType.FLOORLAMP, new byte[] {this.carStatus.getTripDir().getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.getPosition());
		
		
		// Create packet to tell elevator to open doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.OPEN.getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
	}
	
	
	/**
	 * Algorithm for moving the elevator towards its next destination. Exits method once elevator arrives at correct floor
	 * @throws InterruptedException 
	 */
	private void continueMovingElevator() throws InterruptedException{
		// Create DataPacket to request location from elevator
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.LOCATION, new byte[] {0x00});
		
		// send packet
		this.sendRequest(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		
		// get location from elevator
		p = this.receiveRequest();
		
		// update carStatus with the position of the elevator
		this.carStatus.update(p);
		
		// if elevator is not at its destination floor
		if (this.carStatus.getPosition() != this.carStatus.getNextDestination()){
			// Send a location request every quarter second until elevator gets to correct location
			while(this.carStatus.getPosition() != this.carStatus.getNextDestination()){
				TimeUnit.SECONDS.wait(0, DELAY_NANOS); // Delay a quarter of a second
				// Create a DataPacket to request location from elevator
				p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.LOCATION, new byte[] {0x00});
				// Send request to the elevator
				this.sendRequest(p, OriginType.ELEVATOR, this.carStatus.id);
				// Receive request return
				p = this.receiveRequest();
				// Update carStatus with new position of elevator
				this.carStatus.update(p);
			}
		}
		
		return;
	}

	/**
	 * Send a request to the given destination subsystem and ID
	 *
	 * @param sendPacket		DataPacket to be sent to the subsystem
	 * @param destinationType	Subsystem to send the packet to
	 * @param id				Identifier of the specific element of the subsystem
	 * @return					Returns true if the packet that was received is an echo of the packet it sent
	 */
	private void requestEchoed(DataPacket sendPacket, OriginType destinationType, int id) throws IOException {

		this.sendRequest(sendPacket, destinationType, id);

		System.out.println("waiting to get echo of request");
		DataPacket receivePacket = receiveRequest();

		//System.out.println("send packet status = " + Arrays.toString(sendPacket.getBytes()));
		if (receivePacket.equals(destinationType, id, sendPacket.getSubSystem(), sendPacket.getStatus())){
			return;
		} else {
			throw new IOException("Request was not as expected");
		}
	}


	/**
	 * Algorithm for stopping the elevator if it is at the next destination floor
	 */
	private void stopAtFloor(){

		// Tell elevator to stop.
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.OFF.getByte()});

		try{
			this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message


		// TODO: Tell floor to trigger direction lamp


		// Tell elevator to open doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.OPEN.getByte()});

		try{
			this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message


		this.receiveRequest(); // Try to receive a request from the floor

		// Tell elevator to close doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});

		try{
			this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message


		// Check if the elevator has a new destination
		if (this.carStatus.getNextDestination() != this.carStatus.getPosition()){
			// Tell elevator to close doors
			p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});

			try{
				this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}

			this.carStatus.update(p); // Update the ElevatorStatus with the message

			if (this.carStatus.getTripDir() == Direction.UP){
				p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

				try{
					this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}

				this.carStatus.update(p); // Update the ElevatorStatus with the message

			} else {
				p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.DOWN.getByte()});

				try{
					this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}

				this.carStatus.update(p); // Update the ElevatorStatus with the message

			}
		}
	}

	/**
	 * Algorithm for moving the elevator towards its next destination
	 */
	private void continueOneFloor(){
		if(this.carStatus.getNextDestination() > this.carStatus.getPosition()){
			DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

			try{
				this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}

			this.carStatus.update(p); // Update the ElevatorStatus with the message

		} else if (this.carStatus.getNextDestination() < this.carStatus.getPosition()){
			DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.DOWN.getByte()});

			try{
				this.requestEchoed(p, OriginType.ELEVATOR, this.carStatus.id);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}

			this.carStatus.update(p); // Update the ElevatorStatus with the message
		} else if (this.carStatus.getNextDestination() == this.carStatus.getPosition()){
			this.stopAtFloor();
		}
	}


	/**
	 * Send a request to the elevator and check to see if it has been echoed back. Update the carStatus once the echo has been received
	 * @param p				DataPacket to be sent
	 * @param destination	Destination type of the packet	
	 * @param id			ID of the destination for the packet
	 */
	private void sendRequestAndUpdate(DataPacket p, OriginType destination, int id){
		try{
			this.requestEchoed(p, destination, id);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message
	}


	/**
	 * Move the elevator up one floor
	 * @throws InterruptedException 
	 */
	private void moveUpOneFloor() throws InterruptedException{
		// Set destination floor
		this.carStatus.addFloor(2);
		
		// Create packet to tell elevator to Close doors
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);

		// Create packet to tell elevator to Turn on motor up
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);


		// Wait to see if elevator is at destination floor
		// Request location from elevator
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.LOCATION, new byte[] {0x00});

		// send packet
		this.sendRequest(p, OriginType.ELEVATOR, (byte) this.carStatus.id);

		// get location from elevator
		p = this.receiveRequest();

		// if elevator is still on the same floor
		if ((p.getSubSystem() == SubsystemType.LOCATION) && (p.getStatus()[0] == (byte) this.carStatus.getPosition())){
			// Send a request every second until elevator gets to correct location
			while((p.getSubSystem() == SubsystemType.LOCATION) && ((int) p.getStatus()[0] != this.carStatus.getNextDestination())){
				// Request location from elevator
				p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.LOCATION, new byte[] {0x00});

				p = this.receiveRequest();

				TimeUnit.SECONDS.wait(1);
			}
		}


		// Turn off motor up
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.OFF.getByte()});

		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);

		//Turn on the lamp
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.FLOORLAMP, new byte[]{(byte) 4, Direction.UP.getByte()});

		// send packet
		this.sendRequestAndUpdate(p, OriginType.FLOOR, (byte) this.carStatus.getPosition());

		// Open doors

		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.OPEN.getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
	}


	private void elevatorControlLoop() throws InterruptedException{
			// Get incoming request from floor
			
			// Start elevator
			this.startElevator();
			
			// Continue elevator towards destination floor
			this.continueMovingElevator();
			
			// Stop elevator at floor
			this.stopElevator();
	}


	public static void main(String args[]) throws UnknownHostException, InterruptedException{
		Scheduler s = new Scheduler();
		//DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.FLOORLAMP, new byte[]{(byte) 4, Direction.UP.getByte()});

		//DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

		//System.out.println(p.toString());
		//s.sendRequest(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		
		s.carStatus.addFloor(4);
		
		s.startElevator();
		s.continueMovingElevator();
		s.stopElevator();
	}

}
