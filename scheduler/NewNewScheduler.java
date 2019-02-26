package scheduler;

import java.util.concurrent.BlockingQueue;

import Enums.Direction;
import Enums.DoorState;
import Enums.MotorState;
import Enums.OriginType;
import shared.DataPacket;

public class NewNewScheduler {
	
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
	
	private void handleInput(){
		DataPacket input = new DataPacket(null, (byte) 0, null, null);
		
		try {
			input = new DataPacket(inputBuffer.take());		// Take the next input from the databuffer
		} catch (InterruptedException ie) {
			System.err.println(ie);
		}
		
		// If the input came from an elevator
		if (input.getOrigin() == OriginType.ELEVATOR){
			this.car[(int) input.getId()].update(input);	// Update the elevatorStatus with the input
			// TODO: get next step from elevatorStatus
		}
		
		// If the input was a request from a floor
		if(input.getOrigin() == OriginType.FLOOR){
			this.handleNewRequest(input);
		}
		
	}
	
	
	/**
	 * Find the nearest elevator to the given floor that is travelling in the correct direction
	 * 
	 * @param floor		The floor on which the request was made
	 * @param dir		The direction in which the request is
	 * @return			The elevator which can best serve this request
	 */
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
	
	private void handleNewRequest(DataPacket p){
		// If request came from inside elevator, then add request to set inside elevatorStatus
		
		
		// If request came from outside elevator, then findNearestElevator and add request to the queue
	}
	
	
}
