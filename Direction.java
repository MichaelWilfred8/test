
public enum FloorButtonDirection {
	DOWN(0),
	UP(1);
	
	private int value;
	
	private FloorButtonDirection (int value){
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
}
