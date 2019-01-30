// Floor.java
// This class represents the floors in the buildings
// Stores the scheduler that it communicates with, its floor number and the buttons on each floor

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Floor {

	private Scheduler scheduler;
	private int floorNumber;
	private FloorButton[] floorButtons;

	DatagramPacket sendPacket, receivePacket; //packets and socket used to send information
	DatagramSocket sendReceiveSocket;

	public Floor(Scheduler scheduler, int floorNumber){
		this.scheduler = scheduler;
		this.floorNumber = floorNumber;

		if (floorNumber == this.scheduler.getTopFloor()) {//if the floor is the top floor
			floorButtons = new FloorButton[1];//create an array of size one
			floorButtons[0] = new FloorButton(FloorButtonDirection.DOWN);//only button on the floor will to go down
		}else if (floorNumber == 1) {//if the floor is the bottom floor
			floorButtons = new FloorButton[1];//create an array of size one
			floorButtons[0] = new FloorButton(FloorButtonDirection.UP);//only button on the floor will be to go up
		}else {
			floorButtons = new FloorButton[2];//create an array of size two
			floorButtons[0] = new FloorButton(FloorButtonDirection.DOWN);//create an up button
			floorButtons[1] = new FloorButton(FloorButtonDirection.UP);//create a down button
		}
	}

	/**
	 * 
	 * @param button The Button that has been pressed
	 * @return Message to be sent to Scheduler
	 */
	public byte[] createMessage(FloorButton button) {

		FloorButtonDirection direction = button.getDirection();//TODO: add button.pressButton() to method that will call createMessage
		LocalDateTime now = LocalDateTime.now();//time of request  

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(now.getHour());
		output.write(now.getMinute());
		output.write(now.getSecond());
		int nano = now.getNano();
		ByteBuffer bb = ByteBuffer.allocate(4); 
	    bb.putInt(nano);
	    try {
			output.write(bb.array());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output.write(floorNumber);
		output.write(direction.getValue());

		byte[] returnBytes = output.toByteArray();
		System.out.println(nano);
		return returnBytes;
	}

	public void sendAndReceive(FloorButton button){
		byte[] message = createMessage(button);

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
		System.out.println("(Bytes)" + Arrays.toString(sendPacket.getData()) + "\n");
	}

	/**
	 * 
	 * @return List of Floor Buttons
	 */
	public FloorButton[] getFloorButtons() {
		return floorButtons;
	}

	/**
	 * 
	 * @return Floor Number
	 */
	public int getFloorNumber() {	//getter for floor number
		return floorNumber;
	}

}
