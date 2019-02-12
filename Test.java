package ElevatorClass;


import java.io.*;
import java.net.*;


public class Test {

	DatagramPacket sendPacket, receivePacket, EchoPacket;
	DatagramSocket sendSocket, receiveSocket, EchoSocket ;

	public Test()
	{
	   try {

	      sendSocket = new DatagramSocket();

	   } catch (SocketException se) {
	      se.printStackTrace();
	      System.exit(1);
	   }
	}

	public void receiveAndForward() throws InterruptedException, UnknownHostException, UnsupportedEncodingException
	{
	   // Construct a DatagramPacket for receiving packets
	   byte data[] = {5};

	   //send to server

	   sendPacket = new DatagramPacket(data, data.length,
			   InetAddress.getLocalHost(),69);

	   System.out.println( "scheduler: send command from 5th floor");

	   // Send the datagram packet to the Elevator via the send socket.
	   try {
	      sendSocket.send(sendPacket);
	   } catch (IOException e) {
	      e.printStackTrace();
	      System.exit(1);
	   }


	   //Receive done packet from server
	   byte data1[] = new byte[100];
	   EchoPacket = new DatagramPacket(data1,data1.length);
	      try {
	          // Block until a datagram is received via sendReceiveSocket.

	    	  sendSocket.receive( EchoPacket);
	       } catch(IOException e) {
	          e.printStackTrace();
	          System.exit(1);
	       }
	   System.out.println("Waiting for echo packet from ELevator"); // so we know we're waiting
	   System.out.println("Scheduler: echo Packet received from Elevator");
	   System.out.print("Containing: " );
	   String stringData =GetString(sendPacket.getData());
	   System.out.println(stringData);
	}


	public String GetString(byte[] bytes)
	{
	// Form a String from the byte array.
	String file_string = "";

    for(int i = 0; i < bytes.length; i++)
    {
        file_string += (char)bytes[i];
    }

    return file_string;
	}


	public static void main( String args[] ) throws InterruptedException, UnknownHostException, UnsupportedEncodingException
	{
		Test c = new Test();
		c.receiveAndForward();
	}
}
