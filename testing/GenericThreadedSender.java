package testing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class GenericThreadedSender implements Runnable {
	DatagramSocket sendSocket;
	DatagramPacket sendPacket;
	
	BlockingQueue<DatagramPacket> outputBuffer;
	
	private static int BYTE_ARRAY_LENGTH = 100;
	
	public GenericThreadedSender(BlockingQueue<DatagramPacket> outputBuffer){
		try{
			// Construct a datagram socket to send on
			// used to receive packets
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		this.outputBuffer = outputBuffer;
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
		System.out.println("Data (String): " + p.getData().toString()); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	/**
	 * Constantly try to send the head element in the outputBuffer
	 * @throws InterruptedException
	 */
	private void send() {
		byte[] buf = new byte[BYTE_ARRAY_LENGTH];
		this.sendPacket = new DatagramPacket(buf, buf.length);	// create new packet to receive information in
		
		while(true){
			
			System.out.println("GenericThreadedSender Trying to take a packet from the output queue");
			
			try {
				this.sendPacket = outputBuffer.take();	// Take the packet at the head of the output queue
			} catch (InterruptedException ie){
				System.err.println(ie);
				System.exit(0);
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
