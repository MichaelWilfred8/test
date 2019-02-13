package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Threaded class used to constantly listen for inputs to the scheduler from other subsystems.
 * Adds any received packets to the inputBuffer
 * 
 * @author Craig Worthington
 *
 */
public class SchedulerListener implements Runnable {
	
	DatagramSocket receiveSocket;
	DatagramPacket receivePacket;
	
	PriorityBlockingQueue<TimestampedPacket> inputBuffer;
	
	private static int BYTE_ARRAY_LENGTH = 100;
	
	public SchedulerListener(PriorityBlockingQueue<TimestampedPacket> inputBuffer){
		try{
			// Construct a datagram socket to receive on and bind it to port 23 on the local host machine.
			// used to receive packets
			receiveSocket = new DatagramSocket(23);
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
			System.out.println("Scheduler sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "r") {
			System.out.println("Scheduler received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		System.out.println("Data (String): " + new DataPacket(p.getData()).toString()); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	private void listen() throws InterruptedException{
		byte[] buf = new byte[BYTE_ARRAY_LENGTH];
		receivePacket = new DatagramPacket(buf, buf.length);	// create new packet to receive information in
		
		while(true){
			try {
				// Block until a datagram is received via sendReceiveSocket.
				System.out.println("SchedulerListener: Waiting for Packet.\n");
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			printDatagramPacket(receivePacket, "r");
			
			
			synchronized(inputBuffer){
				while(inputBuffer.offer(new TimestampedPacket(this.receivePacket)) == false){
					inputBuffer.wait();
				}
				notifyAll();
			}
		}
	}
	
	public void run(){
		try {
			listen();
		} catch (InterruptedException e){
			System.err.println(e);
			System.exit(0);
		}
	}
}
