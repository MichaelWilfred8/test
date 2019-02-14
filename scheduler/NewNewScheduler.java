package scheduler;

import java.util.concurrent.BlockingQueue;

import shared.DataPacket;

public class NewNewScheduler {
	
	BlockingQueue<DataPacket> inputBuffer, outputBuffer;
	ElevatorStatus car[];
	
	public NewNewScheduler(BlockingQueue<DataPacket> inputBuffer, BlockingQueue<DataPacket> outputBuffer){
		this.inputBuffer = inputBuffer;
		this.outputBuffer = outputBuffer;
	}
	
	/*
	 * To access info from the inputBuffer:
	 * DataPacket packet = new DataPacket(inputBuffer.take());
	 * 
	 */
	
	
}
