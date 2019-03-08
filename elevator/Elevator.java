package elevator;
//change
//This is elevator class, it will keep running and act like server
//The logic is following
//1. waiting a packet contains [orig_Type, id, subsystem, status], receiving port is 69
//			orig_type (1byte) : floor(1), scheduler(2), elevator(3)
//			id (1byte)        : elevator will have 3 id
//			subsystem(1byte)  : MOTOR(7),
//								DOOR(6),
//								CARLAMP(5),
//								FLOORLAMP(4),
//								LOCATION(3),
//								INPUT(2),
//								REQUEST(1)
//			status (List bytes[]): MOTOR: UP3/down1/off2
//								   door: open/close
//								   carLamp: floor#
//								   floorlamp:up/down
//								   location:floor#
//								   request:[currentFloor,DesFloor]
//2. after receive the packet, decode the packet(most likely from scheduler, only need to decode the third byte)
//3. if motor is running. keep sending packet [3, id, 3, MOTOR status] till receive a confirm packet[2, 0, 7, Motor status]
//4. stop and send final packet [3,id,3,MotorOFF]


//update: created three threads, listens to the information from handler, run and wait  


//author Hongbo Pang


import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import Enums.*;
import shared.*;


public class Elevator implements Runnable {
	//private static int idCounter = 1;		// ID for this elevator

	private DatagramPacket sendPacket;
	private DatagramSocket sendSocket;

	private int currentFloor = 1;
	private int DesFloor;
	private boolean door = false;
	private int count;				// number of floors in the elevator
	private boolean[] floorLights;	// Array containing the status of the floor lights in each elevator
	private int id;


	MotorState motorState;


