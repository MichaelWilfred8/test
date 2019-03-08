package Enums;

public enum OriginType {
	ELEVATOR(3),
	SCHEDULER(2),
	FLOOR(1);
	
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
			case ELEVATOR_INT:
				return ELEVATOR_BYTE;
			case SCHEDULER_INT:
				return SCHEDULER_BYTE;
			case FLOOR_INT:
				return FLOOR_BYTE;
			default:
				return 0x00;
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
