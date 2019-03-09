package scheduler;

import shared.*;

import java.net.DatagramPacket;
import java.time.Instant;

/**
 * Class for holding elements in the SchedulerHandler buffers. Holds the DatagramPacket and the timestamp for when it was sent / received. 
 * @author Craig Worthington
 *
 */
public class TimestampedPacket implements Comparable {
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
	
	public static int compare(TimestampedPacket p1, TimestampedPacket p2){
		return p1.getTimeStamp().compareTo(p2.getTimeStamp());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packet == null) ? 0 : packet.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimestampedPacket other = (TimestampedPacket) obj;
		if (packet == null) {
			if (other.packet != null)
				return false;
		} else if (!packet.equals(other.packet))
			return false;
		if (timeStamp == null) {
			if (other.timeStamp != null)
				return false;
		} else if (!timeStamp.equals(other.timeStamp))
			return false;
		return true;
	}

	public int compareTo(TimestampedPacket p) {
		return this.getTimeStamp().compareTo(p.getTimeStamp());
	}

	@Override
	public int compareTo(Object o) {
		return this.getTimeStamp().compareTo(((TimestampedPacket) o).getTimeStamp());
	}
	
	
}
