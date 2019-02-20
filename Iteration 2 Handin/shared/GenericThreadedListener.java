package shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import shared.*;

/**
 * Threaded class used to constantly listen for inputs to the scheduler from other subsystems.
 * Adds any received packets to the inputBuffer
 * 
 * @author Craig Worthington
 *
 */
public class GenericThreadedListener implements Runnable {
	
	DatagramSocket receiveSocket;
	DatagramPacket receivePacket;
	
	DataPacket packet;
	
	//BlockingQueue<DatagramPacket> inputBuffer;
	BlockingQueue<DataPacket> inputBuffer;
	
	private static final int BYTE_ARRAY_LENGTH = 100;
	
	public GenericThreadedListener(BlockingQueue<DataPacket> inputBuffer, int port){
		try{
			// Construct a datagram socket to receive on and bind it to port on the local host machine.
			// used to receive packets
			receiveSocket = new DatagramSocket(port);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
		this.inputBuffer = inputBuffer;
	}
	
	
	/**
	 * Print what was received from a datagram packet to the console
	 *
	 * @param p		datagram packet that was received
	 * @param mode	String representing if a packet was sent ("s") or received ("r")
	 */
	private static void printDatagramPacket(DatagramPacket p, String mode){
		if (mode == "s"){
			System.out.println("GenericThreadedListener sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "r") {
			System.out.println("GenericThreadedListener received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		System.out.println("Data (String): " + p); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	/**
	 * Listen constantly to receive a new packet and add it to the given input queue
	 */
	private void listen() {
		byte[] buf = new byte[BYTE_ARRAY_LENGTH];
		receivePacket = new DatagramPacket(buf, buf.length);	// create new packet to receive information in
		
		
		while(true){
			try {
				// Block until a datagram is received via sendReceiveSocket.
				System.out.println("GenericThreadedListener: Waiting for Packet on port " + this.receiveSocket.getLocalPort() + ".\n");
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			// Print the Received DatagramPacket to the console
			printDatagramPacket(receivePacket, "r");
			
			
			
			// Convert the DatagramPacket into a DataPacket
			DataPacket p = new DataPacket(this.receivePacket.getData());
			
			// Try to add to DataPacket the queue
			try {
				inputBuffer.put(p);
				//System.out.println("GOT THIS MESSAGE " + p);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("GenericThreadedListener successfully added info to the inputBuffer");
		}
	}
	
	
	/** 
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		System.out.println("Starting receiver");
		listen();
	}
}
