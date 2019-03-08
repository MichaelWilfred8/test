package Enums;

public enum FloorLampState {
	UP(2),
	DOWN(1);
	
	private static final int UP_INT = 2;
	private static final byte UP_BYTE = (byte) UP_INT;
	private static final int DOWN_INT = 1;
	private static final byte DOWN_BYTE = (byte) DOWN_INT;
	
	private int value;
	
	private FloorLampState (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == DOWN_INT){
			return "DOWN";
		} else if (this.value == UP_INT){
			return "UP";
		} else {
			return "INVALID FLOORLAMPSTATE";
		}
	}
	
	
	/**	
	 * Generates a byte value for the enum
	 * 
	 * @return	The byte value for this enum
	 */
	public byte getByte(){
		switch(this.value) {
			case UP_INT:
				return UP_BYTE;
			case DOWN_INT:
				return DOWN_BYTE;
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
			case UP_BYTE:
				return UP;
			case DOWN_BYTE:
				return DOWN;
			default:
				return null;
		}
	}
}
