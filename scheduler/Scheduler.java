package scheduler;

import java.util.concurrent.BlockingQueue;

import Enums.Direction;
import Enums.DoorState;
import Enums.MotorState;
import Enums.OriginType;
import Enums.SubsystemType;
import shared.DataPacket;

// TODO: Add state where elevator is at first floor waiting with doors open for elevator to visit
public class Scheduler implements Runnable {

	BlockingQueue<DataPacket> inputBuffer, outputBuffer;
	public ElevatorStatus car[];

	//private static final int FLOOR_INDEX = 17;
	private static final int DIR_INDEX = 16;

	// TODO: send message to floors to update lamp

	/**
	 * Constructor for Scheduler
	 * @param inputBuffer	BlockingQueue used in SchedulerHandler for storing messages to be sent to the scheduler
	 * @param outputBuffer	BlockingQueue used in SchedulerHandler for storing messages that were sent by the handler
	 * @param numElevators	Number of elevators in the system
	 * @param numFloors		Number of floors in the system
	 */
	public Scheduler(BlockingQueue<DataPacket> inputBuffer, BlockingQueue<DataPacket> outputBuffer, int numElevators, int numFloors){
		this.inputBuffer = inputBuffer;
		this.outputBuffer = outputBuffer;
		this.car = new ElevatorStatus[numElevators];

		// Initialize each ElevatorStatus to being on the first floor with the motors off and the doors closed
		for(int i = 0; i < numElevators; ++i){
			this.car[i] = new ElevatorStatus(1, MotorState.OFF, DoorState.CLOSED, numFloors, i);
		}
	}


	/*
	 * To access info from the inputBuffer:
	 * DataPacket packet = new DataPacket(inputBuffer.take());
	 *
	 */

	public ElevatorStatus[] getCar() {
		return car;
	}

	public ElevatorStatus getCar(int index) {
		return car[index];
	}

	public void setCar(ElevatorStatus[] car) {
		this.car = car;
	}

	/**
	 * Handle an input from the inputBuffer
	 */
	private void handleInput(){
		DataPacket input = new DataPacket(null, (byte) 0, null, null);
		System.out.println("\n\nLISTENING");
		try {
			input = new DataPacket(inputBuffer.take());		// Take the next input from the databuffer
		} catch (InterruptedException ie) {
			System.err.println(ie);
		}
		System.out.println("INPUT RETRIEVED");



		// If the input was a request from a floor
		if(input.getOrigin() == OriginType.FLOOR){
			if ((input.getSubSystem() == SubsystemType.REQUEST) || (input.getSubSystem() == SubsystemType.INPUT)) {
				System.out.println("GOING TO FLOOR");
				this.handleNewRequest(input); 	// Send request to handleNewRequest
			}
		}


		// If the input came from an elevator
		if (input.getOrigin() == OriginType.ELEVATOR){
			try {
				this.car[(int) input.getId()].update(input);	// Update the elevatorStatus with the input
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				System.out.println("Elevator with id " + input.getId() + " does not exist");
			}

			this.sendNextStep(input);						// Find the next step for the elevator to do and send it
		}
	}


	/**
	 * Find the nearest elevator to the given floor that is travelling in the correct direction
	 *
	 * @param floor		The floor on which the request was made
	 * @param dir		The direction in which the request is
	 * @return			The elevator which can best serve this request
	 */
	// TODO: look for idle elevators
	private int findNearestElevator(int floor, Direction dir){
		int carNum = 0;	// Current best candidate to serve the request
		
		// Cycle through all elevators
		for(int i = 0; i < car.length; ++i){
			if (dir == Direction.UP){
				// If car is below the given floor and is traveling up
				if ((car[i].getPosition() < floor) && (car[i].getTripDir() == Direction.UP)){
					// If car is above the current best candidate
					if (car[i].getPosition() > car[carNum].getPosition()){
						carNum = i;
					}
				}

			} else if (dir == Direction.DOWN){
				// If car is above the given floor and is traveling down
				if ((car[i].getPosition() > floor) && (car[i].getTripDir() == Direction.DOWN)){
					// If car is below the current best candidate
					if (car[i].getPosition() > car[carNum].getPosition()){
						carNum = i;
					}
				}
			}

		}

		return carNum;
	}

