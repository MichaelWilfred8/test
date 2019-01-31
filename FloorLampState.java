
public enum FloorLampState {
	UP(1),
	DOWN(0);
	
	private int value;
	
	private FloorLampState (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public String toString(){
		if (this.value == 1){
			return "UP";
		} else  {
			return "DOWN";
		}
	}
}
