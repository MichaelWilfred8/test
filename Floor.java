// Floor.java
// This class represents the floors in the buildings
// Stores the scheduler that it communicates with, its floor number and the buttons on each floor

import java.io.*;
import java.net.*;
import java.util.Arrays;
import Enums.Direction;

public class Floor {

	private Scheduler scheduler;//the scheduler with which the floor will communicate 
	private int floorNumber;//floor number, unique id
	private FloorButton[] floorButtons;//list of buttons on floor
	private FloorLamp[] floorLamps;//list of lamps on floor
	private byte[][] requests;//list of requests
	private int requestInsert = 0;//where to insert in list of requests
	private boolean requested;//if an elevator has been requested for this floor


	DatagramPacket sendPacket, receivePacket; //packets and socket used to send information
	DatagramSocket sendSocket;

	/**
	 * Generic constructor
	 * @param scheduler
	 * @param floorNumber
	 */
	public Floor(Scheduler scheduler, int floorNumber){
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.scheduler = scheduler;
		this.floorNumber = floorNumber;

		if (floorNumber == this.scheduler.getTopFloor()) {//if the floor is the top floor
			floorButtons = new FloorButton[1];//create arrays of size one
			floorLamps = new FloorLamp[1];
			floorButtons[0] = new FloorButton(Direction.DOWN);//only button on the floor will to go down
			floorLamps[0] = new FloorLamp(Direction.DOWN);//only light on floor will point down
		}else if (floorNumber == 1) {//if the floor is the bottom floor
			floorButtons = new FloorButton[2];//create arrays of size one
			floorLamps = new FloorLamp[2];
			floorLamps[0] = null;
			floorButtons[0] = null;
			floorButtons[1] = new FloorButton(Direction.UP);//only button on the floor will be to go up
			floorLamps[1] = new FloorLamp(Direction.UP);//only lamp on floor will point up
		}else {
			floorButtons = new FloorButton[2];//create arrays of size two
			floorLamps = new FloorLamp[2];
			floorButtons[0] = new FloorButton(Direction.DOWN);//create an up button
			floorButtons[1] = new FloorButton(Direction.UP);//create a down button
			floorLamps[0] = new FloorLamp(Direction.DOWN);
			floorLamps[1] = new FloorLamp(Direction.UP);
		}

		requests = new byte[scheduler.getTopFloor()][];//can only have as many requests as there are floors
		requested = false;
	}

	/**
	 * send byte array to scheduler
	 * @param message, message to be sent
	 */
	public void sendRequest(byte[] message){

		// Construct a datagram packet that is to be sent to a specified port on a specified host.
		try {
			sendPacket = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), 23);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//print details of outgoing packet
		System.out.println("Floor: Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		int len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		System.out.println("(Bytes)" + Arrays.toString(sendPacket.getData()));

		// Send the datagram packet to the server via the send/receive socket. 
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Sent\n");

		//TODO:notice elevator arrival
		//TODO:toggle direction lamp
		//TODO:remove direction lamp when it leaves
	}

