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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Enums.*;
import shared.*;


public class Elevator implements Runnable {
	//private static int idCounter = 1;		// ID for this elevator

	private DatagramPacket sendPacket;
	private DatagramSocket sendSocket;
	
	//public LinkedBlockingQueue<DataPacket> inputBuffer;
	public LinkedBlockingQueue<DatagramPacket> inputBuffer;		// Queue of input messages for this elevator

	private volatile int currentFloor = 1;						// Current floor of the elevator
	private boolean door = true;								// State of the door: true = open, false = closed
	private MotorState motorState;								// State of the motor for this elevator
	private boolean[] floorLights;								// Array containing the status of the floor lights in each elevator
	private int id;												// ID of this elevator
	private int MAX_FLOOR;										// Maximum floor that this elevator can travel to
	private SocketAddress schedulerAddress;						// SocketAddress of the scheduler to which it will return messages
	
	private boolean printDebug = true;							// PrintDebug enables printing debugging information to the console
	
	// Thread variables
	public FloorChangerThread floorChanger;						// The thread used to change the current floor of the elevator when running
	
	
	
	/**
	 * Constructor for the elevator class
	 * @param id			The ID of this elevator
	 * @param maxFloor		The maximum number of floors this elevator can travel to
	 * @param printDebug	Debugging variable, when enabled print additional information to the console
	 */
	public Elevator(int id, int maxFloor, boolean printDebug){
		
		// Create a DatagramSocket for the elevator to send return messages 
		try {
			sendSocket = new DatagramSocket(SocketPort.ELEVATOR_SENDER.getValue() + id);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		// TODO: determine if floor lights will be used
		// initialize all floor lights to off
		//this.floorLights = new boolean[numFloors];
		//for(int i = 0; i < numFloors; ++i){
		//	floorLights[i] = false;
		//	}

		this.currentFloor = 1;

		this.id = id;
		this.MAX_FLOOR = maxFloor;
		this.inputBuffer = new LinkedBlockingQueue<DatagramPacket>();
		this.floorChanger = new FloorChangerThread(this);
		this.printDebug = printDebug;
	}
	
	
	/**
	 * Getter for elevator input buffer
	 * @return inputBuffer for this elevator
	 */
	public LinkedBlockingQueue<DatagramPacket> getInputBuffer(){
		return this.inputBuffer;
	}
	
	
	/**
	 * Getter for the current floor of the elevator
	 * @return the currentFloor of the elevator
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}


	/**
	 * Setter for the current floor
	 * @param floor the floor to set this elevator to
	 */
	public void setCurrentFloor(int floor) {
		this.currentFloor = floor;
	}


	/**
	 * Getter for the state of the elevator doors
	 * @return True when open
	 */
	public boolean isDoor() {
		return door;
	}


	/**
	 * Setter for the state of the elevator doors
	 * @param The state of the doors to set (true when open, false when closed)
	 */
	public void setDoor(boolean door) {
		this.door = door;
	}


	/**
	 * Getter for the boolean array representing the status of the floor lights inside the elevator
	 * @return the floorLights
	 */
	public boolean[] getFloorLights() {
		return floorLights;
	}


	/**
	 * Setter for the boolean array representing the status of the floor lights inside the elevator
	 * @param floorLights the floorLights to set
	 */
	public void setFloorLights(boolean[] floorLights) {
		this.floorLights = floorLights;
	}


	/**
	 * Getter for the ID of the elevator
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * Getter for the maximum floor this elevator can visit
	 * @return the mAX_FLOOR
	 */
	public int getMAX_FLOOR() {
		return MAX_FLOOR;
	}


	/**
	 * Getter for the state of the motor on this elevator
	 * @return the motorState
	 */
	public MotorState getMotorState() {
		return motorState;
	}


	/**
	 * Setter for the state of the motor on this elevator
	 * @param motorState the motorState to set
	 */
	public void setMotorState(MotorState motorState) {
		this.motorState = motorState;
	}


	/**
	 * Getter for the SocketAddress of the scheduler for this elevator
	 * @return the schedulerAddress
	 */
	public SocketAddress getSchedulerAddress() {
		return schedulerAddress;
	}


	/**
	 * Setter for the SocketAddress of the scheduler for this elevator
	 * @param schedulerAddress the schedulerAddress to set
	 */
	public void setSchedulerAddress(SocketAddress schedulerAddress) {
		this.schedulerAddress = schedulerAddress;
	}
	
	
	// TODO: Determine if this needs to be removed
	/**
	 * Toggle the Floor light inside the elevator on or off
	 * @param p	A DataPacket containing the floor number to toggle inside
	 */
	private void setFloorLight(DataPacket p){
		this.floorLights[(int) p.getStatus()[0]] = !this.floorLights[(int) p.getStatus()[0]];
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
	
	
	private DataPacket createEchoPacket(SubsystemType subSystem, byte[] status){
		return new DataPacket(OriginType.ELEVATOR, (byte) this.id, subSystem, status);
	}
	
	
	/**
	 * Send a DataPacket to a specified SocketAddress
	 * @param p			The DataPacket to be sent
	 * @param address	The SocketAddress of the location to send the DataPacket
	 */
	private void sendDataPacket(DataPacket p, SocketAddress address){
		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, address);
		sendPacket.setPort(SocketPort.SCHEDULER_LISTENER.getValue());


		//System.out.println("Elevator: Sending packet...");
		
		if (printDebug) {
			printDatagramPacket(sendPacket, "s");
		}
		

		try {
			this.sendSocket.send(this.sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//System.out.println("Elevator: packet sent \n");
	}
	
	
	// TODO: refactor this function to not use dp
	/**
	 * Send the location of the elevator back to the Scheduler
	 * @param dp The DatagramPacket received from the Scheduler
	 */
	public void sendLocation(DatagramPacket dp) {
		DataPacket p = new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.LOCATION, new byte[] {(byte) this.currentFloor});

		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, dp.getAddress(), SocketPort.SCHEDULER_LISTENER.getValue());

		//System.out.println( "Elevator: Sending packet to scheduler ");
		
		if (printDebug) {
			printDatagramPacket(this.sendPacket, "s");
		}
		
		// TODO: determine if we need this anymore
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException ie) {
			System.err.println(ie);
		}
		

		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//System.out.println("Elevator: packet sent \n");
	}
	
