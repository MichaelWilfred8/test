package Enums;

public enum OriginType {
	ERROR(4),
	ELEVATOR(3),
	SCHEDULER(2),
	FLOOR(1);
	
	private static final int ERROR_INT = 4;
	private static final byte ERROR_BYTE = (byte) ERROR_INT;
	private static final int ELEVATOR_INT = 3;
	private static final byte ELEVATOR_BYTE = (byte) ELEVATOR_INT;
	private static final int SCHEDULER_INT = 2;
	private static final byte SCHEDULER_BYTE = (byte) SCHEDULER_INT;
	private static final int FLOOR_INT = 1;
	private static final byte FLOOR_BYTE = (byte) FLOOR_INT;
	
	private int value;
	
	private OriginType (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == ELEVATOR_INT){
			return "ELEVATOR";
		} else  if (this.value == SCHEDULER_INT){
			return "SCHEDULER";
		}else  if (this.value == ERROR_INT){
			return "ERROR";
		} else {
			return "FLOOR";
		}
	}
	
	
	/**	
	 * Generates a byte value for the enum
	 * 
	 * @return	The byte value for this enum
	 */
	public byte getByte(){
		switch(this.value) {
			case ERROR_INT:
				return ERROR_BYTE;
			case ELEVATOR_INT:
				return ELEVATOR_BYTE;
			case SCHEDULER_INT:
				return SCHEDULER_BYTE;
			case FLOOR_INT:
				return FLOOR_BYTE;
			default:
				System.err.println("Given value does not match any enum values");
				return (byte) 0x00; 	// Return 0 when given value does not match any enum values
		}
	}
	
	
	/**
	 * Converts a given byte value into an Enum of the same value
	 * 
	 * @param b	the byte to be converted
	 * @return an OriginType enum that matches the byte parameter
	 */
	public static OriginType convertFromByte(byte b){
		switch(b){
			case ERROR_BYTE:
				return ERROR;
			case ELEVATOR_BYTE:
				return ELEVATOR;
			case SCHEDULER_BYTE:
				return SCHEDULER;
			case FLOOR_BYTE:
				return FLOOR;
			default:
				return null;
		}
	}
}