	/**
	 * Create elevator request
	 * @param request request data from csv
	 * @return byte array message
	 */
	private byte[] requestElevator(String[] request){
		byte[] returnBytes=null;//instantiate array to be returned
		int directionCode;//direction code

		TimeStamp ts = new TimeStamp (request[0]);//create a new timestamp from the request string
		byte[] time = ts.getBytes();//get the bytes of the timestamp

		if(request[2].equalsIgnoreCase("UP")) {//if requester wants to go up
			directionCode = 2;
		}else {//if requester wants to go down
			directionCode = 1;
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();//output can be dynamically written to
		for (int i=0;i<time.length;i++) {//write each time parameter
			output.write(time[i]);
		}
		output.write(floorNumber);//write the floor number
		output.write(directionCode);//write the direction
		output.write(-1);//write -1 to signify this is not a button press within the elevator
		returnBytes = output.toByteArray();//creates single byte array to be sent
		return returnBytes;
	}

	/**
	 * Create destination request, to be stored until an elevator arrives
	 * @param request request data from csv
	 * @return byte array message
	 */
	private byte[] destinationRequest(String[] request) {
		byte[] messageBytes = null;
		int directionCode;//direction code

		TimeStamp ts = new TimeStamp (request[0]);//create a new timestamp from the request string
		byte[] time = ts.getBytes();//get the bytes of the timestamp

		if(request[2].equalsIgnoreCase("UP")) {//if requester wants to go up
			directionCode = 1;
		}else {//if requester wants to go down
			directionCode = 0;
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();//output can be dynamically written to
		for (int i=0;i<time.length;i++) {//write each time parameter
			output.write(time[i]);
		}
		output.write(floorNumber);//write the floor number
		output.write(directionCode);//write the direction
		output.write(Integer.parseInt(request[3]));//write the destination floor
		messageBytes = output.toByteArray();//creates single byte array to be sent

		return messageBytes;
	}

	/**
	 * send all requests for floor to scheduler
	 */
	public void purgeRequests() {
		System.out.println("Floor " + floorNumber + " is purging");
		for (int i=0; i<requestInsert; i++) {
			sendRequest(requests[i]);//send request to scheduler
			requests[i] = null;//clear request registry
		}

	}

	/**
	 * Create a request to be sent to the scheduler
	 * @param request String containing request information (time, floor, direction)
	 */
	public void newRequest(String[] request) {
		byte[] message = requestElevator(request);
		byte[] destination = destinationRequest(request);

		if(!floorButtons[message[17]-1].getState()) {//if the button indicating the direction the elevator travelling is not yet on
			floorButtons[message[17]-1].toggle();//switch it on
			System.out.println("Floor " + floorNumber + " is toggling it's " + floorButtons[message[17]-1].getDirection().toString() + " button on.");
			System.out.println("Floor "+ floorNumber +" lamp facing " + floorButtons[message[17]-1].getDirection().toString() + " is now " + floorButtons[message[17]-1].getStateString());	
		}

		if(!requested) {//if no request has been made for this floor
			sendRequest(message);//request an elevator
			requests[requestInsert++] = destination;//add destination to request list
			requested = true;//a request now has been made
		}else {
			requests[requestInsert++] = destination;//add destination to request list
		}
	}

	/**
	 * @param lampTrigger: toggles the correct button and lamp
	 */
	public void elevatorArrived(byte lampTrigger) {

		floorLamps[lampTrigger].toggle();
		System.out.println("Floor " + floorNumber + " is toggling it's " + floorLamps[lampTrigger].getDirection().toString() + " lamp on.");
		System.out.println("Floor lamp facing " + floorLamps[lampTrigger].getDirection().toString() + " is now " + floorLamps[lampTrigger].getStateString());
		
		floorButtons[lampTrigger].toggle();
		System.out.println("Floor " + floorNumber + " is toggling it's " + floorButtons[lampTrigger].getDirection().toString() + " button off.");
		System.out.println("Floor lamp facing " + floorButtons[lampTrigger].getDirection().toString() + " is now " + floorButtons[lampTrigger].getStateString());

		purgeRequests();
		
		floorLamps[lampTrigger].toggle();
		System.out.println("Floor " + floorNumber + " is toggling it's " + floorLamps[lampTrigger].getDirection().toString() + " lamp off.");
		System.out.println("Floor lamp facing " + floorLamps[lampTrigger].getDirection().toString() + " is now " + floorLamps[lampTrigger].getStateString());
		
	}

	/**
	 * 
	 * @return List of Floor Buttons
	 */
	public FloorButton[] getFloorButtons() {
		return floorButtons;
	}

	/**
	 * 
	 * @return List of Floor Lamps
	 */
	public FloorLamp[] getFloorLamps() {
		return floorLamps;
	}

	/**
	 * 
	 * @return Floor Number
	 */
	public int getFloorNumber() {	//getter for floor number
		return floorNumber;
	}

	/**
	 * 
	 * @return whether or not an elevator has been requested
	 */
	public boolean getReqested() {
		return requested;
	}

}