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

public class DataPacket {

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
			return false;
		} else if (id != this.id) {
			return false;
		} else if (!subSystem.equals(this.subSystem)) {
			return false;
		} else if (!status.equals(this.status)) {
			return false;
		} else {
			return true;
		}
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataPacket [origin=" + origin + ", id=" + id + ", subSystem=" + subSystem + ", status="
				+ Arrays.toString(status) + "]";
	}
	
	
}	
