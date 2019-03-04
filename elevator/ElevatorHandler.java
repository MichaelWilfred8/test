package elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import shared.*;

public class ElevatorHandler implements Runnable{


	DatagramPacket receivePacket;
	DatagramSocket receiveSocket;

	private static List<Elevator> elevatorList = new ArrayList<Elevator>(3);
    private boolean stopController;

	 private static final ElevatorHandler instance = new ElevatorHandler();
	    private ElevatorHandler(){
	        if(instance != null){
	            throw new IllegalStateException("Already instantiated");
	        }
	        setStopController(false);
	        initializeElevators();
	    

		// Construct a datagram socket and bind it to port 69 
		// on the local host machine. This socket will be used to
		// receive UDP Datagram packets. 
		try {
			receiveSocket = new DatagramSocket(SocketPort.ELEVATOR_LISTENER.getValue());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// to test socket timeout (2 seconds)
		//receiveSocket.setSoTimeout(2000);
	}

	// static method to create instance of class 
	public static ElevatorHandler getHandler(){ 
		return instance; 
	}

	/**
	 * listen for incoming requests, listens on port 69
	 */
	private void listen(){
//v
		
		boolean notDone = true;
		while(notDone) {
			// Construct a DatagramPacket for receiving packets up 
			// to 100 bytes long (the length of the byte array).
			byte data[] = new byte[5];
			receivePacket = new DatagramPacket(data, data.length);

			try {
				// Block until a datagram is received via sendReceiveSocket.
				System.out.println("ElevatorHandler: Waiting for Packet.\n");
				receiveSocket.receive(receivePacket);
			} catch(IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n");
				e.printStackTrace();
				System.exit(1);
			}

			// Process the received datagram.
			System.out.println("ElevatorHandler: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			int len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			// Form a String from the byte array.
			System.out.println("(Bytes) " + Arrays.toString(data));

			if(data[0]==-1) {
				notDone = false;
			} else {
				 Elevator elevator = null;
				DataPacket dp = new DataPacket(data);
				int id = dp.getId();
				  elevator = findElevator(id);
				  System.out.println("calling elevator " + id + "\n");
				  try {
					elevator.receiveAndEcho(dp, receivePacket);
				} catch (ClassNotFoundException | IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

	}
    public synchronized Elevator selectElevator(int id) {
        Elevator elevator = null;
        
        elevator = findElevator(id);
        // So that elevators can start moving again.
        notifyAll();
        return elevator;
    }
	
    private static void initializeElevators(){
        for(int i=0; i<3; i++){
            Elevator elevator = new Elevator(i);
            Thread t = new Thread(elevator);
            t.start();

            elevatorList.add(elevator);
        }
    }

	/**
	 * Thread run class
	 */
	@Override
	public void run() {
		listen();

	}
	
	  /**
     * Right now, the findElevator is only based on id
     * TODO in the future, findElevator will take (movingDirection, DestinationFloor, RequestFloor) as param
     * 		so we can use hash map
     * @param id
     * @return selected elevator
     */
    private static Elevator findElevator(int id) {
        Elevator elevator = null;
       elevator =  elevatorList.get(id);
        return elevator;
    }
	
	   public void setStopController(boolean stop){
	        this.stopController = stop;

	    }

	public static void main(String args[]){
		instance.run();
	}

}
