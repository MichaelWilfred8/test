
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

import Enums.DoorState;
import Enums.MotorState;

public class Scheduler {

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendRecieveSocket, receiveSocket;
	
	private ElevatorStatus carStatus;	// Information about the status of an elevator car
	
	private static final int MAX_FLOOR = 10;
	private static final int MIN_FLOOR = 1;
	
	private ArrayList<Integer> upRequests;		// ArrayList for holding all requests from an elevator to move from its current position up
	private ArrayList<Integer> downRequests;	// ArrayList for holding all requests from an elevator to move from its current position down

	public Scheduler(){//TODO:make it a singleton?
		try {
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
		
		this.carStatus = new ElevatorStatus(MIN_FLOOR, MotorState.OFF, DoorState.CLOSED, MAX_FLOOR);	// Have an elevator starting on the bottom floor of the building with the door closed and the motor off
	}
	
	
	
	/**
	 * @return Top Level of building
	 */
	public int getTopFloor() {
		return MAX_FLOOR;
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
	 * @param p	DataPacket request from the floor. Parse the request and assign the request to the appropriate elevator
	 */
	private void addRequest(DataPacket p){
		
	}

}

