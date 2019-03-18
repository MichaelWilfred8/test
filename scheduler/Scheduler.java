package scheduler;

import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import Enums.*;
import shared.DataPacket;
import testing.ColouredOutput;

// TODO: Add state where elevator is at first floor waiting with doors open for elevator to visit

// BUG: Elevator is getting messages twice, and this messes everything up
// TODO: Add list for up requests and down requests

public class Scheduler implements Runnable {

	BlockingQueue<DataPacket> inputBuffer, outputBuffer;
	public ElevatorStatus car[];
	SortedSet<Integer> upRequests, downRequests;
	

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
			this.car[i] = new ElevatorStatus(1, MotorState.OFF, DoorState.OPEN, numFloors, i);
		}
		
		this.upRequests = new TreeSet<Integer>();
		this.downRequests = new TreeSet<Integer>();
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
		ColouredOutput.printColouredText("current time =  " + LocalDateTime.now().toString(), ColouredOutput.ANSI_YELLOW);
		ColouredOutput.printColouredText("inputBuffer = " + this.inputBuffer.toString(), ColouredOutput.ANSI_YELLOW);
		//System.out.println("Car[0] = " + car[0].toString());
		
		DataPacket input = new DataPacket(null, (byte) 0, null, null);
		try {
			input = new DataPacket(inputBuffer.take());		// Take the next input from the databuffer
		} catch (InterruptedException ie) {
			System.err.println(ie);
		}

		ColouredOutput.printColouredText("input packet = " + input.toString(), ColouredOutput.ANSI_YELLOW);
		
		
		// If the input was a request from a floor
		if(input.getOrigin() == OriginType.FLOOR){
			if ((input.getSubSystem() == SubsystemType.REQUEST) || (input.getSubSystem() == SubsystemType.INPUT)) {
				ColouredOutput.printColouredText("Handling new request", ColouredOutput.ANSI_YELLOW);
				this.handleNewRequest(input); 	// Send request to handleNewRequest
			}
		}


		// If the input came from an elevator
		if (input.getOrigin() == OriginType.ELEVATOR){
			this.printAllCarStatus("Before");
			try {
				ColouredOutput.printColouredText("Updating elevatorStatus of car " + (int) input.getId(), ColouredOutput.ANSI_YELLOW);
				this.car[(int) input.getId()].update(input);	// Update the elevatorStatus with the input
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				System.out.println("Elevator with id " + input.getId() + " does not exist");
			}

			this.sendNextStep(input);						// Find the next step for the elevator to do and send it
			this.printAllCarStatus("After");
		}
		
		ColouredOutput.printColouredText("Output = " + this.outputBuffer.toString(), ColouredOutput.ANSI_YELLOW);
		
	}
	
	
	private void printAllCarStatus(String prefix) {
		for(int i = 0; i < car.length; ++i) {
			ColouredOutput.printColouredText(prefix + " Car[" + i + "] = " + car[i].toString() + "\n", ColouredOutput.ANSI_YELLOW);
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
			if (car[i].isInoperable()){
				// Do not use elevator i since it is out of operation
			} else if (dir == Direction.UP){
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
		int selectedCar = 0;
		boolean wasIdle = false;
		
		//ColouredOutput.printColouredText("In handleNewRequest", ColouredOutput.ANSI_BLACK);
		
		// check if request came from the floor (up/down)
		if (p.getSubSystem() == SubsystemType.REQUEST){
			selectedCar = findNearestElevator((int) p.getId(), Direction.convertFromByte(p.getStatus()[DIR_INDEX]));
			//ColouredOutput.printColouredText("Request from floor", ColouredOutput.ANSI_BLACK);
			
			if (car[selectedCar].getIdle() == true){
				//ColouredOutput.printColouredText("setting wasIdle true", ColouredOutput.ANSI_BLACK);
				wasIdle = true;
			}
			
			// Check if the request was an up request or a down request, and add it to the sorted set
			if (p.getStatus()[DIR_INDEX] == Direction.UP.getByte()) {
				this.upRequests.add((int) p.getId());
			} else if (p.getStatus()[DIR_INDEX] == Direction.DOWN.getByte()) {
				this.downRequests.add((int) p.getId());
			}
			
			car[selectedCar].addFloor((int) p.getId());
			
			
			// If elevator is already at the floor that the request originated from, send a message back to floor
			try {
				outputBuffer.put(new DataPacket(OriginType.SCHEDULER, (byte) car[selectedCar].getPosition(), SubsystemType.FLOORLAMP,new byte[] {car[selectedCar].getTripDir().getByte(), (byte) selectedCar}));
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		else if (p.getSubSystem() == SubsystemType.INPUT){
			selectedCar = (int)p.getStatus()[0];	// set selectedCar as the elevator which was specified in request
			//ColouredOutput.printColouredText("Request from elevator", ColouredOutput.ANSI_BLACK);
			
			if (car[selectedCar].getIdle() == true){
				//ColouredOutput.printColouredText("setting wasIdle true", ColouredOutput.ANSI_BLACK);
				wasIdle = true;
			}
			for (int i=2; i<p.getStatus()[1]+2; i++) {
				car[p.getStatus()[0]].addFloor(p.getStatus()[i]);
				car[p.getStatus()[0]].setFloorButtonLight(p.getStatus()[i], true);
			}
		}
		
		// Test if car was idle and is now no longer idle
		if ((wasIdle == true) && (car[selectedCar].testIfIdle() == false)) {
			//ColouredOutput.printColouredText("wasIdle is true, returning packet", ColouredOutput.ANSI_BLACK);
			DataPacket returnPacket = new DataPacket(OriginType.SCHEDULER, (byte) selectedCar, null, null); // Create a DataPacket to return to the elevator.
			
			returnPacket.setSubSystem(SubsystemType.DOOR);
			returnPacket.setStatus(new byte[] {DoorState.CLOSED.getByte()});
			
			// Add packet to output queue and exit
			try {
				this.outputBuffer.put(returnPacket);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
						
			//ColouredOutput.printColouredText("ADDED REQUEST TO OUTPUT BUFFER: " + returnPacket.toString(), ColouredOutput.ANSI_BLACK);
		} else {
			//ColouredOutput.printColouredText("wasIdle not true, not sending packet", ColouredOutput.ANSI_BLACK);
		}
		
		return;
	}


	/**
	 * Send the next step in the process back to the elevator
	 * @param 	p	The DataPacket retrieved from the inputBuffer
	 */
	private void sendNextStep(DataPacket p) {
		DataPacket returnPacket = new DataPacket(OriginType.SCHEDULER, p.getId(), null, null); // Create a DataPacket to return to the elevator.
		//ColouredOutput.printColouredText("In sendNextStep", ColouredOutput.ANSI_GREEN);
		ColouredOutput.printColouredText("sendNextStep car = " + car[(int) p.getId()].toString(), ColouredOutput.ANSI_GREEN);
		//System.out.println("car = " + car[(int) p.getId()].toString());


		if(car[(int) p.getId()].testIfIdle()){
			//ColouredOutput.printColouredText("elevator idle!", ColouredOutput.ANSI_GREEN);
			//System.out.println("elevator idle!");
			// TODO: Update something here for when elevator is idle for long period of time?
			car[(int) p.getId()].setIdle(true);		// Set the elevator as being idle

		} else {
			//ColouredOutput.printColouredText("elevator not idle", ColouredOutput.ANSI_GREEN);
			//System.out.println("elevator not idle");
			car[(int) p.getId()].setIdle(false);	// Set the elevator as not being idle
		}

		// Test if the elevator is idle
		if (car[(int) p.getId()].getIdle()) {
			// do nothing since elevator is idle
			//ColouredOutput.printColouredText("Elevator idle, doing nothing", ColouredOutput.ANSI_GREEN);
			//System.out.println("Elevator idle, doing nothing");
		}
		else if (car[(int) p.getId()].isInoperable() == true){
			// Do nothing since the elevator is inoperable
			//ColouredOutput.printColouredText("Elevator " + (int) p.getId() + " is inoperable", ColouredOutput.ANSI_GREEN);
			//System.out.println("Elevator " + (int) p.getId() + " is inoperable");
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
			//ColouredOutput.printColouredText("testing if car is at destination", ColouredOutput.ANSI_GREEN);
			//ColouredOutput.printColouredText("p.getStatus[0] = " + p.getStatus()[0] + " car[(int) p.getId()].getNextDestination() = " + car[(int) p.getId()].getNextDestination(), ColouredOutput.ANSI_GREEN);
			//System.out.println("testing if car is at destination");
			//System.out.println("p.getStatus[0] = " + p.getStatus()[0] + " car[(int) p.getId()].getNextDestination() = " + car[(int) p.getId()].getNextDestination());
			// if car has reached destination
			if ((int) p.getStatus()[0] == car[(int) p.getId()].getNextDestination()) {
				// set the return packet to turn off the motor
				returnPacket.setSubSystem(SubsystemType.MOTOR);
				returnPacket.setStatus(new byte[] {MotorState.OFF.getByte()});

				// remove this destination from the list of destinations for the car to visit
				car[(int) p.getId()].findNextDestination();
				// remove the floorButtonLight for this floor
				car[(int) p.getId()].setFloorButtonLight(p.getStatus()[0], false);
			}
		}
		
		//ColouredOutput.printColouredText("Input packet = " + p.toString(), ColouredOutput.ANSI_GREEN);
		//ColouredOutput.printColouredText("Return packet = " + returnPacket.toString(), ColouredOutput.ANSI_GREEN);
		//System.out.println("Input packet = " + p.toString());
		//System.out.println("Return packet = " + returnPacket.toString());

		// If the returnPacket has been modified, then add it to the output buffer to be sent
		if ((returnPacket.getSubSystem() != null) && (returnPacket.getStatus() != null)) {
			try {
				//ColouredOutput.printColouredText("Sending next step " + returnPacket.toString(), ColouredOutput.ANSI_GREEN_BACKGROUND);
				outputBuffer.add(returnPacket);
			} catch (IllegalStateException ise) {
				ise.printStackTrace();
				System.exit(0);
			}
		} else {
			//ColouredOutput.printColouredText("returnPacket is null, not creating return message", ColouredOutput.ANSI_GREEN_BACKGROUND);
			//System.out.println("returnPacket is null, not creating return message");
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
