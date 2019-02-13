package testing;


import java.net.*;
import java.io.IOException;

public class Server {
	
	DatagramSocket sendSocket, receiveSocket;
	DatagramPacket sendPacket, receivePacket;
	private static final int RECEIVE_PORT_NUM = 69;	// constant value for the port number of the receiveSocket
	private static final int DATA_LEN = 256;		// constant value for initializing the data byte array
	private static final String RECEIVED = "r"; 	// constant value representing a received DatagramPacket for the printDatagramPacket method
	private static final String SENT = "s";			// constant value representing a received DatagramPacket for the printDatagramPacket method
	private static final int FORMAT_BYTE_INDEX = 1;	// constant value of the index of the format byte in the request
	private static final byte READ_BYTE = 0x01;		// constant byte value of a valid read request
	private static final byte WRITE_BYTE = 0x02;	// constant byte value of a valid write request 
	private static final byte[] VALID_READ_RETURN = {0x00, 0x03, 0x00, 0x01};	// constant byte array to be returned when a valid read request is received
	private static final byte[] VALID_WRITE_RETURN = {0x00, 0x04, 0x00, 0x00};	// constant byte array to be returned when a valid write request is received
	private static final String OCTET_FORMAT = "octet";			// constant string value for valid octet mode
	private static final String NETASCII_FORMAT = "netascii"; 	// constant string value for valid netascii mode
	private static final int DEST_PORT = 100;
	
	
	/*
	 * Constructor for objects of class Server
	 */
	public Server(){
		try{
			receiveSocket = new DatagramSocket(RECEIVE_PORT_NUM); // create a new DatagramSocket on port 69 to receive information
		} catch (SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
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
			System.out.println("Server sent:");
			System.out.println("To host: " + p.getAddress());					// Print address of host to which DatagramPacket was sent
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		} else if (mode == RECEIVED) {
			System.out.println("Server received:");		
			System.out.println("From host: " + p.getAddress());					// Print address of host to which DatagramPacket was received
			System.out.println("Host port: " + p.getPort());					// Print port of host to which DatagramPacket was sent
		}
		System.out.println("Length: " + p.getLength());							// Print length of data in DatagramPacket
		String data = new String(p.getData(), 0, p.getLength());				// Create new string from data in DatagramPacket 
		System.out.println("Data (String): " + data); 							// Print the data in the packet as a String
		System.out.println("Data (bytes): " + buildHexString(p.getData()));		// Print the data in the packet as hex bytes
		System.out.println();
	}
	
