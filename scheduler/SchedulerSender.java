package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Threaded class used to try and constantly send packets to their destination
 * 
 * @author Craig Worthington
 *
 */
public class SchedulerSender implements Runnable{
	
	DatagramSocket sendSocket;
	DatagramPacket sendPacket;
	
	PriorityBlockingQueue<TimestampedPacket> outputBuffer;
	
	private static int BYTE_ARRAY_LENGTH = 100;
	
	public SchedulerSender(PriorityBlockingQueue<TimestampedPacket> outputBuffer){
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
			System.out.println("SchedulerSender sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == "r") {
			System.out.println("SchedulerSender received:");
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		System.out.println("Data (String): " + new DataPacket(p.getData()).toString()); // Print the data in the packet as a String
		System.out.println("Data (bytes): " + Arrays.toString(p.getData()) + "\n");		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	/**
	 * Constantly try to send the head element in the outputBuffer
	 * @throws InterruptedException
	 */
	private void send() throws InterruptedException{
		byte[] buf = new byte[BYTE_ARRAY_LENGTH];
		this.sendPacket = new DatagramPacket(buf, buf.length);	// create new packet to receive information in
		TimestampedPacket tempPacket;
		
		// TODO: Find how to access information from SchedulerHandler inside of this thread
		while(true){
			synchronized(outputBuffer){
				while(outputBuffer.isEmpty()){
					outputBuffer.wait();
				}
				tempPacket = outputBuffer.take();
			}
			
			// get underlying datagram packet from tempPacket
			this.sendPacket = tempPacket.getPacket();
			
			// Print the packet to be sent
			printDatagramPacket(this.sendPacket, "s");
			
			// Try to send the packet
			try {
				this.sendSocket.send(this.sendPacket);
			} catch (IOException e){
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		try {
			send();
		} catch (InterruptedException e) {
			System.err.println(e);
			System.exit(0);
		}
	}
}