	/**
	 * Send the location of this elevator back to the scheduler
	 */
	public void sendLocation(){
		this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.LOCATION, new byte[] {(byte) this.currentFloor}), this.getSchedulerAddress());
	}
	
	
	/**
	 * Main function that handles changes to the motor state
	 * @param command	The new command for the elevator's motor
	 * @param address	The SocketAddress of the Scheduler (for returning the message)
	 */
	public void motorController(MotorState command, SocketAddress address) {
		System.out.println("Elevator " + id + ": I am at floor "+ currentFloor);

		if(command == MotorState.DOWN) {
			if (currentFloor > 0){
				// Send echo back saying motor is going down
				this.motorState = MotorState.DOWN;
				this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
				
				// If the floorChanger is already running, then do not start it
				if (!this.floorChanger.isRunning()){
					this.floorChanger.start();
				}
			}
		
		} else if (command == MotorState.UP) {
			// While the elevator is below the maximum floor
			if (currentFloor < MAX_FLOOR){
				// Send echo back to scheduler saying motor is going up
				this.motorState = MotorState.UP;
				//changed the second byte from id to current floor, so that scheduler will get update of the current floor
				this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
				
				// If the floorChanger is already running, then do not start it
				if (!this.floorChanger.isRunning()){
					this.floorChanger.start();
				}
				
			}
			
			
		} else if (command == MotorState.OFF) {
			
			this.motorState = MotorState.OFF;
			
			// If the thread for changing floors is running, then stop it
			if (this.floorChanger.isRunning()){
				this.floorChanger.interrupt();
			}
			
			this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
			
			// TODO: do we need this sleep anymore?
			try {
				TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
			} catch (InterruptedException ie) {
				System.err.println(ie);
			}
			
			System.out.println("Elevator " + id + " : I am at  "+ currentFloor);
		}

	}
	
	
	// TODO: determine if we need this function anymore?
	public String[] GetString(byte[] bytes) throws ClassNotFoundException, IOException
	{
		// Form a String from the byte array.
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

		final String[] stringArray2 = (String[]) objectInputStream.readObject();

		objectInputStream.close();
		return stringArray2;
	}
	
	
	/**
	 * Main control function for the elevator. Receive a single request and echo it back to the scheduler
	 */
	public void receiveAndEcho() {
		// at this stage, elevator will decode the packet
		// The elevator will decode the packet
		
		byte[] data = new byte[100];
		DatagramPacket packet = new DatagramPacket(data, data.length);							// The DatagramPacket pulled from the inputBuffer is stored in this variable
		DataPacket p = new DataPacket(OriginType.ELEVATOR, (byte) this.getId(), null, null);	// The DataPacket converted from the DatagramPacket is stored in this variable
		
		
		// Get DatagramPacket from inputQueue
		try {
			//System.out.println("Elevator " + this.getId() + " waiting for input packet");
			packet = this.inputBuffer.take();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		
		p = new DataPacket(packet.getData());
		
		//System.out.println("At time " + LocalDateTime.now().toString());
		
		// If the DataPacket has an Error as the OriginType
		if(p.getOrigin() == OriginType.ERROR)//Error packets
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
			default:
				break;

			}

		}
		else if(p.getSubSystem() == SubsystemType.MOTOR) {
			//case of motor
			System.out.print("SUBSYSTEM IS MOTOR  " );

			switch(MotorState.convertFromByte(p.getStatus()[0])){
			case DOWN:
				System.out.println("GOING DOWN" );
				motorController(MotorState.DOWN, packet.getSocketAddress());		//motor down and send message
				break;
			case OFF:
				System.out.println("MOTOR OFF" );
				motorController(MotorState.OFF, packet.getSocketAddress());		//motor off
				break;
			case UP:
				System.out.println("GOING UP" );
				motorController(MotorState.UP, packet.getSocketAddress());		//motor up and send message
				break;
			default:
				System.err.println("Invalid type");
				break;
			}
		} else if (p.getSubSystem() == SubsystemType.DOOR) {
			System.out.print("SUBSYSTEM IS DOOR  " );

			switch(DoorState.convertFromByte(p.getStatus()[0])){
			case OPEN:
				door = true;		//door open and send message
				System.out.println("DOOR OPENED  " );
				break;
			case CLOSED:
				door = false;		//door closed and send message
				System.out.println("DOOR CLOSED " );
				break;
			default:
				System.err.println("Invalid State");
				break;
			}
		} else if (p.getSubSystem() == SubsystemType.LOCATION){
			System.out.print("SUBSYSTEM IS LOCATION  " );
			sendLocation(packet);
		} else if (p.getSubSystem() == SubsystemType.INPUT){
			System.out.print("SUBSYSTEM IS INPUT");
			setFloorLight(p);
		}
		
		// Echo back the packet if not from the motor or the location
		if ((p.getSubSystem() != SubsystemType.MOTOR) && (p.getSubSystem() != SubsystemType.LOCATION)){
			sendDataPacket(createEchoPacket(p.getSubSystem(), p.getStatus()), packet.getSocketAddress());
		}
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			receiveAndEcho();
		}

	}
}
