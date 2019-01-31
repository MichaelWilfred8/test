import java.util.ArrayList;
import java.util.SortedSet;
import java.util.Collections;

import Enums.*;

// Class for the scheduler to hold information about the elevator and its current position

/**
 * Class for the scheduler to hold information about a single elevator car
 * 
 * @author Craig Worthington
 *
 */
public class ElevatorStatus {
	private int position;						// floor that elevator is at or was last at
	private Direction tripDir;					// direction that the elevator is travelling / will be traveling on this trip
	private ArrayList<Integer> floorsToVisit; 	// list of floors for the elevator to visit next. The list is stored in numerical order, not as a queue
	private MotorState motorState;				// state of the elevator motor (up, down, off)
	private DoorState doorState;				// state that the elevator door is (open/closed)
	private boolean[] floorButtonLights;		// boolean array containing the state of the lights of all the floor buttons in the elevator. Array is indexed from zero so the light for floor 3 is stored at floorButtonLight[2] 
	private int MIN_FLOOR;						// Highest floor that this elevator can visit. Remains constant once set
	private int MAX_FLOOR;						// Lowest floor that this elevator can visit. Remains constant once set
	
	
	/**
	 * Constructor for ElevatorStatus class
	 * 
	 * @param currentFloor		Floor that the elevator is currently at
	 * @param motorState		State of the elevator motor (up, off, down) 
	 * @param doorState			State of the elevator doors (open, closed)
	 * @param numFloors			Number of floors in the building
	 */
	public ElevatorStatus(int currentFloor, MotorState motorState, DoorState doorState, int numFloors){
		this.position = currentFloor;
		this.tripDir = Direction.UP;
		this.floorsToVisit = new ArrayList<Integer>();
		this.motorState = motorState;
		this.doorState = doorState;
		
		this.MAX_FLOOR = numFloors;		// set the highest floor in the elevator to the number of all floors in the building
		this.MIN_FLOOR = 1;				// set the lowest floor in the elevator to 1
		
		this.floorButtonLights = new boolean[numFloors];
		
		// set all the floor button lights to be off
		for(int i = 0; i < numFloors; ++i){
			floorButtonLights[i] = false;
		}
	}
	
	
	
	/**
	 * @return The current floor of the elevator
	 */
	public int getPosition() {
		return position;
	}



	/**
	 * @param position 	The current floor of the elevator
	 */
	public void setPosition(int position) {
		this.position = position;
	}



	/**
	 * @return The direction that the elevator is moving or will be moving in on this current trip
	 */
	public Direction getTripDir() {
		return tripDir;
	}



	/**
	 * @param tripDir	The direction that the elevator is moving or will be moving on this current trip
	 */
	public void setTripDir(Direction tripDir) {
		this.tripDir = tripDir;
	}



	/**
	 * @return the state of the motor in the elevator and its direction
	 */
	public MotorState getMotorState() {
		return motorState;
	}



	/**
	 * @param motorState	the state of the elevator motor to set
	 */
	public void setMotorState(MotorState motorState) {
		this.motorState = motorState;
	}



	/**
	 * @return the state of the doors on the elevator
	 */
	public DoorState getDoorState() {
		return doorState;
	}



	/**
	 * @param doorState the doorState to set
	 */
	public void setDoorState(DoorState doorState) {
		this.doorState = doorState;
	}



	/**
	 * Get the state of all floorButtonLights
	 * 
	 * @return the full array of floorButtonLights states
	 */
	public boolean[] getAllFloorButtonLights() {
		return floorButtonLights;
	}


	/**
	 * Set the state of all floorButtonLights
	 * 
	 * @param floorButtonLights the status of the lights in the elevator for all floors (i.e. on/off)
	 */
	public void setAllFloorButtonLights(boolean[] floorButtonLights) {
		this.floorButtonLights = floorButtonLights;
	}
	
	
	/**
	 * Set the state for a single floor button light (i.e. on/off)
	 * 
	 * @param floor		The floor of the button to be set
	 * @param state		The state to set the button to
	 */
	public void setFloorButtonLight(int floor, boolean state){
		this.floorButtonLights[floor - 1] = state;
	}
	
	/**
	 * Toggle a single floorButtonLight
	 * 
	 * @param floor	The floor of the floor button to be toggled
	 */
	public void toggleFloorButtonLight(int floor){
		this.floorButtonLights[floor - 1] = !this.floorButtonLights[floor - 1];		// toggle 
	}



	/**
	 * @return the list of floors for this elevator to visit on the current trip
	 */
	public ArrayList<Integer> getFloorsToVisit() {
		return floorsToVisit;
	}



	/**
	 * Add a floor to the list of floors for this elevator to visit. 
	 * 
	 * @param floor		Floor to be added to the list of floors for this elevator to visit
	 */
	public void addFloor(int floor){
		this.floorsToVisit.add(floor);			// Add the new floor to the list
		Collections.sort(this.floorsToVisit);	// Sort the list into numerical order
	}
	
	
	
	/**
	 * Determines the next floor for this elevator to visit
	 * 
	 * @return The next floor for this elevator to visit
	 */
	public int getNextFloor(){
		// Check if this elevator is at the extreme ends of the elevator shaft
		if ((this.tripDir == Direction.UP) && (this.position == this.MAX_FLOOR)){			// check if the elevator is currently at the top floor and on an upwards trip
			if (!this.floorsToVisit.isEmpty()){															// if the list of floors for this elevator to visit is empty
				return this.position;																	// return the current position of the elevator so it remains on this floor
			} else {																					// if the elevator has another floor to visit
				this.tripDir = Direction.DOWN;												// set the current trip direction to down
				return this.floorsToVisit.remove(this.floorsToVisit.size() - 1);							// remove the highest floor in the list and return it
			}
		} else if ((this.tripDir == Direction.DOWN) && (this.position == this.MIN_FLOOR)) {	// check if the elevator is current at the bottom floor and on a downwards trip
			if (!this.floorsToVisit.isEmpty()){															// if the list of floors for this elevator to visit is empty
				return this.position;																	// return the current position of the elevator so it remains on this floor
			} else {																					// if the elevator has another floor to visit
				this.tripDir = Direction.UP;													// set the current trip direction to up
				return this.floorsToVisit.remove(0);														// remove the lowest floor in the list and return it
			}
		}
		
		// Check if this elevator is on an upwards trip
		if (this.tripDir == Direction.UP){
			// find the next floor to visit
			// find the nearest floor above the current floor
			for(int i = 0; i < this.floorsToVisit.size(); ++i){
				if (this.floorsToVisit.get(i) > this.position){
					return this.floorsToVisit.remove(i);
				}
			}
		}
		
		return 1;
	}
}
