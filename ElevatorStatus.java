import java.util.ArrayList;

// Class for the scheduler to hold information about the elevator and its current position

public class ElevatorStatus {
	private int position;	// floor that elevator is at or was last at
	private FloorButtonDirection dir;	// direction that elevator is traveling in
	private ArrayList<Integer> floorsToVisit; // list of floors for the elevator to visit next
	
	public ElevatorStatus(int floor){
		this.position = floor;
		this.dir = FloorButtonDirection.UP;
		this.floorsToVisit = new ArrayList<Integer>();
	}
	
	//  add a floor to the list of floors for this elevator to visit
	public void addFloor(int floor){
		this.floorsToVisit.add(floor);
	}
	
	
	
}
