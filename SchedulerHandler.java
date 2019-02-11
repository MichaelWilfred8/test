import java.net.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import Enums.DoorState;
import Enums.OriginType;
import Enums.SubsystemType;

/**
 * Handles all incoming and outgoing requests for the Scheduler. Stores requests in three separate buffers
 * 
 * 
 * @author Craig Worthington
 *
 */
public class SchedulerHandler {
	
	
	DatagramPacket sendPacket;
	DatagramPacket receivePacket;
	
	PriorityBlockingQueue<TimestampedPacket> inputBuffer, outputBuffer, echoBuffer;
	
	ArrayList<SocketAddress> ElevatorAddress, FloorAddress;
	
	
	public SchedulerHandler(){
		this.inputBuffer = new PriorityBlockingQueue<TimestampedPacket>();
		this.outputBuffer = new PriorityBlockingQueue<TimestampedPacket>();
		this.echoBuffer = new PriorityBlockingQueue<TimestampedPacket>();
	}


	/**
	 * @return the sendPacket
	 */
	public DatagramPacket getSendPacket() {
		return sendPacket;
	}


	/**
	 * @param sendPacket the sendPacket to set
	 */
	public void setSendPacket(DatagramPacket sendPacket) {
		this.sendPacket = sendPacket;
	}


	/**
	 * @return the receivePacket
	 */
	public DatagramPacket getReceivePacket() {
		return receivePacket;
	}


	/**
	 * @param receivePacket the receivePacket to set
	 */
	public void setReceivePacket(DatagramPacket receivePacket) {
		this.receivePacket = receivePacket;
	}


	/**
	 * @return the inputBuffer
	 */
	public PriorityBlockingQueue<TimestampedPacket> getInputBuffer() {
		return inputBuffer;
	}


	/**
	 * @param inputBuffer the inputBuffer to set
	 */
	public void setInputBuffer(PriorityBlockingQueue<TimestampedPacket> inputBuffer) {
		this.inputBuffer = inputBuffer;
	}


	/**
	 * @return the outputBuffer
	 */
	public PriorityBlockingQueue<TimestampedPacket> getOutputBuffer() {
		return outputBuffer;
	}


	/**
	 * @param outputBuffer the outputBuffer to set
	 */
	public void setOutputBuffer(PriorityBlockingQueue<TimestampedPacket> outputBuffer) {
		this.outputBuffer = outputBuffer;
	}


	/**
	 * @return the echoBuffer
	 */
	public PriorityBlockingQueue<TimestampedPacket> getEchoBuffer() {
		return echoBuffer;
	}


	/**
	 * @param echoBuffer the echoBuffer to set
	 */
	public void setEchoBuffer(PriorityBlockingQueue<TimestampedPacket> echoBuffer) {
		this.echoBuffer = echoBuffer;
	}
	
	public void checkForEchos(){
		
	}
	
	/**
	 * Prepare a request for sending and place it in the Output buffer for SchedulerSender to send
	 * 
	 * @param packet		DataPacket to be sent
	 * @param destination	Subsystem to send the packet to
	 * @param id			ID of the element in the subsystem to send it to
	 */
	public void sendRequest(DataPacket packet, OriginType destination, int id){
		
	}
	
}
