// Floor.java
// This class represents the floors in the buildings
// Stores the scheduler that it communicates with, its floor number and the buttons on each floor

package floor;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import Enums.Direction;
import Enums.OriginType;
import Enums.SubsystemType;
import shared.*;

// TODO: make not runnable?

public class Floor {

	private int floorNumber;//floor number, unique id
	private int targetElevator;//
	private FloorButton[] floorButtons;//list of buttons on floor
	private FloorLamp[] floorLamps;//list of lamps on floor
	private int [] requests;//list of requests
	private int requestCount = 0;//where to insert in list of requests
	private boolean requested;//if an elevator has been requested for this floor
	private BlockingQueue<DataPacket> output;//threaded sender

	public Floor(int highestFloor, int floorNumber, BlockingQueue<DataPacket> output){

		this.output = output;

		this.floorNumber = floorNumber;

		if (floorNumber == highestFloor) {//if the floor is the top floor
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

		requests = new int[highestFloor];//can only have as many requests as there are floors
		for (int i=0;i<requests.length;i++) {
			requests[i]=-1;
		}
		requested = false;
		targetElevator = -1;
	}

	/**
	 * send byte array to scheduler
	 * @param message, message to be sent
	 */
	public void sendRequest(DataPacket requests){

		try {
			output.put(requests);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Floor "+ floorNumber +" placed item in send queue\n");
	}

	/**
	 * Create elevator request
	 * @param request request data from csv
	 * @return byte array message
	 */
	private DataPacket requestElevator(String[] request){
		byte[] returnBytes=null;//instantiate array to be returned
		int directionCode;//direction code

		TimeStamp ts = new TimeStamp (request[0]);//create a new timestamp from the request string
		byte[] time = ts.getBytes();//get the bytes of the timestamp

		if(request[2].equalsIgnoreCase("UP")) {//if requester wants to go up
			directionCode = 2;
		}else {//if requester wants to go down
			directionCode = 1;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();//output can be dynamically written to
		for (int i=0;i<time.length;i++) {//write each time parameter
			out.write(time[i]);
		}

		out.write(directionCode);//write the direction
		out.write(-1);//write -1 to signify this is not a button press within the elevator
		returnBytes = out.toByteArray();//creates single byte array to be sent

		DataPacket message = new DataPacket(OriginType.FLOOR, (byte) this.getFloorNumber(), SubsystemType.REQUEST, returnBytes);

		return message;
	}

	/**
	 * Create destination request, to be stored until an elevator arrives
	 * @param request request data from csv
	 * @return byte array message
	 */
	private DataPacket destinationRequest(String[] request) {
		byte[] messageBytes = null;
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

		output.write(directionCode);//write the direction
		output.write(Integer.parseInt(request[3]));//write the destination floor
		messageBytes = output.toByteArray();//creates single byte array to be sent

		DataPacket message = new DataPacket(OriginType.FLOOR, (byte) this.getFloorNumber(), SubsystemType.REQUEST, messageBytes);

		return message;
	}

	/**
	 * send all requests for floor to scheduler
	 */
	public void purgeRequests() {
		System.out.println("Floor " + floorNumber + " is purging");
		ByteArrayOutputStream output = new ByteArrayOutputStream();//output can be dynamically written to
		output.write(targetElevator);
		output.write(requestCount);
		for (int i=0; i<requests.length; i++) {
			if (requests[i]!=-1) {
				output.write(i);//send request to scheduler
				requests[i] = -1;//clear request registry
			}
		}
		requestCount = 0;
		
		DataPacket destReq = new DataPacket(OriginType.FLOOR, (byte) this.getFloorNumber(), SubsystemType.INPUT, output.toByteArray());
		sendRequest(destReq);
	}

	/**
	 * Create a request to be sent to the scheduler
	 * @param request String containing request information (time, floor, direction)
	 */
	public void newRequest(String[] request) {
		
		DataPacket message = requestElevator(request);
		DataPacket destination = destinationRequest(request);

		if(!floorButtons[message.getStatus()[16]-1].getState()) {//if the button indicating the direction the elevator travelling is not yet on
			floorButtons[message.getStatus()[16]-1].toggle();//switch it on
			System.out.println("Floor " + floorNumber + " is toggling it's " + floorButtons[message.getStatus()[16]-1].getDirection().toString() + " button on.");
			System.out.println("Floor "+ floorNumber +" lamp facing " + floorButtons[message.getStatus()[16]-1].getDirection().toString() + " is now " + floorButtons[message.getStatus()[16]-1].getStateString());	
		}

		if(!requested) {//if no request has been made for this floor
			sendRequest(message);//request an elevator
			if (requests[destination.getStatus()[17]]==-1){//if there is not already a destination request
				requests[destination.getStatus()[17]] = 1;//indicate that the floor @ requests[i] is a destination
				requestCount++;
			}
			requested = true;//a request now has been made
		}else {
			if (requests[destination.getStatus()[17]]==-1){//if there is not already a destination request
				requests[destination.getStatus()[17]] = 1;//indicate that the floor @ requests[i] is a destination
				requestCount++;
			}
		}
	}

	/**
	 * @param lampTrigger: toggles the correct button and lamp
	 */
	public void elevatorArrived(byte[] input) { 
		byte lampTrigger = (byte) (input[0]-1);
		targetElevator = input[1];
		

		floorLamps[lampTrigger].toggle();
		System.out.println("Floor " + floorNumber + " is toggling it's " + floorLamps[lampTrigger].getDirection().toString() + " lamp on.");
		System.out.println("Floor lamp facing " + floorLamps[lampTrigger].getDirection().toString() + " is now " + floorLamps[lampTrigger].getStateString());

		floorButtons[lampTrigger].toggle();
		System.out.println("Floor " + floorNumber + " is toggling it's " + floorButtons[lampTrigger].getDirection().toString() + " button off.");
		System.out.println("Floor button facing " + floorButtons[lampTrigger].getDirection().toString() + " is now " + floorButtons[lampTrigger].getStateString());

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