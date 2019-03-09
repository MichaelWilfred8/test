package Enums;

public enum DoorState {
	OPEN(2),
	CLOSED(1);
	
	private static final int OPEN_INT = 2;
	private static final byte OPEN_BYTE = (byte) OPEN_INT;
	private static final int CLOSED_INT = 1;
	private static final byte CLOSED_BYTE = (byte) CLOSED_INT;
	
	private int value;
	
	private DoorState (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	
	/**	
	 * Generates a byte value for the enum
	 * 
	 * @return	The byte value for this enum
	 */
	public byte getByte(){
		switch(this.value) {
			case OPEN_INT:
				return OPEN_BYTE;
			case CLOSED_INT:
				return CLOSED_BYTE;
			default:
				System.err.println("Given value does not match any enum values");
				return (byte) 0x00; 	// Return 0 when given value does not match any enum values
		}
	}
	
	
	/**
	 * Converts a given byte value into an Enum of the same value
	 * 
	 * @param b	the byte to be converted
	 * @return a DoorState enum that matches the byte parameter
	 */
	public static DoorState convertFromByte(byte b){
		switch(b){
			case OPEN_BYTE:
				return OPEN;
			case CLOSED_BYTE:
				return CLOSED;
			default:
				return null;
		}
	}
	
	public String toString(){
		if (this.value == OPEN_INT){
			return "OPEN";
		} else if (this.value == CLOSED_INT) {
			return "CLOSED";
		} else {
			return "INVALID DOORSTATE";
		}
	}
}
