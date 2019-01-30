import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;


// TODO: Make Elevator Input Packet an extension of DatagramPacket

public class ElevatorInputPacket {
	
	private TimeStamp timeStamp;				// Timestamp for when request was sent
	private int floor;							// Floor where request originated from
	private FloorButtonDirection floorButton;	// Button pressed on Floor (up or down)
	private int carButton; 						// Floor Button pressed by passenger in elevator
	
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
	
	
	
	// Constructor for ElevatorInputPacket where the timestamp is inputted (Remove this function?)
	public ElevatorInputPacket(TimeStamp timeStamp, int floor, FloorButtonDirection floorButton, int carButton){
		this.timeStamp = timeStamp;
		this.floor = floor;
		this.floorButton = floorButton;
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
	
	
	// TODO: remove this method from the class and change all references to the TimeStamp object
	// return the time stamp as a string
	public String getTimeStampString(){
		return this.timeStamp.toString();
	}


	public TimeStamp getTimeStamp() {
		return timeStamp;
	}

	public int getFloor() {
		return floor;
	}

	public FloorButtonDirection getFloorButton() {
		return floorButton;
	}

	public int getCarButton() {
		return carButton;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + carButton;
		result = prime * result + floor;
		result = prime * result + ((floorButton == null) ? 0 : floorButton.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElevatorInputPacket other = (ElevatorInputPacket) obj;
		if (carButton != other.carButton)
			return false;
		if (floor != other.floor)
			return false;
		if (floorButton != other.floorButton)
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		return true;
	}
	
	// Create a byte array for the Elevator input packet
	public byte[] toBytes(){
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
		System.out.println("Time Stamp: " + this.getTimeStampString());
		System.out.println("Floor: " + this.getFloor());
		System.out.println("Floor Button Direction: " + this.getFloorButton().toString());
		System.out.println("Car Button: " + this.getCarButton());
	}
	
	public static void main(String args[]){
		ElevatorInputPacket p1 = new ElevatorInputPacket(3, FloorButtonDirection.UP, 7);
		
		p1.printElevatorPacket();
		
		byte[] b = p1.toBytes();
		
		ElevatorInputPacket p2 = new ElevatorInputPacket(b);
		
		p2.printElevatorPacket();
	}
}
