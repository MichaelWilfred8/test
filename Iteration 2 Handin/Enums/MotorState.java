package Enums;

public enum MotorState {
	UP(3),
	OFF(2),
	DOWN(1);
	
	private static final int UP_INT = 3;
	private static final byte UP_BYTE = (byte) UP_INT;
	private static final int OFF_INT = 2;
	private static final byte OFF_BYTE = (byte) OFF_INT;
	private static final int DOWN_INT = 1;
	private static final byte DOWN_BYTE = (byte) DOWN_INT;
	
	private int value;
	
	
	private MotorState (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	
	public String toString(){
		if (this.value == UP_INT) {
			return "UP";
		} else if (this.value == OFF_INT) {
			return "OFF";
		} else if (this.value == DOWN_INT) {
			return "DOWN";
		} else {
			return "INVALID MOTORSTATE";
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
			case OFF_INT:
				return OFF_BYTE;
			case DOWN_INT:
				return DOWN_BYTE;
			default:
				return (Byte) null; //TODO: edit this case
		}
	}
	
	
	/**
	 * Converts a given byte value into an Enum of the same value
	 * 
	 * @param b	the byte to be converted
	 * @return a MotorState enum that matches the byte parameter
	 */
	public static MotorState convertFromByte(byte b){
		switch(b){
			case UP_BYTE:
				return UP;
			case OFF_BYTE:
				return OFF;
			case DOWN_BYTE:
				return DOWN;
			default:
				return null;
		}
	}
}
