package scheduler;

import java.net.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import Enums.DoorState;
import Enums.OriginType;
import Enums.SubsystemType;
import shared.*;

/**
 * Handles all incoming and outgoing requests for the Scheduler. Stores requests in three separate buffers
 * 
 * 
 * @author Craig Worthington
 *
 */

// TODO: check echos,  
public class SchedulerHandler {
	
	
	BlockingQueue<DataPacket> rawInputBuffer, rawOutputBuffer;					// Buffer for DataPackets that have not been processed by the handler
	BlockingQueue<DataPacket> processedInputBuffer, processedOutputBuffer;		// Buffer for DataPackets that have been processed by the handler and are ready to be sent
	BlockingQueue<TimestampedPacket> echoBuffer;								// Buffer for DataPackets that the scheduler has not received an echo for yet.
	// TODO: Does echoBuffer need to be a blockingqueue?
	
	GenericThreadedSender schedulerSender;		// Sender thread that sends all processed DataPackets to their destination
	GenericThreadedListener schedulerListener;	// Listener thread that listens for DataPackets and places them in the rawInputBuffer
	
	
	public SchedulerHandler(){
		this.rawInputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.rawOutputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.processedInputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.processedOutputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.echoBuffer = new PriorityBlockingQueue<TimestampedPacket>();
	}
	
	
	public void checkForEchos(){
		
	}
	
	/**
	 * Process one outgoing request from the rawOutputBuffer
	 */
	private void processOutgoingRequest(){
		DataPacket tempPacket = null;
		
		try {
			tempPacket = new DataPacket(rawOutputBuffer.take());	// Take the first element in the raw output queue
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Create a TimeStampedPacket from the DataPacket and add it to the echo buffer
		this.echoBuffer.add(new TimestampedPacket(tempPacket));
		
		
		// Add tempPacket to the processedOutputBuffer to be sent
		try {
			this.processedOutputBuffer.put(tempPacket);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void processIncomingRequest(){
		DataPacket tempPacket = null;
		
		try {
			tempPacket = new DataPacket(rawInputBuffer.take());	// Take the first element in the raw input queue
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Create a TimeStampedPacket from the DataPacket and add it to the echo buffer
		this.echoBuffer.add(new TimestampedPacket(tempPacket));
		
		
		// Add tempPacket to the processedOutputBuffer to be sent
		try {
			this.processedOutputBuffer.put(tempPacket);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