	/*
	 * Parses the information contained in a DatagramPacket to determine if it is of valid format
	 * 
	 * @param	p 	DatagramPacket to be parsed
	 * @throws		an InvalidFormatException is thrown if the data in the DatagramPacket is not of a valid format
	 */
	private void isPacketValid (DatagramPacket p) throws InvalidFormatException {
		byte[] buf = p.getData();	// Create a byte array to contain the data in the DatagramPacket
		
		// Check if first byte in the data is 0x00
		if (buf[0] != 0x00){
			throw new InvalidFormatException("First byte is not 0x00");
		}
		
		// Check if the format byte is a valid read byte (1) or write byte (2)
		if ((buf[FORMAT_BYTE_INDEX] != READ_BYTE) && (buf[FORMAT_BYTE_INDEX] != WRITE_BYTE)){
			throw new InvalidFormatException("Format byte " + Integer.toHexString(buf[FORMAT_BYTE_INDEX] & 0xFF) + " is not valid read or write byte");
		}
		
		// Check if last byte in the data is a zero
		if (buf[buf.length-1] != 0x00){
			throw new InvalidFormatException("Final byte is not 0x00");
		}
		
		// Check if strings are valid
		// Loop through all text characters to find the 0x00 separator between the file name and mode
		boolean foundZeroByte = false;
		int separatorIndex = 0;
		
		for (int i = 2; i < p.getLength() - 1; i++){
			if ((buf[i] == 0x00) && foundZeroByte == true){
				System.out.println("Second zero byte found, format is invalid");
				throw new InvalidFormatException("Extra 0x00 separator byte found"); // If a second 0x00 separator byte is found, throw an expection
			} else if (buf[i] == 0x00 && foundZeroByte == false){
				foundZeroByte = true; // zero byte found, format is valid
				separatorIndex = i;
			}
		}
		
		if (foundZeroByte == false){
			throw new InvalidFormatException("Zero byte separator between file name and mode was not found");
		}
		
		// TODO: debug this and find correct formatting
		// Check if mode is of correct format
		StringBuilder sb = new StringBuilder();		// create a string builder to store the mode string inside
		for(int i = separatorIndex+1; i < p.getLength()-1; ++i){ 	// iterate through the string from the index of the separator until the end of the data
			sb.append((char)p.getData()[i]);	// cast the byte to a char and append each character from the mode section to the string builder
		}
		String mode = sb.toString(); 	// convert the StringBuilder to a string
		mode = mode.toLowerCase(); 		// convert the string to all lower case characters
		
		if ((!mode.equals(OCTET_FORMAT)) && (!mode.equals(NETASCII_FORMAT))){
			throw new InvalidFormatException("Mode given by data string was did not match any valid modes");
		}
	}
	
	
	/*
	 * The main algorithm for the Server
	 */
	private void sendAndReceive(){
		byte[] data = new byte[DATA_LEN]; 	// create an empty byte array to store information in the DatagramPacket
		
		// Loop through the server algorithm indefinitely
		while (true) {
			this.receivePacket = new DatagramPacket(data, data.length);		// create a DatagramPacket to store the data to be received
			
			// The Server waits to receive the data
			try {
				System.out.println("Waiting to receive...");
				this.receiveSocket.receive(this.receivePacket); 	// Wait to receive a DatagramPacket through the Server's receiveSocket and store it in receivePacket
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			
			// The server prints out the information it has received 
			trimToLength(this.receivePacket); 					// trim the byte array in receivePacket to the correct length
			printDatagramPacket(this.receivePacket, RECEIVED);	// Print the information in the datagramPacket to the console
			
			// Parse the data to determine if the format is valid
			// If the packet is invalid throw an exception and quit
			try {
				this.isPacketValid(this.receivePacket);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			// Create a DatagramPacket to return the information to the SocketAddress from the received packet
			// Initialize sendPacket with the data to be returned for a valid read request
			this.sendPacket = new DatagramPacket(VALID_READ_RETURN, VALID_READ_RETURN.length, this.receivePacket.getSocketAddress());
			
			this.sendPacket.setPort(DEST_PORT);
			
			
			if (this.receivePacket.getData()[FORMAT_BYTE_INDEX] == READ_BYTE){
				this.sendPacket.setData(VALID_READ_RETURN); 	// If the DatagramPacket received is a valid read request, return 0 3 0 1
			} else if (this.receivePacket.getData()[FORMAT_BYTE_INDEX] == WRITE_BYTE){
				this.sendPacket.setData(VALID_WRITE_RETURN); 	// if the DatagramPacket received is a valid write request, return 0 4 0 0
			}
			
			System.out.println("Sleep for one second");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// The server prints out the information contained in the response DatagramPacket
			printDatagramPacket(this.sendPacket, SENT);
			
			
			// The server creates a new DatagramSocket to use just for this response
			try {
				this.sendSocket = new DatagramSocket();
			} catch (SocketException se) {
				se.printStackTrace();
				System.exit(1);
			}
			
			// The server sends sendPacket as a response through the newly created DatagramSocket
			try {
				this.sendSocket.send(this.sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			// The server closes the new DatagramSocket
			this.sendSocket.close();
		}
	}
	
	
	public static void main(String args[]) throws IOException{
		Server s = new Server();
		
		System.out.println("Server Console:");
		
		s.sendAndReceive();
	}
}

