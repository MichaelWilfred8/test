package Enums;

public enum Direction {
	DOWN(0),
	UP(1);
	
	private int value;
	
	private Direction (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == 0){
			return "DOWN";
		} else {
			return "UP";
		}
	}
	
	public byte getByte(){
		switch(this.value) {
			case 1:
				return 0x01;
			case 0:
				return 0x00;
			default:
				return (byte) 0xFF; //TODO: edit this case to return a null different value?
		}
	}
}
