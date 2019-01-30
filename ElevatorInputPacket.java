import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;


// TODO: Make Elevator Input Packet an extension of DatagramPacket

public class ElevatorInputPacket {
	
	private TimeStamp timeStamp;				// Timestamp for when request was sent
	private int floor;							// Floor where request originated from
	private FloorButtonDirection floorButton;	// Button pressed on Floor (up or down)
	private int carButton; 						// Floor Button pressed by passenger in elevator
	private DatagramPacket packet;				// DatagramPacket for sending or receiving the ElevatorInputPacket information
	
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");	// Format for the timestamp. Uses a 24 hour clock (hour is 0-23)
	private static final int BYTE_ARRAY_LENGTH = 28;
	private static final int TIMESTAMP_STRING_LENGTH = 12;
	
	private static final int HOURS_INDEX = 0;
	private static final int MINUTES_INDEX = 4;
	private static final int SECONDS_INDEX = 8;
	private static final int MILLISECONDS_INDEX = 12;
	private static final int FLOOR_INDEX = 16;
	private static final int FLOOR_BUTTON_INDEX = 20;
	private static final int CAR_BUTTON_INDEX = 24;
	
	
	// TODO: create constructor that mimics constructors for DatagramPacket?
	
	
	// Constructor for ElevatorInputPacket where the timestamp is inputted (Remove this function?)
	public ElevatorInputPacket(TimeStamp timeStamp, int floor, FloorButtonDirection floorButton, int carButton){
		this.timeStamp = timeStamp;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}
	
	
	// Constructor for ElevatorInputPacket where no timestamp is given
	public ElevatorInputPacket(int floor, FloorButtonDirection floorButton, int carButton){
		this.timeStamp = new TimeStamp(LocalDateTime.now().format(TIME_FORMAT));		// set timestamp to the current time
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}
	
	// Constructor for Elevator Input Packet from a byte array
	public ElevatorInputPacket(byte[] b){
		ByteBuffer buf = ByteBuffer.allocate(BYTE_ARRAY_LENGTH);	// create a ByteBuffer to parse the byte array
		buf.put(b);													// put all bytes from array into new buffer
		
		this.timeStamp = new TimeStamp(buf.getInt(HOURS_INDEX), buf.getInt(MINUTES_INDEX), buf.getInt(SECONDS_INDEX), buf.getInt(MILLISECONDS_INDEX));		// get time stamp from the buffer, reading each integer
		this.floor = buf.getInt(FLOOR_INDEX);		// get floor number from the ByteBuffer
		this.floorButton = FloorButtonDirection.values()[buf.getInt(FLOOR_BUTTON_INDEX)];		// get the value of the floor button direction from the bytebuffer
		this.carButton = buf.getInt(CAR_BUTTON_INDEX);		// get the value of the car button from the ByteBuffer
	}
	
	// Constructor for creating an ElevatorInputPacket from a DatagramPacket
	public ElevatorInputPacket(DatagramPacket p){
		this.packet = p;	// set the object's DatagramPacket as the given DatagramPacket
		
		ByteBuffer buf = ByteBuffer.allocate(BYTE_ARRAY_LENGTH);	// create a ByteBuffer to parse the data byte array
		buf.put(this.packet.getData());		// put all bytes from the DatagramPacket data array into new buffer
		
		this.timeStamp = new TimeStamp(buf.getInt(HOURS_INDEX), buf.getInt(MINUTES_INDEX), buf.getInt(SECONDS_INDEX), buf.getInt(MILLISECONDS_INDEX));		// get time stamp from the buffer, reading each integer
		this.floor = buf.getInt(FLOOR_INDEX);		// get floor number from the ByteBuffer
		this.floorButton = FloorButtonDirection.values()[buf.getInt(FLOOR_BUTTON_INDEX)];		// get the value of the floor button direction from the bytebuffer
		this.carButton = buf.getInt(CAR_BUTTON_INDEX);		// get the value of the car button from the ByteBuffer
	}
	
