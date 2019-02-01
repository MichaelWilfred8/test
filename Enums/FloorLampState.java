package Enums;

public enum FloorLampState {
	UP(1),
	DOWN(0);
	
	private int value;
	
	private FloorLampState (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == 1){
			return "UP";
		} else  {
			return "DOWN";
		}
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
	 * @return a FloorLampState enum that matches the byte parameter
	 */
	public static FloorLampState convertFromByte(byte b){
		switch(b){
			case 0x01:
				return UP;
			case 0x00:
				return DOWN;
			default:
				return null;
		}
	}
}
