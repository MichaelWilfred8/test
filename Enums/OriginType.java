package Enums;

public enum OriginType {
	ELEVATOR(3),
	SCHEDULER(2),
	FLOOR(1);
	
	private int value;
	
	private OriginType (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == 3){
			return "ELEVATOR";
		} else  if (this.value == 2){
			return "SCHEDULER";
		} else {
			return "FLOOR";
		}
	}
	
	public byte getByte(){
		switch(this.value) {
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
}