	// Constructor for creating an ElevatorInputPacket to send
	public ElevatorInputPacket(TimeStamp timeStamp, int floor, FloorButtonDirection floorButton, int carButton, InetAddress address, int port){
		this.timeStamp = timeStamp;
	}

	public TimeStamp getTimeStamp() {
		return this.timeStamp;
	}

	public int getFloor() {
		return this.floor;
	}

	public FloorButtonDirection getFloorButton() {
		return this.floorButton;
	}

	public int getCarButton() {
		return this.carButton;
	}
	
	
	/*
	 * Below are all the methods from the DatagramPacket class
	 */
	public InetAddress getAddress(){
		return this.packet.getAddress();
	}
	
	public byte[] getData(){
		return this.packet.getData();
	}
	
	public int getLength(){
		return this.BYTE_ARRAY_LENGTH;
	}
	
	// TODO: fix this function to return the actual offset
	public int getOffset(){
		return 1;
	}
	
	public int getPort(){
		return this.packet.getPort();
	}
	
	public SocketAddress getSocketAddress(){
		return this.packet.getSocketAddress();
	}
	
	public void setAddress(InetAddress iaddr){
		this.packet.setAddress(iaddr);
	}
	
	public void setData(byte[] buf){
		this.packet.setData(buf);
	}
	
	public void setData(byte[] buf, int offset, int length){
		this.packet.setData(buf, offset, length);
	}
	
	public void setPort(int iport){
		this.packet.setPort(iport);
	}
	
	public void setSocketAddress(SocketAddress address){
		this.packet.setSocketAddress(address);
	}
	
	// TODO: add toString, hashCode, equals
	
	// Create a byte array for the Elevator input packet
	public byte[] getBytes(){
		ByteBuffer buf = ByteBuffer.allocate(BYTE_ARRAY_LENGTH);
		
		buf.put(this.timeStamp.getBytes()); // add timestamp object as bytes to the byte buffer
		
		// TODO: Use this code below to add bytes to the timestamp to reduce coupling?
		/*
		buf.putInt(this.timeStamp.getHours()); 			// add hour from time stamp as bytes to the byte buffer
		buf.putInt(this.timeStamp.getMinutes());		// add minute from time stamp as bytes to the byte buffer
		buf.putInt(this.timeStamp.getSeconds());		// add second from time stamp as bytes to the byte buffer
		buf.putInt(this.timeStamp.getMilliseconds());	// add nanosecond from time stamp as bytes to the byte buffer
		 */
		
		
		buf.putInt(this.floor);	// add floor number to byte buffer
		buf.putInt(this.floorButton.ordinal()); // add floor button to byte buffer
		buf.putInt(this.carButton);		// add car button to byte buffer
		
		buf.flip();	// flip buffer to allow for reading appropriately
		
		return buf.array();		// return the byte buffer as an array
	}
	
	
	public void printElevatorPacket(){
		System.out.println("Time Stamp: " + this.timeStamp.toString());
		System.out.println("Floor: " + this.getFloor());
		System.out.println("Floor Button Direction: " + this.getFloorButton().toString());
		System.out.println("Car Button: " + this.getCarButton());
	}
	
	// Returns a DatagramPacket containing the data in this class. Packet does not contain an address!
	public DatagramPacket getDatagramPacket(){
		this.packet.setData(this.getBytes());
		this.packet.setLength(BYTE_ARRAY_LENGTH);
		return this.packet;
	}
	
	public static void main(String args[]){
		ElevatorInputPacket p1 = new ElevatorInputPacket(3, FloorButtonDirection.UP, 7);
		
		p1.printElevatorPacket();
		
		byte[] b = p1.getBytes();
		
		ElevatorInputPacket p2 = new ElevatorInputPacket(b);
		
		p2.printElevatorPacket();
	}
}
