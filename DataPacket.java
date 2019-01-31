import java.nio.ByteBuffer;

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
	 * Generate a byte array of all the information in the data packet
	 * @return byte array containing the information in the data packet
	 */
	public byte[] getBytes(){
		ByteBuffer buf = ByteBuffer.allocate(100);
		buf.put(this.origin.getByte());
		buf.put(this.id);
		buf.putShort(this.subSystem.getByte());
		buf.put(this.status);
		
		buf.flip();
		
		return buf.array();
	}
}	
