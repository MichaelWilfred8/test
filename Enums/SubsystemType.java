package Enums;

public enum SubsystemType {
	ERROR(8),
	MOTOR(7),
	DOOR(6),
	CARLAMP(5),
	FLOORLAMP(4),
	LOCATION(3),
	INPUT(2),
	REQUEST(1);
	
	private static final int ERROR_INT = 8;
	private static final byte ERROR_BYTE = (byte) ERROR_INT;
	private static final int MOTOR_INT = 7;
	private static final byte MOTOR_BYTE = (byte) MOTOR_INT;
	private static final int DOOR_INT = 6;
	private static final byte DOOR_BYTE = (byte) DOOR_INT;
	private static final int CARLAMP_INT = 5;
	private static final byte CARLAMP_BYTE = (byte) CARLAMP_INT;
	private static final int FLOORLAMP_INT = 4;
	private static final byte FLOORLAMP_BYTE = (byte) FLOORLAMP_INT;
	private static final int LOCATION_INT = 3;
	private static final byte LOCATION_BYTE = (byte) LOCATION_INT;
	private static final int INPUT_INT = 2;
	private static final byte INPUT_BYTE = (byte) INPUT_INT;
	private static final int REQUEST_INT = 1;
	private static final byte REQUEST_BYTE = (byte) REQUEST_INT;
	
	
	private int value;
	
	private SubsystemType (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		switch(this.value) {
			case ERROR_INT:
				return "ERROR";
			case MOTOR_INT:
				return "MOTOR";
			case DOOR_INT:
				return "DOOR";
			case CARLAMP_INT:
				return "CARLAMP";
			case FLOORLAMP_INT:
				return "FLOORLAMP";
			case LOCATION_INT:
				return "LOCATION";
			case INPUT_INT:
				return "INPUT";
			case REQUEST_INT:
				return "REQUEST";
			default:
				return "Invalid Type";
		}
	}
	
	public SubsystemType toSubsystem(int error){
		switch(error) {
			case ERROR_INT:
				return ERROR;
			case MOTOR_INT:
				return MOTOR;
			case DOOR_INT:
				return DOOR;
			case CARLAMP_INT:
				return CARLAMP;
			case FLOORLAMP_INT:
				return FLOORLAMP;
			case LOCATION_INT:
				return LOCATION;
			case INPUT_INT:
				return INPUT;
			case REQUEST_INT:
				return REQUEST;
			default:
				System.err.println("Given integer does not match any subsystem enum values. Returning ERROR");
				return ERROR;
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
			case MOTOR_INT:
				return MOTOR_BYTE;
			case DOOR_INT:
				return DOOR_BYTE;
			case CARLAMP_INT:
				return CARLAMP_BYTE;
			case FLOORLAMP_INT:
				return FLOORLAMP_BYTE;
			case LOCATION_INT:
				return LOCATION_BYTE;
			case INPUT_INT:
				return INPUT_BYTE;
			case REQUEST_INT:
				return REQUEST_BYTE;
			default:
				System.err.println("Given value does not match any enum values");
				return (byte) 0x00; 	// Return 0 when given value does not match any enum values
		}
	}


	/**
	 * Converts a given byte value into an Enum of the same value
	 * 
	 * @param b	the byte to be converted
	 * @return a SubSystemType enum that matches the byte parameter
	 */
	public static SubsystemType convertFromByte(byte b){
		switch(b){
			case ERROR_BYTE:
				return ERROR;
			case MOTOR_BYTE:
				return MOTOR;
			case DOOR_BYTE:
				return DOOR;
			case CARLAMP_BYTE:
				return CARLAMP;
			case FLOORLAMP_BYTE:
				return FLOORLAMP;
			case LOCATION_BYTE:
				return LOCATION;
			case INPUT_BYTE:
				return INPUT;
			case REQUEST_BYTE:
				return REQUEST;
			default:
				return null;
		}
	}
}
