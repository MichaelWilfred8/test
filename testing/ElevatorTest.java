package testing;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;



/**
 * Class used to test the function of the Elevators
 * 
 * @author Hongbo Pang
 */
public class ElevatorTest {

	DatagramPacket sendPacket, receivePacket, EchoPacket;
	DatagramSocket sendSocket, receiveSocket, EchoSocket ;
	int count;

	public ElevatorTest()
	{
	   try {

	      sendSocket = new DatagramSocket();

	   } catch (SocketException se) {
	      se.printStackTrace();
	      System.exit(1);
	   } 
	}
	
	public void receiveAndForward() throws InterruptedException, IOException, ClassNotFoundException
	{
	   // Construct a DatagramPacket for receiving packets

		byte[] data = {(byte) 2,(byte)2,(byte)7,(byte)3};
		
	    
	   //send to server

	   sendPacket = new DatagramPacket(data, data.length,
			   InetAddress.getLocalHost(),68);

	   System.out.println( "scheduler: sent ");

	   // Send the datagram packet to the Elevator via the send socket. 
	   try {
	      sendSocket.send(sendPacket);
	   } catch (IOException e) {
	      e.printStackTrace();
	      System.exit(1);
	   }

	   sendSocket.setSoTimeout(10000); //set time out
	   
	   //Receive done packet from server
	   while(true) {
	   byte data1[] = new byte[5];
	   EchoPacket = new DatagramPacket(data1,data1.length);
	      try {
	          // Block until a datagram is received via sendReceiveSocket.  
	    	  
	    	  sendSocket.receive( EchoPacket);
	       } catch(IOException e) {
	          e.printStackTrace();
	          System.out.println("Timeout reached!!! " + e);
	          sendSocket.close();
	          System.exit(1);
	       }
	   System.out.println("Waiting for current location from ELevator"); // so we know we're waiting
	   int floorNum  = EchoPacket.getData()[1];
	   System.out.println("elevator is on " + floorNum + " floor. \n");

	  
	   
	   }
 	  
	
	}
	

	public String[] GetString(byte[] bytes) throws ClassNotFoundException, IOException
	{
		// Form a String from the byte array.
		final ByteArrayInputStream byteArrayInputStream =
			    new ByteArrayInputStream(bytes);
			final ObjectInputStream objectInputStream =
			    new ObjectInputStream(byteArrayInputStream);

			final String[] stringArray2 = (String[]) objectInputStream.readObject();

			objectInputStream.close();
			return stringArray2;   
	}
	
	private static byte[] convertToBytes(String[] strings) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ObjectOutputStream objectOutputStream =
		new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(strings);
		objectOutputStream.flush();
		objectOutputStream.close();

		final byte[] data = byteArrayOutputStream.toByteArray();

		return data;
	}
	

	public static void main( String args[] ) throws InterruptedException, IOException, ClassNotFoundException
	{
		ElevatorTest c = new ElevatorTest();
		c.receiveAndForward();
	}
	
}
