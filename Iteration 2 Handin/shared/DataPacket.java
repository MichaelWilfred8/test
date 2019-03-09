package shared;

import java.nio.ByteBuffer;
import java.util.Arrays;
import Enums.*;


/**
 * DataPacket object for sending information between subsystems. Includes the source of the message (elevator, floor, or scheduler),
 * the id of the source (i.e. car 2, floor 7, etc), the subsystem within the object that the message is addressing (i.e. the motor 
 * of the elevator, the direction light on the floor) and then the status of this object.
 * 
 * @author Craig Worthington
 *
 */

public class DataPacket implements Cloneable {

	OriginType origin;
	byte id;
	SubsystemType subSystem;
	byte[] status;

	private static final int ORIGIN_INDEX = 0;
	private static final int ID_INDEX = 1;
	private static final int SUBSYSTEM_INDEX = 2;
	private static final int STATUS_INDEX = 3;


	/**
	 * Constructor for DataPacket object
	 * 
	 * @param origin		Origin subsystem of the DataPacket
	 * @param id			ID of the element in the subsystem (i.e. elevator car 2, floor 7)
	 * @param subSystem		The element within the subsystem that is to be addressed
	 * @param status		The message to send to or to be received by that subsystem
	 */
	public DataPacket(OriginType origin, byte id, SubsystemType subSystem, byte[] status){
		this.origin = origin;
		this.id = id;
		this.subSystem = subSystem;
		this.status = status;
	}



	/**
	 * Constructor for DataPacket object that fills each field based the byte array given as a parameter
	 * @param b		Byte array that follows the DataPacket getBytes format
	 */
	public DataPacket(byte[] b){
		this.origin = OriginType.convertFromByte(b[ORIGIN_INDEX]);
		this.id = b[ID_INDEX];
		this.subSystem = SubsystemType.convertFromByte(b[SUBSYSTEM_INDEX]);
		this.status = Arrays.copyOfRange(b, STATUS_INDEX, b.length); 
	}
	
	
	/**
	 * Copy Constructor for DataPacket. Creates a brand new DataPacket object with the same values as p but 
	 * @param p	DataPacket to create a deep copy of
	 */
	public DataPacket(DataPacket packet){
		this.origin = packet.getOrigin();
		this.id = packet.getId();
		this.subSystem = packet.getSubSystem();
		this.status = new byte[packet.getStatus().length];
		System.arraycopy(packet.getStatus(), 0, this.status, 0, packet.getStatus().length);
	}

	/**
	 * Contains a reference of DataPacket and implements clone with a deep copy
	 * @return A deep copy of a DatagramPacket
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException{
		// Assign the shallow copy to new reference variable packet
		DataPacket p = (DataPacket) super.clone();
		
		return super.clone();
	}
	
	/**
	 * @return the origin
	 */
	public OriginType getOrigin() {
		return origin;
	}



	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(OriginType origin) {
		this.origin = origin;
	}



	/**
	 * @return the id
	 */
	public byte getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(byte id) {
		this.id = id;
	}

	/**
	 * @return the subSystem
	 */
	public SubsystemType getSubSystem() {
		return subSystem;
	}



	/**
	 * @param subSystem the subSystem to set
	 */
	public void setSubSystem(SubsystemType subSystem) {
		this.subSystem = subSystem;
	}



	/**
	 * @return the status
	 */
	public byte[] getStatus() {
		return status;
	}



	/**
	 * @param status the status to set
	 */
	public void setStatus(byte[] status) {
		this.status = status;
	}



	/**
	 * Generate a byte array of all the information in the data packet
	 * @return byte array containing the information in the data packet
	 */
	public byte[] getBytes(){
		ByteBuffer buf = ByteBuffer.allocate(100);
		buf.put(this.origin.getByte());
		buf.put(this.id);
		buf.put(this.subSystem.getByte());
		buf.put(this.status);

		buf.flip();

		return buf.array();
	}
	
	public boolean equals(OriginType origin, int id, SubsystemType subSystem, byte[] status){
		if (!origin.equals(this.origin)){
			System.out.println("Origin");
			return false;
		} else if (id != this.id) {
			System.out.println("id");
			return false;
		} else if (!subSystem.equals(this.subSystem)) {
			System.out.println("subsystem");
			return false;
		} else if (!Arrays.equals(Arrays.copyOfRange(status, 0, this.status.length), this.status)) {
			byte [] statusCopy = Arrays.copyOfRange(status, 0, this.status.length);
			
			for(int i = 0; i < Math.min(statusCopy.length, this.status.length); ++i){
				if (statusCopy[i] != this.status[i]){
					System.out.println("statusCopy[" + i + "] = " + statusCopy[i]);
					System.out.println("this.status[" + i + "] = " + this.status[i]);
				}
			}
			System.out.println("given status = " + Arrays.toString(Arrays.copyOfRange(status, 0, this.status.length)));
			System.out.println("this status = " + Arrays.toString(this.status));
			System.out.println("status");
			return false;
		} else {
			return true;
		}
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + Arrays.hashCode(status);
		result = prime * result + ((subSystem == null) ? 0 : subSystem.hashCode());
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
		DataPacket other = (DataPacket) obj;
		if (id != other.id)
			return false;
		if (origin != other.origin)
			return false;
		if (!Arrays.equals(status, other.status))
			return false;
		if (subSystem != other.subSystem)
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataPacket [origin=" + origin + ", id=" + id + ", subSystem=" + subSystem + ", status="
				+ Arrays.toString(status) + "]";
	}
	
	// TODO: remove this
	public static void main(String args[]){
		DataPacket p1 = new DataPacket(OriginType.FLOOR, (byte) 0, SubsystemType.FLOORLAMP, new byte[]{0x00, 0x01});
		DataPacket p2 = new DataPacket(p1);
		
		System.out.println("p1 = " + p1.toString() + System.identityHashCode(p1));
		System.out.println("p2 = " + p2.toString() + System.identityHashCode(p2));
	}
}	
