package shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import Enums.OriginType;
import Enums.SubsystemType;

// TODO: Have GenericThreadedSender use DataPacket output queue to send info?

public class GenericThreadedSender implements Runnable {
	DatagramSocket sendSocket;
	DatagramPacket sendPacket;
	
	//BlockingQueue<DatagramPacket> outputBuffer;
	BlockingQueue<DataPacket> outputBuffer;
	
	private static int BYTE_ARRAY_LENGTH = 100;
	private SocketAddress elevatorAddress;
	private SocketAddress schedulerAddress;
	private SocketAddress floorAddress;
	
	
	/**
	 * Constructor for GenericThreadedSender object. Used to send objects stored in outputBuffer to other handlers.
	 * 
	 * @param outputBuffer		The output buffer shared by the handler and the sender
	 * @param elevatorAddress	The SocketAddress of the elevator handler
	 * @param schedulerAddress	The SocketAddress of the scheduler handler
	 * @param floorAddress		The SocketAddress of the floor handler
	 */
	public GenericThreadedSender(BlockingQueue<DataPacket> outputBuffer, SocketAddress elevatorAddress, SocketAddress schedulerAddress, SocketAddress floorAddress){
		try{
			// Construct a datagram socket to send on
			// used to receive packets
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		this.outputBuffer = outputBuffer;
		
		this.floorAddress = floorAddress;
		this.elevatorAddress = elevatorAddress;
		this.schedulerAddress = schedulerAddress;
	}
	
	
	
	/**
	 * Print what was received from a datagram packet to the console
	 *
	 * @param p		datagram packet that was received
	 * @param mode	String representing if a packet was sent ("s") or received ("r")
	 */
	private static void printDatagramPacket(DatagramPacket p, String mode){
		if (mode == "s"){
			System.out.println("GenericThreadedSender sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "r") {
			System.out.println("GenericThreadedSender received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		System.out.println("Data (String): " + p); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	/**
	 * Constantly try to send the head element in the outputBuffer
	 * @throws InterruptedException
	 */
	private void send() {
		byte[] buf = new byte[BYTE_ARRAY_LENGTH];
		this.sendPacket = new DatagramPacket(buf, buf.length);	// create new packet to send information in
		DataPacket tempPacket = null;
		
		while(true){
			// Get the data to send from the output buffer
			System.out.println("GenericThreadedSender Trying to take a packet from the output queue");
			try {
				tempPacket = new DataPacket(outputBuffer.take());	// Take the packet at the head of the output queue
			} catch (InterruptedException ie){
				System.err.println(ie);
				System.exit(0);
			}
			
			// Set the data in sendPacket to the DataPacket taken from the queue
			this.sendPacket.setData(tempPacket.getBytes());
			
			// Find the destination address of the DataPacket
			if ((tempPacket.getSubSystem() == SubsystemType.DOOR) || (tempPacket.getSubSystem() == SubsystemType.MOTOR) || (tempPacket.getSubSystem() == SubsystemType.LOCATION)){
				if (tempPacket.getOrigin() == OriginType.SCHEDULER){		// If the data is for an elevator and originated from the scheduler
					this.sendPacket.setSocketAddress(elevatorAddress);		// Send to the elevatorHandler
					try {
						this.sendPacket.setAddress(InetAddress.getLocalHost());
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (tempPacket.getOrigin() == OriginType.ELEVATOR){	// If the data if for an elevator and originated from an elevator
					this.sendPacket.setSocketAddress(schedulerAddress);		// Send to the scheduler handler
					try {
						this.sendPacket.setAddress(InetAddress.getLocalHost());
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if ((tempPacket.getSubSystem() == SubsystemType.REQUEST) || (tempPacket.getSubSystem() == SubsystemType.FLOORLAMP)){
				if (tempPacket.getOrigin() == OriginType.FLOOR){				// If the data is for a floor and originated from a floor
					this.sendPacket.setSocketAddress(schedulerAddress);			// Send to the scheduler
					try {
						this.sendPacket.setAddress(InetAddress.getLocalHost());
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (tempPacket.getOrigin() == OriginType.SCHEDULER){		// If the data is for a floor and originated from an elevator
					this.sendPacket.setSocketAddress(floorAddress);				// Send to the floor handler
					try {
						this.sendPacket.setAddress(InetAddress.getLocalHost());
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// Print the packet to be sent
			printDatagramPacket(this.sendPacket, "s");
			
			
			// Try to send the packet
			try {
				System.out.println("GenericThreadedSender: Sending on port " + this.sendSocket.getLocalPort() + ".\n");
				this.sendSocket.send(this.sendPacket);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		System.out.println("Starting sender");
		send();
	}
}
