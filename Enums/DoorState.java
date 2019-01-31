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
	 * Converts a given byte value into an Enum of the same value
	 * 
	 * @param b	the byte to be converted
	 * @return a DoorState enum that matches the byte parameter
	 */
	public DoorState convertFromByte(byte b){
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
