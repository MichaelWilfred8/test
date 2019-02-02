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



//author Hongbo Pang


import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import Enums.*;


public class elevator1 {

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	int currentFloor = 1;
	int DesFloor;
	boolean door = false;
	int count;				// number of floors in the elevator
	boolean[] floorLights;	// Array containing the status of the floor lights in each elevator
	
	MotorState motorState;
	
	
	
	
	public elevator1()
	{
		try {
		
		   sendSocket = new DatagramSocket();
		
		   // Construct a datagram socket and bind it to port 5000 
		   // on the local host machine. This socket will be used to
		   // receive UDP Datagram packets.
		   receiveSocket = new DatagramSocket(69);
		   
		   // to test socket timeout (2 seconds)
		   //receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
		   se.printStackTrace();
		   System.exit(1);
		} 
	}
	
	
	public elevator1(int numFloors){
		try {
		
		   sendSocket = new DatagramSocket();
		
		   // Construct a datagram socket and bind it to port 5000 
		   // on the local host machine. This socket will be used to
		   // receive UDP Datagram packets.
		   receiveSocket = new DatagramSocket(69);
		   
		   // to test socket timeout (2 seconds)
		   //receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
		   se.printStackTrace();
		   System.exit(1);
		} 
		
		count = numFloors;
		
		// initialize all floor lights to off
		this.floorLights = new boolean[numFloors];
		for(int i = 0; i < numFloors; ++i){
			floorLights[i] = false;
		}
		
		this.currentFloor = 1;
	}
	
//	public void receiveAndEcho() throws IOException, ClassNotFoundException, InterruptedException
//	{
//	
//		//receive the packet
//		byte data[] = new byte[100];
//		
//		receivePacket = new DatagramPacket(data, data.length);
//		System.out.println("Elevator: Waiting for Command.\n");
//		
//		try {        
//		   System.out.println("Waiting..."); // so we know we're waiting
//		   receiveSocket.receive(receivePacket);
//		} catch (IOException e) {
//		   System.out.print("IO Exception: likely:");
//		   System.out.println("Receive Socket Timed Out.\n" + e);
//		   e.printStackTrace();
//		   System.exit(1);
//		}
//		
//		
//		System.out.println("packet is  " );
//		String[] array = GetString(receivePacket.getData());
//		System.out.println(Arrays.toString(array));
//		System.out.println();
//		
//		int subsystem = Integer.parseInt(array[2]);
//		int state = Integer.parseInt(array[3]);
//		System.out.println("SUBSYSTEM IS   " + subsystem );
//		System.out.println("STATE IS   " + state );
//		
//		// at this stage, elevator will decode the packet 
//		
//		if(subsystem == 7) {
//			//case of motor
//			System.out.println("SUBSYSTEM IS MOTOR  " );
//			if(state == 1){
//				System.out.println("going down" );
//				Motor(1);		//motor down and send message
//			}
//			
//			if(state == 2){
//				System.out.println("motor off" );
//				Motor(2);		//motor off
//			}
//			
//			if(state == 3){
//				System.out.println("going up" );
//				Motor(3);		//motor up and send message
//			}
//		}
//		
//		if(subsystem==6) {
//			System.out.println("SUBSYSTEM IS DOOR  " );
//			
//			if(state == 1){
//				door = true;		//motor down and send message
//				System.out.println("door opened  " );
//			}
//			if(state == 0){
//				door = false;		//motor down and send message
//				System.out.println("door closed " );
//			}
//		}
//		
//		if(subsystem == 3) {
//			System.out.println("SUBSYSTEM IS LOCATION  " );
//				sendLocation();		
//		}
//		
//		receiveSocket.close();
//	}
	
