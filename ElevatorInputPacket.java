import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class ElevatorInputPacket {
	
	private LocalDateTime timeStamp;			// Timestamp for when request was sent
	private int floor;							// Floor where request originated from
	private FloorButtonDirection floorButton;	// Button pressed on Floor (up or down)
	private int carButton; 						// Floor Button pressed by passenger in elevator
	
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");	// Format for the timestamp. Uses a 24 hour clock (hour is 0-23)
	private static final int BYTE_ARRAY_LENGTH = 100;
	private static final int TIMESTAMP_STRING_LENGTH = 12;
	
	
	// Constructor for ElevatorInputPacket where the timestamp is inputted (Remove this function?)
	public ElevatorInputPacket(LocalDateTime timeStamp, int floor, FloorButtonDirection floorButton, int carButton){
		this.timeStamp = timeStamp;
		this.floor = floor;
		this.floorButton = floorButton;
	}
	
	
	// Constructor for ElevatorInputPacket where no timestamp is given
	public ElevatorInputPacket(int floor, FloorButtonDirection floorButton, int carButton){
		this.timeStamp = LocalDateTime.now();		// set timestamp to the current time
		this.floor = floor;
		this.floorButton = floorButton;
	}
	
	// TODO: finish this method
	// return the time stamp as a string
	public String getTimeStampString(){
		return this.timeStamp.format(TIME_FORMAT);
	}


	public LocalDateTime getTimeStamp() {
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
		
		buf.put(this.getTimeStampString().getBytes());	// add time stamp string as bytes to the byte buffer
		
		/* Code for adding the time stamp as individual characters to the buffer
		 * 
		 * buf.putInt(this.timeStamp.getHour()); 		// add hour from time stamp as bytes to the byte buffer
		 * buf.putInt(this.timeStamp.getMinute());		// add minute from time stamp as bytes to the byte buffer
		 * buf.putInt(this.timeStamp.getSecond());		// add second from time stamp as bytes to the byte buffer
		 * buf.putInt(this.timeStamp.getNano());		// add nanosecond from time stamp as bytes to the byte buffer
		 */
		
		
		buf.putInt(this.floor);	// add floor number to byte buffer
		
		buf.putInt(this.floorButton.ordinal()); // add floor button to byte buffer
		
		buf.putInt(this.carButton);		// add car button to byte buffer
		
		return buf.array();		// return the byte buffer as an array
	}
	
	public void fromBytes(byte[] b){
		ByteBuffer buf = ByteBuffer.allocate(BYTE_ARRAY_LENGTH);
		buf.put(b);	// put all bytes from array into new buffer
		
		// create an emptystring from byte buffer
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < TIMESTAMP_STRING_LENGTH; ++i){
			sb.append(buf.getChar());	// append each character from the timestamp bytes to the stringbuilder
		}
		
		this.timeStamp = LocalDateTime.parse(sb.toString(), TIME_FORMAT);	// set the timeString to the string from the byte buffer
		
		this.floor = buf.getInt();
		this.floorButton = buf.getInt();
		this.carButton = buf.getInt();
		
		
	}
	
	// TODO: create function that prints the information in the packet to the console
	public void printElevatorPacket(){
		
	}
	
	public static void main(String args[]){
		
	}
}
