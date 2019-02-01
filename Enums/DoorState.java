package Enums;

public enum DoorState {
	OPEN(1),
	CLOSED(0);
	
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
			case 1:
				return 0x01;
			case 0:
				return 0x00;
			default:
				return (Byte) null; //TODO: edit this case to return a null different value?
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
			case 0x01:
				return OPEN;
			case 0x00:
				return CLOSED;
			default:
				return null;
		}
	}
	
	public String toString(){
		if (this.value == 1){
			return "OPEN";
		} else  {
			return "CLOSED";
		}
	}
}
