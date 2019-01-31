// Floor.java
// This class represents the floors in the buildings
// Stores the scheduler that it communicates with, its floor number and the buttons on each floor

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Floor {

	private Scheduler scheduler;
	private int floorNumber;
	private FloorButton[] floorButtons;

	DatagramPacket sendPacket, receivePacket; //packets and socket used to send information
	DatagramSocket sendReceiveSocket;

	public Floor(Scheduler scheduler, int floorNumber){
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		this.scheduler = scheduler;
		this.floorNumber = floorNumber;

		if (floorNumber == this.scheduler.getTopFloor()) {//if the floor is the top floor
			floorButtons = new FloorButton[1];//create an array of size one
			floorButtons[0] = new FloorButton(FloorButtonDirection.DOWN);//only button on the floor will to go down
		}else if (floorNumber == 1) {//if the floor is the bottom floor
			floorButtons = new FloorButton[1];//create an array of size one
			floorButtons[0] = new FloorButton(FloorButtonDirection.UP);//only button on the floor will be to go up
		}else {
			floorButtons = new FloorButton[2];//create an array of size two
			floorButtons[0] = new FloorButton(FloorButtonDirection.DOWN);//create an up button
			floorButtons[1] = new FloorButton(FloorButtonDirection.UP);//create a down button
		}
	}

	/**
	 * @param time The time request was initiated in bytes [Hours,Minutes,Seconds,Milliseconds]
	 * @param direction The direction requester would like to travel and destination floor
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
		try {
			for (int i=0;i<time.length;i++) {//write each time parameter
				output.write(time[i]);
			}
			output.write(floorNumber);//write the floor number
			if (directionCode!=-1) {//if the direction code is not invalid
				output.write(directionCode);
			}else {//else throw an exception
				throw new Exception("Invalid Direction Code");
			}
			output.write(Integer.parseInt(direction[1]));//write destination floor
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @param request String containing request information (time, floor, direction, destination)
	 */
	public void newRequest(String[] request) {
		TimeStamp ts = new TimeStamp (request[0]);//create a new timestamp from the request string
		byte[] time = ts.toBytes();//get the bytes of the timestamp
		byte[] message = createMessage(time, Arrays.copyOfRange(request,2,4));
		
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
	 * @return Floor Number
	 */
	public int getFloorNumber() {	//getter for floor number
		return floorNumber;
	}

}
