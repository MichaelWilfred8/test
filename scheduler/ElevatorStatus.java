package scheduler;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import Enums.*;
import shared.*;

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
	private SortedSet<Integer> floorsToVisit;	// Sorted Set of floors for this elevator to visit.
	private int nextDestination;				// Floor for this elevator to visit next
	private MotorState motorState;				// state of the elevator motor (up, down, off)
	private DoorState doorState;				// state that the elevator door is (open/closed)
	private boolean[] floorButtonLights;		// boolean array containing the state of the lights of all the floor buttons in the elevator. Array is indexed from zero so the light for floor 3 is stored at floorButtonLight[2]
	private int MIN_FLOOR;						// Highest floor that this elevator can visit. Remains constant once set
	private int MAX_FLOOR;						// Lowest floor that this elevator can visit. Remains constant once set
	public int id;

	/**
	 * Constructor for ElevatorStatus class
	 *
	 * @param currentFloor		Floor that the elevator is currently at
	 * @param motorState		State of the elevator motor (up, off, down)
	 * @param doorState			State of the elevator doors (open, closed)
	 * @param numFloors			Number of floors in the building
	 */
	public ElevatorStatus(int currentFloor, MotorState motorState, DoorState doorState, int numFloors, int id){
		this.position = currentFloor;
		this.tripDir = Direction.UP;
		this.floorsToVisit = new TreeSet<Integer>();	// TreeSet is an implementation of the SortedSet interface
		this.motorState = motorState;
		this.doorState = doorState;
		this.nextDestination = 1;

		this.MAX_FLOOR = numFloors;		// set the highest floor in the elevator to the number of all floors in the building
		this.MIN_FLOOR = 1;				// set the lowest floor in the elevator to 1

		this.floorButtonLights = new boolean[numFloors];

		// set all the floor button lights to be off
		for(int i = 0; i < numFloors; ++i){
			floorButtonLights[i] = false;
		}

		this.id = id;
	}



	/**
	 * @return The current floor of the elevator
	 */
	public int getPosition() {
		return position;
	}



	/**
	 * Set the current position of the elevator
	 * @param position 	The current floor of the elevator
	 */
	public void setPosition(int position) {
		this.position = position;
		if (this.getFloorsToVisit().contains(Integer.valueOf(position))){
			this.floorsToVisit.remove(Integer.valueOf(position));
			System.out.println("Removed " + position + " from floorsToVisit");
			this.setNextDestination(this.getNextFloor());
		}
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
	 * @return the set of floors for this elevator to visit on the current trip
	 */
	public SortedSet<Integer> getFloorsToVisit() {
		return floorsToVisit;
	}



	/**
	 * Add a floor to the set of floors for this elevator to visit. Also finds and sets the next destination floor
	 *
	 * @param floor		Floor to be added to the set of floors for this elevator to visit
	 */
	public void addFloor(int floor){
		System.out.println("Floor " + floor + " added to list");
		this.floorsToVisit.add((Integer.valueOf(floor)));	// Add the new floor to the sorted set
		this.nextDestination = this.getNextFloor();			// set the next destination floor
	}



	/**
	 * @return the nextDestination
	 */
	public int getNextDestination() {
		return nextDestination;
	}



	/**
	 * @param nextDestination the nextDestination to set
	 */
	public void setNextDestination(int nextDestination) {
		this.nextDestination = nextDestination;
	}


	/**
	 * Get the next destination floor above the current floor
	 * @return The closest floor above the elevator's current floor that is to be visited. Returns null if there are no floors to be visited above
	 */
	private Integer getNextDestFloorAbove(){
		SortedSet<Integer> tempSet;	// Temporary set to store head sets and tail sets

		tempSet = this.floorsToVisit.tailSet(this.position); // Create a set of all floors to be visited above this current floor

		// If there are no floors to visit above this current floor then return null
		if(tempSet.isEmpty()){
			return null;
		} else {
			return tempSet.first();	// Return the closest floor to be visited that is above the current floor
		}
	}


	/**
	 * Get the next destination floor below the current floor
	 * @return The closest floor below the elevator's current floor that is to be visited. Returns null if there are no floors to be visited below
	 */
	private Integer getNextDestFloorBelow(){
		SortedSet<Integer> tempSet;	// Temporary set to store head sets and tail sets

		tempSet = this.floorsToVisit.headSet(this.position); // Create a set of all floors to be visited above this current floor

		// If there are no floors to visit above this current floor then return null
		if(tempSet.isEmpty()){
			return null;
		} else {
			return tempSet.last();	// Return the closest floor to be visited that is above the current floor
		}
	}

	/**
	 * Get the next floor for this elevator to visit. Will try to find the nearest floor in the current direction of the elevator
	 * @return The next floor this elevator has to stop at
	 */
	private Integer getNextFloor(){
		Integer nextFloor = 1;

		// Check if floorsToVisit is empty (no floors for this elevator to visit)
		if (this.floorsToVisit.isEmpty()){	// If true
			return this.getPosition();		// Return the current position of the elevator
		}

		// check if the elevator is on an upwards trip
		if (this.getTripDir() == Direction.UP){
			// check if the elevator is at the top floor
			if (this.position == this.MAX_FLOOR){
				// Change the trip direction of the elevator
				this.setTripDir(Direction.DOWN);

				nextFloor = getNextDestFloorBelow();	// set nextFloor equal to the closest destination floor below the elevator

				// check if there are no destination floors below to visit
				if (nextFloor == null){
					return this.getPosition();	// remain at this position
				} else {
					return nextFloor;			// return the next floor to visit below the current floor
				}
			} else {
				nextFloor = getNextDestFloorAbove();	// set nextFloor equal to the closest destination floor above the current floor

				// check if there are no destination floors above to visit
				if (nextFloor == null){
					this.setTripDir(Direction.DOWN); 		// Set the trip direction to down
					nextFloor = getNextDestFloorBelow();	// Get the next destination floor below the current floor

					// check if there are no destination floors below to visit
					if(nextFloor == null){
						return this.getPosition();	// If true, remain on current floor
					} else {
						return nextFloor;
					}
				} else {
					return nextFloor;
				}
			}
		// Check if elevator is on a downwards trip
		} else if (this.getTripDir() == Direction.DOWN) {
			// Check if elevator is at the bottom floor
			if(this.position == this.MIN_FLOOR){
				this.tripDir = Direction.UP;	// change trip direction to up

				nextFloor = getNextDestFloorAbove();	// set nextFloor equal to the nearest destination floor above the current floor

				// if nextFloor is null, there are no destination floors above the current floor
				if(nextFloor == null){
					return this.getPosition();	// return current position
				} else {
					return nextFloor;	// Else return the next destination floor above the current floor
				}
			} else {
				nextFloor = getNextDestFloorBelow();	// set nextFloor equal to the nearest destination floor below the current floor

				// Check if nextFloor is null (no floors to visit below the current floor)
				if (nextFloor == null){
					this.tripDir = Direction.UP;	// Change the trip direction to up

					nextFloor = getNextDestFloorAbove();	// get the next destination floor above the current floor

					// If no floors to visit above the current floor
					if (nextFloor == null){
						return this.getPosition();	// return the current position
					} else {
						return nextFloor;	// return the next destination floor above the current floor
					}
				} else {
					return nextFloor;
				}
			}
		}

		return nextFloor;	// if logic fails, return 1 to send elevator back to ground floor
	}


	/**
	 * Take a message from the elevator and use it to update the state in elevatorState
	 *
	 * @param p	DataPacket from the elevator
	 */
	public void update(DataPacket p){
		switch(p.getSubSystem()){
			case MOTOR:	// Motor is to be updated
				this.setMotorState(MotorState.convertFromByte(p.getStatus()[0]));
				System.out.println("motor was updated to " + this.getMotorState());
				break;
			case DOOR:	// Door state is to be updated
				this.setDoorState(DoorState.convertFromByte(p.getStatus()[0]));
				System.out.println("door was updated to " + this.getDoorState());
				break;
			case CARLAMP:	// Car Lamp State is to be updated
				//TODO: handle updates from elevator about floor lights. Remove?
				break;
			case LOCATION:	// Location is to be updated
				this.setPosition((int) p.getStatus()[0]);
				System.out.println("position was updated to " + this.getPosition());
				break;
			default:
				break;
		}
	}



	@Override
	public String toString() {
		return "ElevatorStatus [position=" + position + ", tripDir=" + tripDir + ", floorsToVisit=" + floorsToVisit
				+ ", nextDestination=" + nextDestination + ", motorState=" + motorState + ", doorState=" + doorState
				+ ", floorButtonLights=" + Arrays.toString(floorButtonLights) + ", MIN_FLOOR=" + MIN_FLOOR
				+ ", MAX_FLOOR=" + MAX_FLOOR + ", id=" + id + "]";
	}



	public static void main(String args[]) throws UnknownHostException, InterruptedException{
		
		//ElevatorStatus car = new ElevatorStatus(0, MotorState.OFF, DoorState.CLOSED, 7, 1);
		
		LinkedBlockingQueue<DataPacket> input = new LinkedBlockingQueue<DataPacket>();
		LinkedBlockingQueue<DataPacket> output = new LinkedBlockingQueue<DataPacket>();
		
		NewNewScheduler scheduler = new NewNewScheduler(input, output, 1, 7);
		Thread schThread = new Thread(scheduler);
		
		byte[] tempReq = {0,0,0,12, 0,0,0,15, 0,0,0,13, 0,0,0,111, 2, -1};
		
		final int dirIndex = 
		
		schThread.start();
		
		
		try {
			input.add(new DataPacket(OriginType.ELEVATOR, (byte) 0, SubsystemType.LOCATION, new byte[] {(byte) 1}));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		
		// Floor Request:
		// new DataPacket(OriginType.
		
		
		// Set floor to 1
		try {
			input.add(new DataPacket(OriginType.ELEVATOR, (byte) 0, SubsystemType.LOCATION, new byte[] {(byte) 1}));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		// Add floor 4 to visit
		tempReq[]
		try {
			input.add(new DataPacket(OriginType.FLOOR, (byte) 1, SubsystemType.REQUEST, tempReq));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		/*
		car.addFloor(4);
		System.out.println("car = " + car.toString() + "\n");

		car.addFloor(5);
		System.out.println("car = " + car.toString() + "\n");

		car.addFloor(3);
		System.out.println("car = " + car.toString() + "\n");

		car.addFloor(7);
		System.out.println("car = " + car.toString() + "\n");
		
		try {
			input.add(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 2}));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		
		//car.update(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 2}));
		System.out.println("car = " + car.toString() + "\n");

		car.update(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 3}));
		System.out.println("car = " + car.toString() + "\n");

		car.update(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 4}));
		System.out.println("car = " + car.toString() + "\n");

		car.addFloor(3);
		System.out.println("car = " + car.toString() + "\n");

		car.update(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 5}));
		System.out.println("car = " + car.toString() + "\n");

		car.update(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 6}));
		System.out.println("car = " + car.toString() + "\n");

		car.update(new DataPacket(OriginType.ELEVATOR, (byte) 1, SubsystemType.LOCATION, new byte[] {(byte) 7}));
		System.out.println("car = " + car.toString() + "\n");
		*/
	}

}
