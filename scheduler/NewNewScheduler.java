package scheduler;

import java.util.concurrent.BlockingQueue;

import Enums.DoorState;
import Enums.MotorState;
import Enums.OriginType;
import shared.DataPacket;

public class NewNewScheduler {
	
	BlockingQueue<DataPacket> inputBuffer, outputBuffer;
	ElevatorStatus car[];
	
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
	
	
	private void handleNewRequest(DataPacket p){
		
	}
	
	
}
