// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits 
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Arrays;

// To be changed to floor

public class SimpleClient {

	DatagramPacket sendPacket, receivePacket; //instantiates packets and socket
	DatagramSocket sendReceiveSocket;

	public SimpleClient(){
		try {
			// Construct a datagram socket and bind it to any available 
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(10000);//set simple client receive socket to timeout after 10seconds of no input
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	public byte [] messageHeader(int iteration) {//create messageHeader

		byte [] requestBytes = new byte [2];//create new byte array, represents type of request

		if (iteration==11) {//send an invalid request
			requestBytes [0] = 1;
			requestBytes [1] = 1;

		} else if (iteration%2==0) {//on even iterations send a read request
			requestBytes [0] = 0;
			requestBytes [1] = 1;
		} else if (iteration%2==1) {//on odd iterations (except 11) send a write request
			requestBytes [0] = 0;
			requestBytes [1] = 2;
		}

		return requestBytes;
	}

	public void sendAndReceive(){
		for (int i=0;i<=11;i++) {//do 11 times

			byte [] requestHeader = messageHeader(i);//get messageHeader

			String fName = "test.txt";//the file to be sent
			String mode = "octet";//the mode to be sent

			byte fileName[] = null;//Instantiates array to contain binary representation of strings in bytes 
			byte bMode[] = null;
			fileName = fName.getBytes();//encode into bytes
			bMode= mode.getBytes();


			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				output.write(requestHeader);//add all relevant byte arrays into a single stream
				output.write(fileName);
				output.write(0);//separator
				output.write(bMode);
				output.write(0);//separator
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			byte[] message = output.toByteArray();//creates single byte message to be sent

			String s = new String (message, 0, message.length);//string representation of byte message

			// Prepare a DatagramPacket and send it via sendReceiveSocket
			// to port 32 on the intermediate server.
			System.out.println("Client: sending a packet containing:\n" + "(String) " + s + "\n(Bytes) " + Arrays.toString(message));

			// Construct a datagram packet that is to be sent to a specified port on a specified host.
			try {
				sendPacket = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), 23);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}

			//print details of outgoing packet
			System.out.println("Client: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			int len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			System.out.println("(String)" + new String(sendPacket.getData(),0,len));
			System.out.println("(Bytes)" + Arrays.toString(sendPacket.getData()) + "\n");

			// Send the datagram packet to the server via the send/receive socket. 
			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Client: Packet sent.\n");

			// Construct a DatagramPacket for receiving packets up 
			// to 100 bytes long (the length of the byte array).
			byte data[] = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);

			try {
				// Block until a datagram is received via sendReceiveSocket.
				System.out.println("Client: Waiting for Packet.\n");
				sendReceiveSocket.receive(receivePacket);
			} catch(IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n");
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("Client: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			// Form a String from the byte array.
			String received = new String(data,0,len);   
			System.out.println("(String)" + received);
			System.out.println("(Bytes) " + Arrays.toString(data) + "\n");

		}
		// We're finished, so close the socket.
		sendReceiveSocket.close();
	}

	public static void main(String args[]){
		SimpleClient c = new SimpleClient();
		c.sendAndReceive();
	}
}