	public Elevator(int id){
		try {

			sendSocket = new DatagramSocket(SocketPort.ELEVATOR_SENDER.getValue() + id);

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

		//count = numFloors;
		//id = idCounter++;

		// initialize all floor lights to off
		//this.floorLights = new boolean[numFloors];
		//for(int i = 0; i < numFloors; ++i){
		//	floorLights[i] = false;
	//	}

		this.currentFloor = 1;

		this.id = id;
	}

	/**
	 * Print what was received from a datagram packet to the console
	 *
	 * @param p		datagram packet that was received
	 * @param mode	String representing if a packet was sent ("sent") or received ("received")
	 */
	private static void printDatagramPacket(DatagramPacket p, String mode){
		if (mode == "s"){
			System.out.println("Elevator sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
 			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "r") {
			System.out.println("Elevator received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		System.out.println("Data (String): " + new DataPacket(p.getData()).toString()); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}


	public void receiveAndEcho(DataPacket p, DatagramPacket packet) throws IOException, ClassNotFoundException, InterruptedException {
		// at this stage, elevator will decode the packet

		// The elevator will decode the packet
		if(p.getOrigin() == OriginType.ERROR)
		{
			switch(p.getSubSystem()) {
			case MOTOR:
				System.out.println("Motor Error");
				break;
			case DOOR:
				System.out.println("Door Error");
				break;
			case CARLAMP:
				System.out.println("Carlamp Error");
				break;
			case FLOORLAMP:
				System.out.println("Floorlamp Error");
				break;
			
			}
			
		}
		else if(p.getSubSystem() == SubsystemType.MOTOR) {
			//case of motor
			System.out.println("SUBSYSTEM IS MOTOR  " );

			switch(MotorState.convertFromByte(p.getStatus()[0])){
			case DOWN:
				System.out.println("going down" );
				Motor(MotorState.DOWN, packet.getSocketAddress());		//motor down and send message
				break;
			case OFF:
				System.out.println("motor off" );
				Motor(MotorState.OFF, packet.getSocketAddress());		//motor off
				break;
			case UP:
				System.out.println("going up" );
				Motor(MotorState.UP, packet.getSocketAddress());		//motor up and send message
				break;
			default:
				  
				System.out.println("Invalid type");
				break;
			}
		} else if (p.getSubSystem() == SubsystemType.DOOR) {
			System.out.println("SUBSYSTEM IS DOOR  " );

			switch(DoorState.convertFromByte(p.getStatus()[0])){
			case OPEN:
				door = true;		//door open and send message
				System.out.println("door opened  " );
				break;
			case CLOSED:
				door = false;		//door closed and send message
				System.out.println("door closed " );
				break;
			default:
				System.out.println("Invalid State");
				break;
			}
		} else if (p.getSubSystem() == SubsystemType.LOCATION){
			System.out.println("SUBSYSTEM IS LOCATION  " );
			sendLocation(packet);
		} else if (p.getSubSystem() == SubsystemType.INPUT){
			System.out.println("SUBSYSTEM IS INPUT");
			setFloorLight(p);
		}

		System.out.println("\n\n");



		// Echo back the packet if not from the motor or the location
		if ((p.getSubSystem() != SubsystemType.MOTOR) && (p.getSubSystem() != SubsystemType.LOCATION)){
			sendDataPacket(createEchoPacket(p.getSubSystem(), p.getStatus()), packet.getSocketAddress());
		}

		//receiveSocket.close();
	}


	// toggle the floor light inside the elevator to on or off
	private void setFloorLight(DataPacket p){
		this.floorLights[(int) p.getStatus()[0]] = !this.floorLights[(int) p.getStatus()[0]];
	}


	private DataPacket createEchoPacket(SubsystemType subSystem, byte[] status){
		return new DataPacket(OriginType.ELEVATOR, (byte) this.id, subSystem, status);
	}

	private void sendDataPacket(DataPacket p, SocketAddress address){
		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, address);


		System.out.println("Elevator: Sending packet...");

		printDatagramPacket(sendPacket, "s");

		try {
			this.sendSocket.send(this.sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Elevator: packet sent \n");
	}


	public void sendLocation(DatagramPacket dp) throws IOException, InterruptedException {
		DataPacket p = new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.LOCATION, new byte[] {(byte) this.currentFloor});

		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, dp.getAddress(), dp.getPort());

		System.out.println( "Elevator: Sending packet to scheduler ");

		printDatagramPacket(this.sendPacket, "s");

		TimeUnit.SECONDS.sleep(1);

		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Elevator: packet sent \n");

		// We're finished, so close the sockets.
		//sendSocket.close();
	}

	public void Motor(MotorState command, SocketAddress address) throws IOException, InterruptedException{
		System.out.println("Elevator " + id + " : I am at floor "+ currentFloor);

		if(command == MotorState.DOWN) {
			while(currentFloor > 0) {
			this.motorState = MotorState.DOWN;
			this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
			TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
			currentFloor--;
			//sendLocation();
			System.out.println("Elevator " + id + " : I am at  "+ currentFloor);
			}
		} else if (command == MotorState.UP){
			while(currentFloor < 11) {
			this.motorState = MotorState.UP;
			
			//changed the second byte from id to current floor, so that scheduler will get update of the current floor
			this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte)currentFloor, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
			TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
			currentFloor++;
			//sendLocation();
			System.out.println("Elevator " + id + " : I am at  "+ currentFloor);
			}
		} else if (command == MotorState.OFF){
			this.motorState = MotorState.OFF;
			this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
			TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
			System.out.println("Elevator " + id + " : I am at  "+ currentFloor);
		}
	
	}

	public String[] GetString(byte[] bytes) throws ClassNotFoundException, IOException
	{
		// Form a String from the byte array.
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

		final String[] stringArray2 = (String[]) objectInputStream.readObject();

		objectInputStream.close();
		return stringArray2;
	}
	
	
	// TODO: determine if this can be deleted
	private static byte[] convertToBytes(String[] strings) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ObjectOutputStream objectOutputStream =
				new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(strings);
		objectOutputStream.flush();
		objectOutputStream.close();

		final byte[] data = byteArrayOutputStream.toByteArray();

		return data;
	}

	/**
	 * @return current location
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * @return destination floor
	 */
	public int getDesFloor() {
		return DesFloor;
	}

	/**
	 *
	 * @return status of the door
	 */
	public boolean getDoorState() {
		return door;
	}

	/**
	 * @return count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return elevator id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return state of motor
	 */
	public MotorState getMotorState() {
		return motorState;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	// TODO: remove this from the function
	/*
	public static void main( String args[] ) throws IOException, ClassNotFoundException, InterruptedException {
		while(true) {
			Elevator c = new Elevator(10, 1);
			//c.receiveAndEcho();
		}
	}
	*/
	

}
