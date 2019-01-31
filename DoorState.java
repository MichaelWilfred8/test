
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
	
	public String toString(){
		if (this.value == 1){
			return "OPEN";
		} else  {
			return "CLOSED";
		}
	}
}
