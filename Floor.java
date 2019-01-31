// Floor.java
// This class represents the floors in the buildings
// Stores the scheduler that it communicates with, its floor number and the buttons on each floor

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Floor {

	private Scheduler scheduler;//the scheduler with which the floor will communicate 
	private int floorNumber;//floor number, unique id
	private FloorButton[] floorButtons;//list of buttons on floor
	private FloorLamp[] floorLamps;//list of lamps on floor
	private int[] requests;//list of requests
	private int requestInsert = 0;//where to insert in list of requests


	DatagramPacket sendPacket, receivePacket; //packets and socket used to send information
	DatagramSocket sendReceiveSocket;



	//TODO:Create toggleable lamps
	//TODO:Create floor request list

	public Floor(Scheduler scheduler, int floorNumber){
		try {
			sendReceiveSocket = new DatagramSocket();
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
			floorButtons = new FloorButton[1];//create arrays of size one
			floorLamps = new FloorLamp[1];
			floorButtons[0] = new FloorButton(Direction.UP);//only button on the floor will be to go up
			floorLamps[0] = new FloorLamp(Direction.UP);//only lamp on floor will point up
		}else {
			floorButtons = new FloorButton[2];//create arrays of size two
			floorLamps = new FloorLamp[2];
			floorButtons[0] = new FloorButton(Direction.DOWN);//create an up button
			floorButtons[1] = new FloorButton(Direction.UP);//create a down button
			floorLamps[0] = new FloorLamp(Direction.DOWN);
			floorLamps[1] = new FloorLamp(Direction.UP);
		}

		requests = new int[scheduler.getTopFloor()];//can only have as many requests as there are floors
	}

	/**
	 * @param time The time request was initiated in bytes [Hours,Minutes,Seconds,Milliseconds]
	 * @param direction The direction requester would like to travel
	 * @return byte array to be sent within a DatagramPacket via DatagramSocket
	 */
	public byte[] createMessage(byte[] time, String[] direction) {
		byte[] returnBytes=null;//instantiate array to be returned
		int directionCode;//direction code
		if(direction[0].equalsIgnoreCase("UP")) {//if requester wants to go up
			directionCode = 1;
		}else if(direction[0].equalsIgnoreCase("DOWN")) {//if requester wants to go down
			directionCode = 0;
		}else directionCode=-1;//invalid request, should never happen

		ByteArrayOutputStream output = new ByteArrayOutputStream();//output can be dynamically written to
		for (int i=0;i<time.length;i++) {//write each time parameter
			output.write(time[i]);
		}
		output.write(floorNumber);//write the floor number
		if (directionCode!=-1) {//if the direction code is not invalid
			output.write(directionCode);
		}
		returnBytes = output.toByteArray();//creates single byte array to be sent
		//System.out.println(Arrays.toString(returnBytes));
		return returnBytes;
	}

	public void sendAndReceive(byte[] message){

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
		System.out.println("(Bytes)" + Arrays.toString(sendPacket.getData()) + "\n");
	}

	/**
	 * Create a request to be sent to the scheduler
	 * @param request String containing request information (time, floor, direction)
	 */
	public void newRequest(String[] request) {
		TimeStamp ts = new TimeStamp (request[0]);//create a new timestamp from the request string
		byte[] time = ts.toBytes();//get the bytes of the timestamp
		byte[] message = createMessage(time, Arrays.copyOfRange(request,2,3));
		requests[requestInsert] = Integer.parseInt(request[3]);
		System.out.println(requests[requestInsert]);
		requestInsert++;
		sendAndReceive(message);

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
	public int getFloorNumber() {//getter for floor number
		return floorNumber;
	}

}
