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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Enums.*;
import shared.*;


public class Elevator implements Runnable {
	//private static int idCounter = 1;		// ID for this elevator

	private DatagramPacket sendPacket;
	private DatagramSocket sendSocket;
	
	//public LinkedBlockingQueue<DataPacket> inputBuffer;
	public LinkedBlockingQueue<DatagramPacket> inputBuffer;

	private volatile int currentFloor = 1;
	private int DesFloor;
	private boolean door = true;	//true = open, false = closed
	private int count;				// number of floors in the elevator
	private boolean[] floorLights;	// Array containing the status of the floor lights in each elevator
	private int id;
	private int MAX_FLOOR;			// Maximum floor that this elevator can travel to
	
	MotorState motorState;
	
	// Thread variables
	static volatile boolean exitMovementFlag = false;
	public Thread floorChanger;
	private static final int TIME_BETWEEN_FLOORS = 3000;
	
	private SocketAddress schedulerAddress;
	

	public Elevator(int id, int maxFloor){
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
		this.MAX_FLOOR = maxFloor;
		//this.inputBuffer = new LinkedBlockingQueue<DataPacket>();
		this.inputBuffer = new LinkedBlockingQueue<DatagramPacket>();
	}
	
	
	/**
	 * Getter for elevator input buffer
	 * @return inputBuffer for this elevator
	 */
	/*public LinkedBlockingQueue<DataPacket> getInputBuffer(){
		return this.inputBuffer;
	}*/
	
	
	/**
	 * Getter for elevator input buffer
	 * @return inputBuffer for this elevator
	 */
	public LinkedBlockingQueue<DatagramPacket> getInputBuffer(){
		return this.inputBuffer;
	}
	
	
	/**
	 * @return the currentFloor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}


	/**
	 * @param currentFloor the currentFloor to set
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}


	/**
	 * @return the door
	 */
	public boolean isDoor() {
		return door;
	}


	/**
	 * @param door the door to set
	 */
	public void setDoor(boolean door) {
		this.door = door;
	}


	/**
	 * @return the floorLights
	 */
	public boolean[] getFloorLights() {
		return floorLights;
	}


	/**
	 * @param floorLights the floorLights to set
	 */
	public void setFloorLights(boolean[] floorLights) {
		this.floorLights = floorLights;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the mAX_FLOOR
	 */
	public int getMAX_FLOOR() {
		return MAX_FLOOR;
	}


	/**
	 * @param mAX_FLOOR the mAX_FLOOR to set
	 */
	public void setMAX_FLOOR(int mAX_FLOOR) {
		MAX_FLOOR = mAX_FLOOR;
	}


	/**
	 * @return the motorState
	 */
	public MotorState getMotorState() {
		return motorState;
	}


	/**
	 * @param motorState the motorState to set
	 */
	public void setMotorState(MotorState motorState) {
		this.motorState = motorState;
	}


	/**
	 * @return the schedulerAddress
	 */
	public SocketAddress getSchedulerAddress() {
		return schedulerAddress;
	}


	/**
	 * @param schedulerAddress the schedulerAddress to set
	 */
	public void setSchedulerAddress(SocketAddress schedulerAddress) {
		this.schedulerAddress = schedulerAddress;
	}


	/**
	 * @param inputBuffer the inputBuffer to set
	 */
	public void setInputBuffer(LinkedBlockingQueue<DatagramPacket> inputBuffer) {
		this.inputBuffer = inputBuffer;
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
	

	/**
	 * New Receive and Echo
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public void receiveAndEcho() throws IOException, ClassNotFoundException, InterruptedException {
		// at this stage, elevator will decode the packet
		// The elevator will decode the packet
		
		byte[] data = new byte[100];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		DataPacket p = new DataPacket(null, (Byte) null, null, new byte[] {(Byte) null});
		
		
		// Get DatagramPacket from inputQueue
		try {
			packet = this.inputBuffer.take();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		
		p = new DataPacket(packet.getData());
		
		
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
	
	
	
	
	
	/**
	 * Deprecated Receive and Echo
	 * @param p
	 * @param packet
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	/*public void receiveAndEcho(DataPacket p, DatagramPacket packet) throws IOException, ClassNotFoundException, InterruptedException {
		// at this stage, elevator will decode the packet
		// The elevator will decode the packet
		
		
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
	}*/


	// toggle the floor light inside the elevator to on or off
	private void setFloorLight(DataPacket p){
		this.floorLights[(int) p.getStatus()[0]] = !this.floorLights[(int) p.getStatus()[0]];
	}


	private DataPacket createEchoPacket(SubsystemType subSystem, byte[] status){
		return new DataPacket(OriginType.ELEVATOR, (byte) this.id, subSystem, status);
	}

	private void sendDataPacket(DataPacket p, SocketAddress address){
		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, address);
		sendPacket.setPort(SocketPort.SCHEDULER_LISTENER.getValue());


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

		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, dp.getAddress(), SocketPort.SCHEDULER_LISTENER.getValue());

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
	
	/**
	 * Send the location of this elevator back to the scheduler
	 */
	public void sendLocation(){
		this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.LOCATION, new byte[] {(byte) this.currentFloor}), this.getSchedulerAddress());
	}
	
	public void Motor(MotorState command, SocketAddress address) throws IOException, InterruptedException{
		System.out.println("Elevator " + id + " : I am at floor "+ currentFloor);

		if(command == MotorState.DOWN) {
			if (currentFloor > 0){
				// Send echo back saying motor is going down
				this.motorState = MotorState.DOWN;
				this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {this.motorState.getByte()}), address);
				
				this.floorChanger = new Thread(new FloorChangerThread(this));
				this.floorChanger.start();
				
				/*
				// Create thread inside main elevator function to increment the floor while allowing elevator to still run
				new Thread() {
					public void run() {
						// Exit the thread when the MotorState is no longer down
						while ((currentFloor <= MAX_FLOOR) && (motorState == MotorState.DOWN) && (exitMovementFlag)){
							
							try {
								Thread.sleep(TIME_BETWEEN_FLOORS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							currentFloor++;															// Increment floor of the elevator
							System.out.println("Elevator " + id + " : I am at  "+ currentFloor);	// Print location
							sendLocation();														// Send a packet back to Scheduler with location
						}
					}
				}.start();
				*/
			}
		
		} else if (command == MotorState.UP) {
			// While the elevator is below the maximum floor
			if (currentFloor < MAX_FLOOR){
				// Send echo back to scheduler saying motor is going up
				this.motorState = MotorState.UP;
				//changed the second byte from id to current floor, so that scheduler will get update of the current floor
				this.sendDataPacket(new DataPacket(OriginType.ELEVATOR, (byte) this.id, SubsystemType.MOTOR, new byte[] {(byte) this.currentFloor, this.motorState.getByte()}), address);
				
				this.floorChanger = new Thread(new FloorChangerThread(this));
				this.floorChanger.start();
			}
			
			
		} else if (command == MotorState.OFF) {
			
			//Elevator.exitMovementFlag = true;	// Stop the movement of the elevator
			
			this.motorState = MotorState.OFF;
			
			// If the thread for changing floors is running, then interrupt it
			if (this.floorChanger.isAlive()){
				this.floorChanger.interrupt();
			}
			
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
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				receiveAndEcho();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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
