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
	
	
	public byte getByte(){
		switch(this.value) {
			case 1:
				return 0x01;
			case 0:
				return 0x00;
			case -1:
				return (byte) 0xFF;
			default:
				return 0x00; //TODO: edit this case
		}
	}
}
