package Enums;

public enum SubsystemType {
	MOTOR(7),
	DOOR(6),
	CARLAMP(5),
	FLOORLAMP(4),
	LOCATION(3),
	INPUT(2),
	REQUEST(1);
	
	private int value;
	
	private SubsystemType (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		switch(this.value) {
			case 7:
				return "MOTOR";
			case 6:
				return "DOOR";
			case 5:
				return "CARLAMP";
			case 4:
				return "FLOORLAMP";
			case 3:
				return "LOCATION";
			case 2:
				return "INPUT";
			case 1:
				return "REQUEST";
			default:
				return "Invalid Type";
		}
	}
	
	
	/**	
	 * Generates a byte value for the enum
	 * 
	 * @return	The byte value for this enum
	 */
	public byte getByte(){
		switch(this.value) {
			case 7:
				return 0x07;
			case 6:
				return 0x06;
			case 5:
				return 0x05;
			case 4:
				return 0x04;
			case 3:
				return 0x03;
			case 2:
				return 0x02;
			case 1:
				return 0x01;
			default:
				return 0x00;
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
			case 0x07:
				return MOTOR;
			case 0x06:
				return DOOR;
			case 0x05:
				return CARLAMP;
			case 0x04:
				return FLOORLAMP;
			case 0x03:
				return LOCATION;
			case 0x02:
				return INPUT;
			case 0x01:
				return REQUEST;
			default:
				return null;
		}
	}
}