	public void receiveAndEcho() throws IOException, ClassNotFoundException, InterruptedException
	{
		while (true){
			//receive the packet
			byte data[] = new byte[100];
			
			receivePacket = new DatagramPacket(data, data.length);
			System.out.println("Elevator: Waiting for Command.\n");
			
			try {        
			   System.out.println("Waiting..."); // so we know we're waiting
			   receiveSocket.receive(receivePacket);
			} catch (IOException e) {
			   System.out.print("IO Exception: likely:");
			   System.out.println("Receive Socket Timed Out.\n" + e);
			   e.printStackTrace();
			   System.exit(1);
			}
			
			// Convert DatagramPacket to DataPacket
			DataPacket p = new DataPacket(receivePacket.getData());
			
			System.out.println("Packet is " + p.toString());
			
			System.out.println("SUBSYSTEM IS   " + p.getSubSystem());
			System.out.println("STATE IS   " + Arrays.toString(p.getStatus()));
			
			// at this stage, elevator will decode the packet 
			
			// The elevator will decode the packet
			if(p.getSubSystem() == SubsystemType.MOTOR) {
				//case of motor
				System.out.println("SUBSYSTEM IS MOTOR  " );
				
				switch(MotorState.convertFromByte(p.getStatus()[0])){
					case DOWN:
						System.out.println("going down" );
						Motor(MotorState.DOWN);		//motor down and send message
						break;
					case OFF:
						System.out.println("motor off" );
						Motor(MotorState.OFF);		//motor off
						break;
					case UP:
						System.out.println("going up" );
						Motor(MotorState.UP);		//motor up and send message
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
				sendLocation();
			} else if (p.getSubSystem() == SubsystemType.INPUT){
				System.out.println("SUBSYSTEM IS INPUT");
				setFloorLight(p);
			}
			
			// Echo back the packet
			this.sendPacket(createEchoPacket(p.getSubSystem(), p.getStatus()), receivePacket.getSocketAddress());
			
			//receiveSocket.close();
		}
		
	}
	
	
	// toggle the floor light inside the elevator to on or off
	private void setFloorLight(DataPacket p){
		this.floorLights[(int) p.getStatus()[0]] = !this.floorLights[(int) p.getStatus()[0]];
	}
	
	
	private static DataPacket createEchoPacket(SubsystemType subSystem, byte[] status){
		return new DataPacket(OriginType.ELEVATOR, (byte) 0, subSystem, status);
	}
	
	private void sendPacket(DataPacket p, SocketAddress address){
		sendPacket = new DatagramPacket(p.getBytes(), p.getBytes().length, address);
		
		System.out.println("Elevator: Sending packet...");
		
		try {
		      this.sendSocket.send(this.sendPacket);
		   } catch (IOException e) {
		      e.printStackTrace();
		      System.exit(1);
		   }
		
		   System.out.println("Elevator: packet sent \n");
	}
	
	public void sendLocation() throws IOException {
		
		String [] floornum = {Integer.toString(currentFloor)};
		byte[] location = convertToBytes(floornum);
		//send packet back contains location 
	   sendPacket = new DatagramPacket(location, location.length, receivePacket.getAddress(), receivePacket.getPort());
	   
	   
	   System.out.println( "Elevator: Sending packet to scheduler ");
	    
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
	
	
	public void Motor(MotorState command) throws IOException, InterruptedException{
		System.out.println("Elevator: I am at floor "+ currentFloor);
		
//		while(count != 5) {  //this condition is depends on the packet from scheduler  ***
//			if(command == MotorState.DOWN) {
//				this.motorState = MotorState.DOWN;
//				TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
//				currentFloor--;	
//				sendLocation();
//			} else if (command == MotorState.UP){
//				this.motorState = MotorState.UP;
//				TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
//				currentFloor++;	
//				sendLocation();
//			}
//			
//			System.out.println("Elevator: I am at  "+ currentFloor);
//			count ++;
//		}
		
		if(command == MotorState.DOWN) {
			this.motorState = MotorState.DOWN;
			//TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
			currentFloor--;	
			sendLocation();
		} else if (command == MotorState.UP){
			this.motorState = MotorState.UP;
			//TimeUnit.SECONDS.sleep(3); 		 // sleep for three seconds
			currentFloor++;	
			sendLocation();
		}
		
		System.out.println("Elevator: I am at  "+ currentFloor);
		count ++;
	}
	
	// Depricated
//	public void Motor(int command) throws IOException{
//		System.out.println("Elevator: I am at floor "+ currentFloor);
//		while(count != 5) {  //this condition is depends on the packet from scheduler  ***
//		
//		if(command == 1) {
//			currentFloor--;	
//			sendLocation();
//		}
//		if (command == 3){
//			currentFloor++;	
//			sendLocation();
//		}
//		System.out.println("Elevator: i am at  "+ currentFloor);
//			count ++;
//		}
//		
//	}
	
	public String[] GetString(byte[] bytes) throws ClassNotFoundException, IOException
	{
		// Form a String from the byte array.
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
	
			final String[] stringArray2 = (String[]) objectInputStream.readObject();
	
			objectInputStream.close();
			return stringArray2;   
	}
	
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
	
	public static void main( String args[] ) throws IOException, ClassNotFoundException, InterruptedException {
		while(true) {
			elevator1 c = new elevator1(10);
			c.receiveAndEcho();
		}
	}

}