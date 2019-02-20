package Enums;

public enum Direction {
	UP(2),
	DOWN(1);
	
	private static final int UP_INT = 2;
	private static final byte UP_BYTE = (byte) UP_INT;
	private static final int DOWN_INT = 1;
	private static final byte DOWN_BYTE = (byte) DOWN_INT;
	
	private int value;
	
	private Direction (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == DOWN_INT){
			return "DOWN";
		} else if (this.value == UP_INT) {
			return "UP";
		} else {
			return "INVALID DIRECTION";
		}
	}
	
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
	 * @return a Direction enum that matches the byte parameter
	 */
	public static Direction convertFromByte(byte b){
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
