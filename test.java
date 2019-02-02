package elevator1;


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




public class test {

	DatagramPacket sendPacket, receivePacket, EchoPacket;
	DatagramSocket sendSocket, receiveSocket, EchoSocket ;
	int count;

	public test()
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
		String[] strings = {"2","0","3","1"};
		byte[] data = convertToBytes(strings);
		
	    
	   //send to server

	   sendPacket = new DatagramPacket(data, data.length,
			   InetAddress.getLocalHost(),69);

	   System.out.println( "scheduler: sent " +Arrays.toString(strings));

	   // Send the datagram packet to the Elevator via the send socket. 
	   try {
	      sendSocket.send(sendPacket);
	   } catch (IOException e) {
	      e.printStackTrace();
	      System.exit(1);
	   }

	   
	   
	   //Receive done packet from server
	   while(count!=5) {
	   byte data1[] = new byte[100];
	   EchoPacket = new DatagramPacket(data1,data1.length);
	      try {
	          // Block until a datagram is received via sendReceiveSocket.  
	    	  
	    	  sendSocket.receive( EchoPacket);
	       } catch(IOException e) {
	          e.printStackTrace();
	          System.exit(1);
	       }
	   System.out.println("Waiting for current location from ELevator"); // so we know we're waiting
	   String[] Ehco = GetString(EchoPacket.getData());
	   System.out.println("elevator is on " + Ehco[0] );

	  
	   count ++;
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
		test c = new test();
		c.receiveAndForward();
	}
	
}