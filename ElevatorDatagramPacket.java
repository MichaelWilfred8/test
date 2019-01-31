import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;

import Enums.FloorButtonDirection;


/*
 * ElevatorDatagramPacket class. Extends ElevatorInputPacket class and is used to mimic 
 * a DatagramPacket class for sending and receiving packets.
 * 
 */


public class ElevatorDatagramPacket extends ElevatorInputPacket{
	
	private DatagramPacket packet;				// DatagramPacket for sending or receiving the ElevatorInputPacket information
	
	
	// Constructor for creating ElevatorDatagramPacket from a given DatagramPacket
	public ElevatorDatagramPacket(DatagramPacket p) {
		super(p.getData());
		this.packet = new DatagramPacket(p.getData(), p.getLength(), p.getSocketAddress());
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket to send from InetAddress and Port
	public ElevatorDatagramPacket(TimeStamp timeStamp, int floor, FloorButtonDirection floorButton, int carButton, InetAddress address, int port){
		super(timeStamp, floor, floorButton, carButton);
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH, address, port);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket from an existing ElevatorInputPacket to send to a given InetAddress and Port
	public ElevatorDatagramPacket(ElevatorInputPacket p, InetAddress address, int port){
		super(p.getBytes());
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH, address, port);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket from a byte array to send to a given InetAddress and Port
	public ElevatorDatagramPacket(byte[] buf, InetAddress address, int port){
		super(buf);
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH, address, port);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket to send to a given SocketAddress
	public ElevatorDatagramPacket(TimeStamp timeStamp, int floor, FloorButtonDirection floorButton, int carButton, SocketAddress address){
		super(timeStamp, floor, floorButton, carButton);
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH, address);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket from an existing ElevatorInputPacket and a given SocketAddress
	public ElevatorDatagramPacket(ElevatorInputPacket p, SocketAddress address){
		super(p.getBytes());
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH, address);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket from a byte array to send to a given SocketAddress
	public ElevatorDatagramPacket(byte[] buf, SocketAddress address){
		super(buf);
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH, address);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket to be received
	public ElevatorDatagramPacket(byte[] buf, int length){
		super(buf);
		this.packet = new DatagramPacket(super.getBytes(), length);
	}
	
	
	// Constructor for creating an ElevatorDatagramPacket to be received, but with length removed
	public ElevatorDatagramPacket(byte[] buf){
		super(buf);
		this.packet = new DatagramPacket(super.getBytes(), super.BYTE_ARRAY_LENGTH);
	}
	
	public DatagramPacket getPacket(){
		this.packet.setData(super.getBytes()); 	// ensure that all information in packet is up to date
		return this.packet;
	}
	
	/*
	 * Below are all the methods from the DatagramPacket class
	 */
	public InetAddress getAddress(){
		return this.packet.getAddress();
	}
	
	public byte[] getData(){
		return this.packet.getData();
	}
	
	public int getLength(){
		return this.packet.getLength();
	}
	
	// TODO: fix this function to return the actual offset? or remove entirely
	public int getOffset(){
		return 1;
	}
	
	public int getPort(){
		return this.packet.getPort();
	}
	
	public SocketAddress getSocketAddress(){
		return this.packet.getSocketAddress();
	}
	
	public void setAddress(InetAddress iaddr){
		this.packet.setAddress(iaddr);
	}
	
	public void setData(byte[] buf){
		this.packet.setData(buf);
	}
	
	public void setData(byte[] buf, int offset, int length){
		this.packet.setData(buf, offset, length);
	}
	
	public void setPort(int iport){
		this.packet.setPort(iport);
	}
	
	public void setSocketAddress(SocketAddress address){
		this.packet.setSocketAddress(address);
	}
}