	/**
	 * Handle a new request for an elevator to visit a floor
	 * @param p	The DataPacket that contains the request
	 */
	private void handleNewRequest(DataPacket p){
		System.out.println("handleNewRequest: " + p.toString());
		
		// check if request came from the floor (up/down)
		if (p.getSubSystem() == SubsystemType.REQUEST){
			System.out.println("THIS IS A REQUEST FOR AN ELEVATOR");
			car[findNearestElevator((int) p.getId(), Direction.convertFromByte(p.getStatus()[DIR_INDEX]))].addFloor((int) p.getId());
		}
		else if (p.getSubSystem() == SubsystemType.INPUT){					// If request came from inside the elevator, the subsystem is input
			System.out.println("REQUEST CAME FROM FLOOR: " + p.getId());
			// Find elevator that is on the same floor as the request
			for (int i=2; i<p.getStatus()[1]+2; i++) {
				car[p.getStatus()[0]].addFloor(p.getStatus()[i]);
			}
		}
	}

	/**
	 * Send the next step in the process back to the elevator
	 * @param 	p	The DataPacket retrieved from the inputBuffer
	 */
	private void sendNextStep(DataPacket p) {
		DataPacket returnPacket = new DataPacket(OriginType.SCHEDULER, p.getId(), null, null); // Packet to return to the elevator.
		
		System.out.println("car = " + car[(int) p.getId()].toString());

		if(car[(int) p.getId()].testIfIdle()){
			System.err.println("elevator idle!");
			// TODO: Update something here for when elevator is idle for long period of time?
			car[(int) p.getId()].setIdle(true);		// Set the elevator as being idle
			
		} else {
			System.out.println("elevator not idle");
			car[(int) p.getId()].setIdle(false);	// Set the elevator as not being idle	
		}
		
		if (car[(int) p.getId()].getIdle()) {
			// do nothing since elevator is idle
			System.out.println("Elevator idle, doing nothing");
		}
		// If the echo was from the motor system
		else if (p.getSubSystem() == SubsystemType.MOTOR) {

			// If elevator was told to stop, open the doors
			if (p.getStatus()[0] == MotorState.OFF.getByte()) {
				// create a DataPacket to open the doors for the elevator
				returnPacket.setSubSystem(SubsystemType.DOOR);
				returnPacket.setStatus(new byte[] {DoorState.OPEN.getByte()});
				try {
					outputBuffer.put(new DataPacket(OriginType.SCHEDULER, (byte) car[(int) p.getId()].getPosition(), SubsystemType.FLOORLAMP,new byte[] {car[(int) p.getId()].getTripDir().getByte(),p.getId()}));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} // If the echo was from the door system
		else if (p.getSubSystem() == SubsystemType.DOOR) {
			// If elevator has successfully closed its doors
			if(p.getStatus()[0] == DoorState.CLOSED.getByte()) {
				// TODO: make sure elevator is going in correct direction
				// send new motor direction to the elevator
				// If elevator is on an upwards trip
				if(car[(int) p.getId()].getTripDir() == Direction.UP) {
					returnPacket.setSubSystem(SubsystemType.MOTOR);
					returnPacket.setStatus(new byte[] {MotorState.UP.getByte()});
				} // If elevator is on a downwards trip
				else if (car[(int) p.getId()].getTripDir() == Direction.DOWN) {
					returnPacket.setSubSystem(SubsystemType.MOTOR);
					returnPacket.setStatus(new byte[] {MotorState.DOWN.getByte()});
				}
			} // If elevator has opened the doors
			else if (p.getStatus()[0] == DoorState.OPEN.getByte()){
				// Tell elevator to close doors
				returnPacket.setSubSystem(SubsystemType.DOOR);
				returnPacket.setStatus(new byte[] {DoorState.CLOSED.getByte()});
			}

		} // If the packet is the current location of the elevator
		else if (p.getSubSystem() == SubsystemType.LOCATION) {
			// if car has reached destination
			System.out.println("testing if car is at destination");
			System.out.println("p.getStatus[0] = " + p.getStatus()[0] + " car[(int) p.getId()].getNextDestination() = " + car[(int) p.getId()].getNextDestination());
			if ((int) p.getStatus()[0] == car[(int) p.getId()].getNextDestination()) {
				returnPacket.setSubSystem(SubsystemType.MOTOR);
				returnPacket.setStatus(new byte[] {MotorState.OFF.getByte()});

				// remove this destination from the list of destinations for the car to visit
				car[(int) p.getId()].findNextDestination();
			}
		}

		System.out.println("Input packet = " + p.toString());
		System.out.println("Return packet = " + returnPacket.toString());

		// If the returnPacket has been modified, then add it to the output buffer to be sent
		if ((returnPacket.getSubSystem() != null) && (returnPacket.getStatus() != null)) {
			try {
				outputBuffer.add(returnPacket);
			} catch (IllegalStateException ise) {
				ise.printStackTrace();
				System.exit(0);
			}
		} else {
			System.out.println("returnPacket is null, not creating return message");
		}
	}

	@Override
	public void run() {
		System.out.println("Starting scheduler");
		while(true) {
			handleInput();
		}
	}

}
