package shared;

public enum SocketPort {
	FLOOR_LISTENER(101),
	FLOOR_SENDER(102),
	SCHEDULER_LISTENER(201),
	SCHEDULER_SENDER(202),
	ELEVATOR_LISTENER(68),
	ELEVATOR_SENDER(302);
	
	private int value;
	
	private SocketPort (int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
