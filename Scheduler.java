import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import Enums.*;

/**
 * 
 * Scheduler class. Holds information about the state of 
 * each elevator and processes requests. Verifies that each subsystem 
 * echoes back the correct request	
 * 
 * @author Craig Worthington
 *
 */

// TODO: rewrite completely

public class Scheduler {

	DatagramPacket sendPacket;
	DatagramPacket receivePacket;
	DatagramSocket sendReceiveSocket, receiveSocket;

	private ElevatorStatus carStatus;	// Information about the status of an elevator car

	private static final int MAX_FLOOR = 10;
	private static final int MIN_FLOOR = 1;
	private static final int ARRAY_LEN = 100;
	private static final int DELAY_MILLIS = 250;	// Delay 0.25 of a second

	private Queue<ElevatorInputPacket> requestBuffer;	// Buffer Queue for all requests that have not been handled by the scheduler yet

	private SocketAddress floorHandlerAddress;	// Holds addresses for each floor

	//TODO: create floorStatus class?


	public Scheduler() throws UnknownHostException{//TODO:make it a singleton?
		try {
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

		this.requestBuffer = new ConcurrentLinkedQueue<ElevatorInputPacket>();

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
	 * Schedule requests in the elevator queue. Assign requests to the best elevator. Function not yet complete
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
		System.out.println("Data (String): " + new DataPacket(p.getData()).toString()); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}


	/**
	 * Receive a DataPacket request from another subsystem
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
		DataPacket dp = new DataPacket(receivePacket.getData());
		
		// If the datapacket originated from an elevator
		if (dp.getOrigin() == OriginType.ELEVATOR){
			this.carStatus.update(dp);		// update the carStatus with this information
		}
		return new DataPacket(receivePacket.getData());
	}

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
		// Create Packet to turn off Up Floor Lamp
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.getPosition(), SubsystemType.FLOORLAMP, new byte[] {(byte) this.carStatus.getPosition(), this.carStatus.getTripDir().getByte()});
		// Send packet
		this.sendRequest(p, OriginType.FLOOR, this.carStatus.getPosition());
		
		// Find direction to send elevator in
		// Create DataPacket to request location from elevator
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.LOCATION, new byte[] {0x00});
		
		// send packet
		this.sendRequest(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
		
		// get location from elevator
		p = this.receiveRequest();
		
		// update carStatus with the position of the elevator
		this.carStatus.update(p);
		
		System.out.println("current position = " + this.carStatus.getPosition() + " destination = " + this.carStatus.getNextDestination());
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
				Thread.sleep(DELAY_MILLIS);
				//TimeUnit.SECONDS.wait(0, DELAY_NANOS); // Delay a quarter of a second
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
		
		
		// Create Packet to turn off Up Floor Lamp
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.getPosition(), SubsystemType.FLOORLAMP, new byte[] {(byte) this.carStatus.getPosition(), this.carStatus.getTripDir().getByte()});
		// Send packet
		this.sendRequest(p, OriginType.FLOOR, this.carStatus.getPosition());
		
		// Create packet to tell elevator to Close doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});
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

		// Create Packet to turn off Up Floor Lamp
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.getPosition(), SubsystemType.FLOORLAMP, new byte[] {(byte) this.carStatus.getPosition(), this.carStatus.getTripDir().getByte()});
		// Send packet
		this.sendRequest(p, OriginType.FLOOR, this.carStatus.getPosition());

		// Open doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.DOOR, new byte[] {DoorState.OPEN.getByte()});
		// send packet
		this.sendRequestAndUpdate(p, OriginType.ELEVATOR, (byte) this.carStatus.id);
	}

	
	
	/**
	 * The Sequence of controlling the elevator. Function not yet complete
	 * 
	 * @throws InterruptedException
	 */
	private void elevatorControlLoop() throws InterruptedException{
		// Get incoming request from floor
		this.scheduleElevator();
	
		// Start elevator
		this.startElevator();
		
		// Continue elevator towards destination floor
		this.continueMovingElevator();
		
		// Stop elevator at floor
		this.stopElevator();
	}


	public static void main(String args[]) throws UnknownHostException, InterruptedException{
		Scheduler s = new Scheduler();
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) s.carStatus.id, SubsystemType.FLOORLAMP, new byte[]{(byte) 1, Direction.DOWN.getByte()});

		//DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) this.carStatus.id, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

		//System.out.println(p.toString());
		
		s.sendRequest(p, OriginType.FLOOR, (byte) s.carStatus.id);
		
		p = new DataPacket(OriginType.SCHEDULER, (byte) s.carStatus.id, SubsystemType.FLOORLAMP, new byte[]{(byte) 1, Direction.UP.getByte()});
		
		s.sendRequest(p, OriginType.FLOOR, (byte) s.carStatus.id);
		
//		s.carStatus.addFloor(4);
//		System.out.println("Next destination = " + s.carStatus.getNextDestination());
//		
		//s.startElevator();
		//s.continueMovingElevator();
		//s.stopElevator();
		
		//s.moveUpOneFloor();
	}

}
