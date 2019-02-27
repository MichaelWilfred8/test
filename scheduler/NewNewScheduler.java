package scheduler;

import java.util.concurrent.BlockingQueue;

import Enums.Direction;
import Enums.DoorState;
import Enums.MotorState;
import Enums.OriginType;
import Enums.SubsystemType;
import shared.DataPacket;

public class NewNewScheduler implements Runnable {
	
	BlockingQueue<DataPacket> inputBuffer, outputBuffer;
	ElevatorStatus car[];
	
	//TODO: send messaage to open/close doors, send message to toggle motor
	
	public NewNewScheduler(BlockingQueue<DataPacket> inputBuffer, BlockingQueue<DataPacket> outputBuffer, int numElevators, int numFloors){
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
	
	/**
	 * Handle an input from the inputBuffer
	 */
	private void handleInput(){
		DataPacket input = new DataPacket(null, (byte) 0, null, null);
		
		try {
			input = new DataPacket(inputBuffer.take());		// Take the next input from the databuffer
		} catch (InterruptedException ie) {
			System.err.println(ie);
		}
		
		// If the input was a request from a floor
		if(input.getOrigin() == OriginType.FLOOR){
			if (input.getSubSystem() == SubsystemType.REQUEST) {
				this.handleNewRequest(input); 	// Send request to handleNewRequest
			}
		}
		
		
		// If the input came from an elevator
		if (input.getOrigin() == OriginType.ELEVATOR){
			this.car[(int) input.getId()].update(input);	// Update the elevatorStatus with the input
			// TODO: get next step from elevatorStatus
			this.sendNextStep(input);
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
	// TODO: Ensure that this fits the format used for requests
	private void handleNewRequest(DataPacket p){
		// If request came from inside elevator, then add request to set inside elevatorStatus
		// TODO: Request for an elevator to floor has -1 in status[0]
		if (p.getOrigin() == OriginType.ELEVATOR) {
			car[(int) p.getId()].addFloor((int) p.getStatus()[0]);
		}
		// If request came from outside elevator, then findNearestElevator and add request to the queue
		else if (p.getOrigin() == OriginType.FLOOR) {
			car[findNearestElevator((int) p.getStatus()[0], Direction.convertFromByte(p.getStatus()[1]))].addFloor((int) p.getStatus()[0]);
		}
	}
	
	/**
	 * Send the next step in the process back to the elevator
	 * @param 	p	The DataPacket retrieved from the inputBuffer
	 */
	private void sendNextStep(DataPacket p) {
		DataPacket returnPacket = new DataPacket(OriginType.SCHEDULER, p.getId(), null, null); // Packet to return to the elevator.
		
		// If the echo was from the motor system
		if (p.getSubSystem() == SubsystemType.MOTOR) {
			// If the echo was the motor turning off and the elevator has arrived at its destination floor
			if((p.getStatus()[0] == MotorState.OFF.getByte()) && (car[(int) p.getId()].getPosition() == car[(int) p.getId()].getNextDestination())){
				// create a DataPacket to open the doors for the elevator
				returnPacket.setSubSystem(SubsystemType.DOOR);
				returnPacket.setStatus(new byte[] {DoorState.OPEN.getByte()});
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
			}
		// If the packet is the current location of the elevator
		} else if (p.getSubSystem() == SubsystemType.LOCATION) {
			// Update the car status with the location
			car[(int) p.getId()].update(p);
			
			// if car has reached destination
			if ((int) p.getStatus()[0] == car[(int) p.getId()].getNextDestination()) {
				returnPacket.setSubSystem(SubsystemType.MOTOR);
				returnPacket.setStatus(new byte[] {MotorState.OFF.getByte()});
			}
		}
		
		// If the returnPacket has been modified, then add it to the output buffer to be sent
		if ((returnPacket.getSubSystem() != null) && (returnPacket.getStatus() != null)) {
			try {
				outputBuffer.add(returnPacket);
			} catch (IllegalStateException ise) {
				ise.printStackTrace();
				System.exit(0);
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Starting scheduler");
		while(true) {
			handleInput();
		}
	}
	
}