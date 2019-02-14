package scheduler;

import shared.*;

import java.net.DatagramPacket;
import java.time.Instant;

/**
 * Class for holding elements in the SchedulerHandler buffers. Holds the DatagramPacket and the timestamp for when it was sent / received. 
 * @author Craig Worthington
 *
 */
public class TimestampedPacket {
	private Instant timeStamp;
	private DataPacket packet;
	
	/**
	 * Creates a new Timestamped Packet and sets the timestamp to the moment it was created
	 * 
	 * @param p	DataPacket to be sent
	 */
	public TimestampedPacket(DataPacket p){
		this.timeStamp = Instant.now();
		this.packet = p;
	}

	/**
	 * @return the timeStamp
	 */
	public Instant getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(Instant timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the packet
	 */
	public DataPacket getPacket() {
		return packet;
	}

	/**
	 * @param packet the packet to set
	 */
	public void setPacket(DataPacket packet) {
		this.packet = packet;
	}
}