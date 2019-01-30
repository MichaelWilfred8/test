// This is elevator class, it will keep running and act like server
// The logic is following
// 1. waiting a packet contain floor number
// 2. run the motor logic according to floor number 
// 3. send a packet back contains [OK/fail, DestinationFloorNum]
// 4. run the motor logic 
// 5. once currentFloor = DestinationFloor, send a [OK/fail] packet

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Elevator {

DatagramPacket sendPacket, receivePacket;
DatagramSocket sendSocket, receiveSocket;
int currentFloor = 1;
int DesFloor;
boolean door = false;

public Elevator()
{
   try {

      sendSocket = new DatagramSocket();

      // Construct a datagram socket and bind it to port 5000 
      // on the local host machine. This socket will be used to
      // receive UDP Datagram packets.
      receiveSocket = new DatagramSocket(69);
      
      // to test socket timeout (2 seconds)
      //receiveSocket.setSoTimeout(2000);
   } catch (SocketException se) {
      se.printStackTrace();
      System.exit(1);
   } 
}

public void receiveAndEcho()
{

   //receive the packet
   byte data[] = new byte[100];
   byte done[] = {1};
   receivePacket = new DatagramPacket(data, data.length);
   System.out.println("Elevator: Waiting for Command.\n");
   try {        
      System.out.println("Waiting..."); // so we know we're waiting
      receiveSocket.receive(receivePacket);
   } catch (IOException e) {
      System.out.print("IO Exception: likely:");
      System.out.println("Receive Socket Timed Out.\n" + e);
      e.printStackTrace();
      System.exit(1);
   }

   
   int hexString ;
   hexString = receivePacket.getData()[0];
 
   DesFloor  = hexString;
   Motor(DesFloor);
   System.out.println("Elevator: arrieved floor  "+ DesFloor);
   System.out.println();
   
   
   // at this stage, elevator is on the demanded floor
   // door open, people enter and ask them for floor number
   System.out.println("Elevator: Door open\n");
   door = true;
   System.out.println("Elevator: which floor you want to go?\n");
   Scanner reader = new Scanner(System.in);  // Reading from System.in
   System.out.println("Enter a number: ");
   int n = reader.nextInt(); // Scans the next token of the input as an int.
   //once finished
   reader.close();
   System.out.println("Elevator: Door close\n");
   door = false;
   
   
   //asking for floor number
   DesFloor = n;
   Motor(DesFloor);
   System.out.println("Elevator: arrieved floor  "+ currentFloor);
   System.out.println();
   System.out.println("Elevator: Door open\n");
   door = true;
   

   //send packet back saying job is done 
   sendPacket = new DatagramPacket(done, done.length,
           receivePacket.getAddress(), receivePacket.getPort());
   System.out.println( "Elevator: Sending packet to scheduler ");
    
   try {
      sendSocket.send(sendPacket);
   } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
   }

   System.out.println("Eelevator: packet sent \n");

   // We're finished, so close the sockets.
   sendSocket.close();
   receiveSocket.close();
}

public void Motor(int floor) {
	System.out.println("Elevator: i am at  "+ currentFloor);
	while(DesFloor != currentFloor) {
		
		if(DesFloor < currentFloor) {
			currentFloor--;	
		}
		else {
			currentFloor++;	
		}
		System.out.println("Elevator: i am at  "+ currentFloor);
	}
}

public String GetString(byte[] bytes)
{
	// Form a String from the byte array.
	String file_string = "";

    for(int i = 2; i < bytes.length; i++)
    {
        file_string += (char)bytes[i];
    }

    return file_string;    
}

public static void main( String args[] )
{
	
   
   while(true) {
	   Elevator c = new Elevator();
   c.receiveAndEcho();
   }
}
}

