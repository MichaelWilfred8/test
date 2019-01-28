
public enum FloorButtonDirection {
	UP(1),  
	DOWN(0);
	
	private int value;
	
	private FloorButtonDirection (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
