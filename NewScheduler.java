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

	private ElevatorStatus carStatus;	// Information about the status of an elevator car

	private static final int MAX_FLOOR = 10;
	private static final int MIN_FLOOR = 1;

	private ArrayList<Integer> upRequests;		// ArrayList for holding all requests from an elevator to move from its current position up
	private ArrayList<Integer> downRequests;	// ArrayList for holding all requests from an elevator to move from its current position down

	private Queue<ElevatorInputPacket> requestBuffer;	// Buffer Queue for all requests that have not been handled by the scheduler yet

	private SocketAddress floorHandlerAddress;	// Holds addresses for each floor

	//TODO: create floorStatus class?


	public Scheduler() throws UnknownHostException{//TODO:make it a singleton?
		try {

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

		this.floorHandlerAddress = new InetSocketAddress(InetAddress.getLocalHost(), 3000);
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

		DataPacket receivePacket = receiveRequest();

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
		DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.MOTOR, new byte[] {MotorState.OFF.getByte()});

		try{
			this.requestEchoed(p, OriginType.ELEVATOR, 1);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message

		// Tell elevator to open doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.DOOR, new byte[] {DoorState.OPEN.getByte()});

		try{
			this.requestEchoed(p, OriginType.ELEVATOR, 1);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message


		this.receiveRequest(); // Try to receive a request from the floor

		// Tell elevator to close doors
		p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});

		try{
			this.requestEchoed(p, OriginType.ELEVATOR, 1);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}

		this.carStatus.update(p); // Update the ElevatorStatus with the message


		// Check if the elevator has a new destination
		if (this.carStatus.getNextDestination() != this.carStatus.getPosition()){
			// Tell elevator to close doors
			p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.DOOR, new byte[] {DoorState.CLOSED.getByte()});

			try{
				this.requestEchoed(p, OriginType.ELEVATOR, 1);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}

			this.carStatus.update(p); // Update the ElevatorStatus with the message

			if (this.carStatus.getTripDir() == Direction.UP){
				p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

				try{
					this.requestEchoed(p, OriginType.ELEVATOR, 1);
				} catch (IOException e){
					e.printStackTrace();
					System.exit(1);
				}

				this.carStatus.update(p); // Update the ElevatorStatus with the message

			} else {
				p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.MOTOR, new byte[] {MotorState.DOWN.getByte()});

				try{
					this.requestEchoed(p, OriginType.ELEVATOR, 1);
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
			DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.MOTOR, new byte[] {MotorState.UP.getByte()});

			try{
				this.requestEchoed(p, OriginType.ELEVATOR, 1);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}

			this.carStatus.update(p); // Update the ElevatorStatus with the message

		} else if (this.carStatus.getNextDestination() < this.carStatus.getPosition()){
			DataPacket p = new DataPacket(OriginType.SCHEDULER, (byte) 0, SubsystemType.MOTOR, new byte[] {MotorState.DOWN.getByte()});

			try{
				this.requestEchoed(p, OriginType.ELEVATOR, 1);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}

			this.carStatus.update(p); // Update the ElevatorStatus with the message
		} else if (this.carStatus.getNextDestination() == this.carStatus.getPosition()){
			this.stopAtFloor();
		}
	}


	private void elevatorControlLoop(){
		while(true){
			// Wait to receive packet and parse it
			DataPacket p = receiveRequest();

			this.parseIncomingRequest(p);

			// Handle the incoming request
			
			/*
			 <<Check the direction the elevator is going
			 <<Receive the floor the elevator is at
			 <<compare the floor to the direction arraylist
			 <<if the next floor is the top of the arraylist, send a motor off
			 <<if it's not then send a motor on 
			 <<loop till top or bottom is reached, then switch the direction of the arraylist
			 
			 */
			
			ArrayList<Integer> Requests;//One list to assign either the up or down list
			int nextfloor;//used to check the if the next floor is the right floor
			if(findNextMotorState()==MotorState.DOWN)//if the direction is down
			{
				Requests = downRequests;
				nextfloor=-1;
				
			}
			else //if the direction is up
			{
				Requests = upRequests;
				nextfloor=1;
			}
			
			while(!Requests.isEmpty()) //loops till all the requests in that direction are complete
			{
				byte floor[] = new byte[100];
				receivePacket = new DatagramPacket(floor, floor.length);
				
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
				int len = receivePacket.getLength();
				byte[] reply = new byte[1];
				if(floor[len-1]+nextfloor!=Requests.get(0)) //if the next floor is not the same as the floor at the top of the arraylist
				{
					//create reply motor on using data array
					reply[0]= 0;
				}
				
				else
				{
					//<<create reply motor off using data array
					//and remove that floor from the arraylist
					reply[0] = 1;
					if(nextfloor==1)
					{
						upRequests.remove(0);
						Requests.remove(0);
					}
					else
					{
						downRequests.remove(0);
						Requests.remove(0);
					}
				}
					//create reply motor on using data array
					sendPacket = new DatagramPacket(reply, 1,
							receivePacket.getAddress(), receivePacket.getPort());

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
					len = sendPacket.getLength();
					
					// Send the datagram packet to the client via the send socket. 
					try {
						sendSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				if(Requests.isEmpty()) //change direction of the elevator to do the requests of the opposite direction
				{
					if(nextfloor==1)
					{
						Requests=downRequests;
					}
					
					else
					{
						Requests=upRequests;
					}
				}
			}
			
			
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
