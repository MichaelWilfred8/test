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
	
	
	public BlockingQueue<DataPacket> rawInputBuffer, rawOutputBuffer;					// Buffer for DataPackets that have not been processed by the handler
	public BlockingQueue<DataPacket> processedInputBuffer, processedOutputBuffer;		// Buffer for DataPackets that have been processed by the handler and are ready to be sent
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
		
		// TODO: convert the packet into a replica of the packet it is meant to receive
		
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
	
	
	/**
	 * Process one incoming request from the rawInputBuffer
	 */
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
			this.processedInputBuffer.put(tempPacket);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		SchedulerHandler s = new SchedulerHandler();
		GenericThreadedListener listener = new GenericThreadedListener(s.rawInputBuffer, 43);
		GenericThreadedSender sender = new GenericThreadedSender(s.rawOutputBuffer, )
	}
}
