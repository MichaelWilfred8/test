package Enums;

public enum MotorState {
	UP(1),
	OFF(0),
	DOWN(-1);
	
	private int value;
	
	private MotorState (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	
	public String toString(){
		if (this.value == -1){
			return "DOWN";
		} else  if (this.value == 0){
			return "OFF";
		} else {
			return "UP";
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
			case -1:
				return (byte) 0xFF;
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
	public MotorState convertFromByte(byte b){
		switch(b){
			case 0x01:
				return UP;
			case 0x00:
				return OFF;
			case (byte) 0xFF:
				return DOWN;
			default:
				return null;
		}
	}
}
