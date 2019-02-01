import Enums.*;
import java.net.*;


/**
 * Class to hold the addresses of each part of the subsystem
 * 
 * @author Craig Worthington
 *
 */
public class SubsystemAddress {
	private OriginType type;
	private int id;
	private InetAddress inetAddress;
	private int port;


	public SubsystemAddress(OriginType type, int id, InetAddress address, int port) {
		super();
		this.type = type;
		this.id = id;
		this.inetAddress = address;
		this.port = port;
	}


	public SubsystemAddress(OriginType type, int id, InetSocketAddress address) {
		super();
		this.type = type;
		this.id = id;
		this.inetAddress = address.getAddress();
		this.port = address.getPort();
	}


	/**
	 * @return the type
	 */
	public OriginType getType() {
		return type;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @return the inetAddress
	 */
	public InetAddress getInetAddress() {
		return inetAddress;
	}


	public SocketAddress getSocketAddress(){
		return new InetSocketAddress(this.inetAddress, this.port);
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}



}
