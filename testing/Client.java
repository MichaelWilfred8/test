package testing;

/*
the client creates a DatagramSocket to use to both send and receive 
repeat the following 11 times:
the client creates a DatagramPacketthe packet is either a "read request" or a "write request" (alternate between read and write requests, five each) with #11 an invalid request
read request format:
first two bytes are 0 and 1 (these are binary, not text)
then there's a filename converted from a string to bytes (e.g. test.txt)
then a 0 byte
then a mode (netasciior octet, any mix of cases, e.g. ocTEt) converted from a string to bytes
finally another 0 byte (and nothing else after that!)
write request format:
just like a read request, except it starts with 0 2 instead of 0 1
the client prints out the information it has put in the packet (print the request both as a String and as bytes)
the client sends the packet to a well-known port: 23 on the intermediate host 
the client waits on its DatagramSocket
when it receives a DatagramPacket from the intermediate host, it prints out the information received, including the byte array
 */

import java.net.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import shared.*;

public class Client {
	DatagramSocket sendReceiveSocket;
	DatagramPacket sendPacket;
	//DatagramPacket receivePacket;
	DataPacket receivePacket;
	
	BlockingQueue<DataPacket> inputBuffer;
	//BlockingQueue<DatagramPacket> inputBuffer;
	BlockingQueue<DatagramPacket> outputBuffer;
	
	
	private static final int RECEIVE_PORT = 100;
	private static final int SEND_PORT = 104;
	private static final int DEST_PORT_NUM = 69;	// constant value for the port number of the destination server
	private static final int DATA_LEN = 256;		// constant value for initializing the data byte array
	private static final String RECEIVED = "r"; 	// constant value representing a received DatagramPacket for the printDatagramPacket method
	private static final String SENT = "s";			// constant value representing a received DatagramPacket for the printDatagramPacket method
	private static final String READ_REQ = "r";		// constant value representing a read request
	private static final String WRITE_REQ = "w";	// constant value representing a write request
	
	
	/*
	 * Constructor for objects of class Client
	 */
	public Client(){
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
		
		this.inputBuffer = new ArrayBlockingQueue<DataPacket>(21);
		//this.inputBuffer = new ArrayBlockingQueue<DatagramPacket>(21);
		//this.outputBuffer = new ArrayBlockingQueue<DataPacket>(21);
		this.outputBuffer = new ArrayBlockingQueue<DatagramPacket>(21);
	}
	
	/*
	 * Builds a byte array from a give ArrayList
	 * 
	 * @param	l	An Arraylist of Bytes to be converted to an array of primitive type bytes
	 * @return		An array of bytes
	 */
	private static byte[] listToArray(ArrayList<Byte> l){
		byte[] a = new byte[l.size()]; 		// create a new array of the same size as the given list to store the bytes
		
		for (int i = 0; i < l.size(); ++i){	// loop through each element in the list and store it in the new array
			a[i] = l.get(i);
		}
		return a;
	}
	
	
	/*
	 * Builds a string of hex characters
	 * 
	 * @param 	bytes 	an array of bytes to be converted
	 * @return			a string of hex characters, with a space separating each byte
	 */
	private static String buildHexString(byte[] bytes){
		StringBuilder sb = new StringBuilder();		// Create a StringBuilder to contain the new String of hex characters
		for (byte b : bytes){
			sb.append(String.format("%02X ", b)); 	// Add each hex character in the string to the StringBuilder
		}
		return sb.toString();
	}
	
	
	/*
	 * Trims the length of the byte array data in a DatagramPacket down to its specified length
	 * 
	 * @param	p	a DatagramPacket with a byte array that is too long
	 */
	private static void trimToLength(DatagramPacket p) {	
		byte[] trimmed = new byte[p.getLength()]; 	// create a byte array of the length specified in the DatagramPacket
		
		for(int i = 0; i < p.getLength(); ++i){
			trimmed[i] = p.getData()[i];			// copy the data from the DatagramPacket array into the new array
		}
		
		p.setData(trimmed, 0, p.getLength());  		// set the data in the DatagramPacket to the new trimmed array
	}
	
