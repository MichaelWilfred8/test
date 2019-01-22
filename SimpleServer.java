// SimpleEchoServer.java
// This class is the server side of a simple echo server based on
// UDP/IP. The server receives from a client a packet containing a character
// string, then echoes the string back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class SimpleServer {

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	public SimpleServer()
	{
		try {
			//sendSocket = new DatagramSocket();

			// Construct a datagram socket and bind it to port 69 on the local host machine. 
			receiveSocket = new DatagramSocket(69);

		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
	}

	public byte [] validate(byte [] data) {//validates incoming messages

		byte [] returnBytes = new byte [4];//create new byte array, represents type of request
		try {
			if(data[0] != 0) {
				throw new RuntimeException();
			}else if (data[1] == 1) {//check if second bit indicates write request
				returnBytes[0] = 0;
				returnBytes[1] = 3;
				returnBytes[2] = 0;
				returnBytes[3] = 1;
			}else if(data[1] == 2) {//check if second bit indicates read request
				returnBytes[0] = 0;
				returnBytes[1] = 4;
				returnBytes[2] = 0;
				returnBytes[3] = 0;
			}
		} catch (RuntimeException e) {
			System.out.println("Invalid Request:");
			e.printStackTrace();
			System.exit(1);
		}

		return returnBytes;
	}

	public void receiveAndRespond()
	{
		while (true) {
			// Construct a DatagramPacket for receiving packets up 
			// to 100 bytes long (the length of the byte array).

			byte data[] = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			System.out.println("Server: Waiting for Packet.\n");

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
			System.out.println("Server: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			int len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.print("Containing: " );

			// Form a String from the byte array.
			String received = new String(data,0,len);   
			System.out.println("(String)" + received);
			System.out.println("(bytes)" + Arrays.toString(data) + "\n");

			// Slow things down (wait 1 second)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}

			byte[] returnBytes = validate(data);

			// Create a new datagram packet containing the response
			sendPacket = new DatagramPacket(returnBytes, returnBytes.length,
					receivePacket.getAddress(), 23);

			//print outgoing packet
			System.out.println( "Server: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.print("Containing: ");
			System.out.println("(String) " +new String(sendPacket.getData(),0,len));
			System.out.println("(Bytes) " + Arrays.toString(sendPacket.getData()) + "\n");
			// or (as we should be sending back the same thing)
			// System.out.println(received); 

			//create the socket to send the response packet
			try {
				sendSocket = new DatagramSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			};

			if (sendSocket == null) break;

			// Send the datagram packet to the client via the send socket. 
			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Server: packet sent\n");

			// We're finished, so close the sockets.
			sendSocket.close();
		}
		receiveSocket.close();
	}

	public static void main( String args[] )
	{
		SimpleServer c = new SimpleServer();
		c.receiveAndRespond();
	}
}

