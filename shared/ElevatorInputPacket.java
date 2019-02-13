package shared;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.ByteBuffer;

import Enums.*;

/**
 * @author Craig Worthington
 *
 */

// TODO: remove?

public class ElevatorInputPacket {

	private TimeStamp timeStamp;				// Timestamp for when request was sent
	private int floor;							// Floor where request originated from
	private Direction floorButton;	// Button pressed on Floor (up or down)
	private int carButton; 						// Floor Button pressed by passenger in elevator

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");	// String format of the time stamp. Uses a 24 hour clock (hour is 0-23)
	protected static final int BYTE_ARRAY_LENGTH = 28;	// Length of the byte array needed to transmit all information in an ElevatorInputPacket

	/*
	 * Below are the indexes of each element in the byte array created by the ElevatorInputPacket
	 */
	public static final int HOURS_INDEX = 0;
	public static final int MINUTES_INDEX = 4;
	public static final int SECONDS_INDEX = 8;
	public static final int MILLISECONDS_INDEX = 12;
	public static final int FLOOR_INDEX = 16;
	public static final int FLOOR_BUTTON_INDEX = 20;
	public static final int CAR_BUTTON_INDEX = 24;

	public static final int REQUEST_FROM_FLOOR = -1;	// Constant value used in carButton field if the request originated from a floor and not within an elevator

	/**
	 * Constructor for ElevatorInputPacket. Each element of the ElevatorInputPacket is entered separately
	 *
	 * @param timeStamp: 	TimeStamp object of when request was created
	 * @param floor:		Floor where request originated from
	 * @param floorButton: 	Value of button that was pressed on the floor (i.e. up or down)
	 * @param carButton:	Value of button that was pressed inside the elevator car (i.e. floor that passenger wishes to travel to)
	 */
	public ElevatorInputPacket(TimeStamp timeStamp, int floor, Direction floorButton, int carButton){
		this.timeStamp = timeStamp;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}


	/**
	 * Constructor for ElevatorInputPacket where no timestamp is given. Automatically generates its own timestamp based on current system time
	 *
	 * @param floor:		Floor where request originated from
	 * @param floorButton: 	Value of button that was pressed on the floor (i.e. up or down)
	 * @param carButton:	Value of button that was pressed inside the elevator car (i.e. floor that passenger wishes to travel to)
	 */
	public ElevatorInputPacket(int floor, Direction floorButton, int carButton){
		this.timeStamp = new TimeStamp(LocalDateTime.now().format(TIME_FORMAT));		// set timestamp to the current time
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}


	/**
	 * Constructor for ElevatorInputPacket where a byte array containing all relevant information is given.
	 *
	 * @param b:	Byte array containing all information needed to create the ElevatorInputPacket
	 */
	public ElevatorInputPacket(byte[] b){
		ByteBuffer buf = ByteBuffer.allocate(BYTE_ARRAY_LENGTH);	// create a ByteBuffer to parse the byte array
		buf.put(b);													// put all bytes from array into new buffer

		this.timeStamp = new TimeStamp(buf.getInt(HOURS_INDEX), buf.getInt(MINUTES_INDEX), buf.getInt(SECONDS_INDEX), buf.getInt(MILLISECONDS_INDEX));		// get time stamp from the buffer, reading each integer
		this.floor = buf.getInt(FLOOR_INDEX);		// get floor number from the ByteBuffer
		this.floorButton = Direction.values()[buf.getInt(FLOOR_BUTTON_INDEX)];		// get the value of the floor button direction from the bytebuffer
		this.carButton = buf.getInt(CAR_BUTTON_INDEX);		// get the value of the car button from the ByteBuffer
	}


	/**
	 *
	 * @return TimeStamp of when request was sent (format HH:mm:ss:SSS)
	 */
	public TimeStamp getTimeStamp() {
		return this.timeStamp;
	}


	/**
	 *
	 * @return Floor where request was sent from
	 */
	public int getFloor() {
		return this.floor;
	}


	/**
	 *
	 * @return Direction of button pressed on floor (i.e. up/down)
	 */
	public Direction getFloorButton() {
		return this.floorButton;
	}


	/**
	 *
	 * @return floor button that was selected in the elevator
	 */
	public int getCarButton() {
		return this.carButton;
	}

	// TODO: add toString, hashCode, equals



	/**
	 * Creates a byte array containing the values of all the fields in the class
	 * Byte array format: TimeStamp (Hour, minutes, seconds, milliseconds)(all int), floor(int), floorButton(int), carButton(int)
	 * @return Byte array
	 */
	public byte[] getBytes(){
		ByteBuffer buf = ByteBuffer.allocate(BYTE_ARRAY_LENGTH);

		buf.put(this.timeStamp.getBytes()); // add timestamp object as bytes to the byte buffer
		buf.putInt(this.floor);	// add floor number to byte buffer
		buf.putInt(this.floorButton.ordinal()); // add floor button to byte buffer
		buf.putInt(this.carButton);		// add car button to byte buffer

		buf.flip();	// flip buffer to allow for reading appropriately

		return buf.array();		// return the byte buffer as an array
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


	@Override
	public String toString() {
		return "ElevatorInputPacket [timeStamp=" + timeStamp + ", floor=" + floor + ", floorButton=" + floorButton
				+ ", carButton=" + carButton + "]";
	}

	public static void main(String args[]){
		ElevatorInputPacket p1 = new ElevatorInputPacket(3, Direction.UP, 7);

		System.out.println(p1.toString());

		ElevatorInputPacket p2 = new ElevatorInputPacket(p1.getBytes());

		System.out.println(p2.toString());
	}
}