	/*
	 * Prints the information in the DatagramPacket to the console
	 * 
	 * @param 	p		the DatagramPacket whose information is to be printed
	 * @param	mode 	Constant that specifies if the DatagramPacket was sent or received by this host
	 */
	private static void printDatagramPacket(DatagramPacket p, String mode){
		if (mode == SENT){
			System.out.println("Client sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == RECEIVED) {
			System.out.println("Client received:");		
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		String data = new String(p.getData(), 0, p.getLength());				// Create new string from data in DatagramPacket 
		System.out.println("Data (String): " + data); 							// Print the data in the packet as a String
		System.out.println("Data (bytes): " + buildHexString(p.getData()));		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	
	/* Create a correctly formatted request to send to the server
	 * 
	 * Read request format:
	 * first two bytes are 0 and 1 (these are binary, not text)
	 * then there's a filename converted from a string to bytes (e.g. test.txt)
	 * then a 0 byte
	 * then a mode (netascii or octet, any mix of cases, e.g. ocTEt) converted from a string to bytes
	 * finally another 0 byte (and nothing else after that!)
	 * 
	 * Write request format:
	 * First two bytes are 0 and 1
	 * Remaining parts request follows the same format as the read request above
	 * 
	 * @param	format		A string that indicates the format of the request (read, write or invalid)
	 * 			filename	A string containing the filename to be encoded
	 * 			mode		A string containing the name of the format used to encode the characters 
	 * @return				A byte array that contains the information in the correct format
	 */
	private static byte[] createRequest(String format, String filename, String mode){
		ArrayList<Byte> data = new ArrayList<Byte>(); 	// create an ArrayList to contain the bytes as the array is being constructed
		
		data.add((byte) 0x00); 				// Add 0 byte at beginning (for all requests)
		
		if (format == READ_REQ){ 			// If the request is to be a read request
			data.add((byte) 0x01); 			// Set the second byte to 0x01
		} else if (format == WRITE_REQ){ 	// If the request is to be a write request
			data.add((byte) 0x02); 			// Set the second byte to 0x02
		} else {							// If the request is neither read or write
			data.add((byte) 0xFF); 			// Set the second byte to a random value (for invalid request)
		}
		
		// Convert the filename from a String to a byte array
		byte[] fileBytes = filename.getBytes();
		for (int i = 0; i < fileBytes.length; ++i){
			data.add(fileBytes[i]);
		}
		
		data.add((byte) 0x00); // Add a 0 byte between the filename and the mode
		
		// TODO: add other formatting??
		// Convert the mode from a String to bytes
		byte[] modeBytes = mode.getBytes();
		for (int i = 0; i < modeBytes.length; ++i){
			data.add(modeBytes[i]);
		}
		
		// Add the final zero byte to the request
		data.add((byte) 0x00); 
		return listToArray(data);	// Convert the ArrayList to a byte array and return that
	}
	
	/**
	 * Main algorithm for the Client server
	 * 
	 * @param 	requestType A string that indicates the format of the request (read, write or invalid)
	 * 			filename	A string containing the filename to be encoded
	 * 			mode		A string containing the name of the format used to encode the characters 
	 */
	private void send(String requestType, String filename, String mode){
		byte[] request = createRequest(requestType, filename, mode);
		
		// The Client creates a DatagramPacket to send to the destination port on the local host
		try {
			this.sendPacket = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), DEST_PORT_NUM);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Put the packet in the output buffer
		try {
			this.outputBuffer.put(this.sendPacket);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Sleep for half a second
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
		
		
	
	
	private void receive(){
		try {
			System.out.println("Client Trying to take from inputBuffer");
			System.out.println("inputBuffer = " + this.inputBuffer.toString());
			//System.out.println("receivePacket = " + this.receivePacket.toString());
			
			// create the new info for the datagrampacket at the head of the queue
			this.receivePacket = new DataPacket(this.inputBuffer.take());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("inputBuffer = " + this.inputBuffer.toString());
		System.out.println("receivePacket = " + this.receivePacket.toString());
		System.out.println("packet info " + this.receivePacket.getOrigin());
		System.out.println("sucessfully retrieved packet\n\n");
	}
	
	
	/**
	 * Loops through the Client algorithm 11 times, selecting different types of requests each time
	 */
	private void clientLoop(){
		String filename = "textDocument.txt";
		String mode = "ocTet";
		
		// Loop through the Client Server Algorithm 11 times, alternating between read and write requests (five each) with #11 an invalid request
		for (int i = 0; i < 11; ++i){
			if (i == 10){ 									
				this.send(READ_REQ, filename, "BadMode");		// on the 11th iteration of the loop, create an invalid request
			} else if (i % 2 == 1){ // read requests
				this.send(READ_REQ, filename, mode);	// create a read request on odd iterations of the loop
			} else if (i % 2 == 0){ // write requests
				this.send(WRITE_REQ, filename, mode);	// create a write request on even iterations of the loop
			} 
		}
		
		for(int i = 0; i < 11; ++i){
			this.receive();
		}
	}
	
	public static void main(String args[]) throws IOException{
		Client c = new Client();
		Thread sender = new Thread(new GenericThreadedSender(c.outputBuffer));
		Thread receiver = new Thread(new GenericThreadedListener(c.inputBuffer, RECEIVE_PORT));
		
		System.out.println("Client Console:");
		
		sender.start();
		receiver.start();
		
		c.clientLoop();
	}
}

