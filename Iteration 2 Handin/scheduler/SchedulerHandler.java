package scheduler;

import java.net.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private List<TimestampedPacket> echoBuffer;								// Buffer for DataPackets that the scheduler has not received an echo for yet.
	// TODO: Does echoBuffer need to be a blockingqueue?
	
	GenericThreadedSender schedulerSender;		// Sender thread that sends all processed DataPackets to their destination
	GenericThreadedListener schedulerListener;	// Listener thread that listens for DataPackets and places them in the rawInputBuffer
	
	private static final int TIMEOUT_MILLIS = 1000;
	
	public static final SocketAddress FLOOR_PORT_NUMBER = new InetSocketAddress(32);		// Floor port number
	public static final SocketAddress ELEVATOR_PORT_NUMBER = new InetSocketAddress(69);		// Elevator port number
	public static final SocketAddress SCHEDULER_PORT_NUMBER = new InetSocketAddress(23);	// Scheduler port number
	
	
	
	/**
	 * SchedulerHandler constructor
	 */
	public SchedulerHandler(){
		this.rawInputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.rawOutputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.processedInputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.processedOutputBuffer = new PriorityBlockingQueue<DataPacket>();
		this.echoBuffer = new ArrayList<TimestampedPacket>();
	}
	
	
	
	/**
	 * Process one outgoing request from the rawOutputBuffer
	 */
	private void processOutgoingRequest(){
		DataPacket tempPacket = null;
		DataPacket echoPacket = null;
				
		try {
			tempPacket = new DataPacket(rawOutputBuffer.take());	// Take the first element in the raw output queue
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Convert the packet into a replica of the packet it is meant to receive
		switch(tempPacket.getSubSystem()){
			case MOTOR:
				echoPacket = new DataPacket(OriginType.ELEVATOR, tempPacket.getId(), tempPacket.getSubSystem(), tempPacket.getStatus());
				break;
			case DOOR:
				echoPacket = new DataPacket(OriginType.ELEVATOR, tempPacket.getId(), tempPacket.getSubSystem(), tempPacket.getStatus());
				break;
			case CARLAMP:
				echoPacket = new DataPacket(OriginType.ELEVATOR, tempPacket.getId(), tempPacket.getSubSystem(), tempPacket.getStatus());
				break;
			case FLOORLAMP:
				echoPacket = new DataPacket(OriginType.FLOOR, tempPacket.getId(), tempPacket.getSubSystem(), tempPacket.getStatus());
				break;
			default:
				echoPacket = new DataPacket(null, (Byte) null, null, null);
				break;
		}
		
		// Create a TimeStampedPacket from the DataPacket and add it to the echo buffer
		this.echoBuffer.add(new TimestampedPacket(echoPacket));
		
		
		// Add tempPacket to the processedOutputBuffer to be sent
		try {
			this.processedOutputBuffer.put(tempPacket);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Check if the packet is an echo from another subsystem. 
	 * @param p	The DataPacket to be tested
	 * @return	True if the packet is an echo from another subsystem. 
	 */
	private boolean checkIfEcho(DataPacket p){
		Collections.sort(echoBuffer); //Sort 
		for(int i = 0; i < echoBuffer.size(); ++i){
			if (echoBuffer.get(i).getPacket().equals(p)){
				echoBuffer.remove(i);
				return true;
			}
		}
		return false;
	}
	
	
	private void resendTimedOutMessage(DataPacket p){
		p.setOrigin(OriginType.SCHEDULER); // Set the origin type to scheduler
		this.rawOutputBuffer.add(p);	// Add the datapacket to the raw output buffer to be reprocessed and resent
	}
	
	/**
	 * Find the next timed out message in the echoBuffer and resend it
	 */
	private void getTimedOutMessage(){
		// Exit out of the function if there are no packets in the echo Buffer
		if (echoBuffer.size() == 0){
			return;
		}
		
		Instant now = Instant.now();	// Get the current instant
		
		for(int i = 0; i < echoBuffer.size(); ++i){
			Instant timeToCheck = echoBuffer.get(i).getTimeStamp();
			timeToCheck = timeToCheck.plus(TIMEOUT_MILLIS, ChronoUnit.MILLIS);
			
			if (timeToCheck.isBefore(now)){
				this.resendTimedOutMessage(echoBuffer.get(i).getPacket());
				echoBuffer.remove(i);
				return;
			}
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
		
		// Check if the received packet is an echo from another subsystem
		if (!this.checkIfEcho(tempPacket)){
			try {	// If not an echo, add tempPacket to the processedOutputBuffer to be sent
				this.processedInputBuffer.put(tempPacket);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void mainLoop(){
		while(true){
			this.processIncomingRequest();
			this.getTimedOutMessage();
			this.processOutgoingRequest();
		}
	}
	
	public static void main(String args[]){
		SchedulerHandler s = new SchedulerHandler();
		GenericThreadedListener listener = new GenericThreadedListener(s.rawInputBuffer, 23);
		GenericThreadedSender sender = new GenericThreadedSender(s.rawOutputBuffer, s.ELEVATOR_PORT_NUMBER, s.SCHEDULER_PORT_NUMBER, s.FLOOR_PORT_NUMBER);
		s.mainLoop();
	}
}