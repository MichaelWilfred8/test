package testing;

import java.util.concurrent.LinkedBlockingQueue;

import Enums.MotorState;
import Enums.OriginType;
import Enums.SubsystemType;
import scheduler.Scheduler;
import shared.DataPacket;

public class ElevatorStatusTesting {

	public static void main(String[] args) throws InterruptedException {
		//ElevatorStatus car = new ElevatorStatus(0, MotorState.OFF, DoorState.CLOSED, 7, 1);

		LinkedBlockingQueue<DataPacket> input = new LinkedBlockingQueue<DataPacket>();
		LinkedBlockingQueue<DataPacket> output = new LinkedBlockingQueue<DataPacket>();
		
		Scheduler scheduler = new Scheduler(input, output, 1, 7);
		Thread schThread = new Thread(scheduler);
		
		byte[] tempReq = {0,0,0,12, 0,0,0,15, 0,0,0,13, 0,0,0,111, 2, -1};
		// byte[] tempReq = {0,0,0,12, 0,0,0,15, 0,0,0,13, 0,0,0,111, enum reqType = floor, (dir)2};
		// byte[] tempReq = {0,0,0,12, 0,0,0,15, 0,0,0,13, 0,0,0,111, enum reqType = elev, floor 3, floor 4, floor 5, };
		
		final int dirIndex = 16;
		final int floorIndex = 17;
		
		schThread.start();
		
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		
		// Tell elevator that motor is stopped
		try {
			input.add(new DataPacket(OriginType.ELEVATOR, (byte) 0, SubsystemType.MOTOR, new byte[] {MotorState.OFF.getByte()}));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		System.out.println("output = " + output.toString() + "\n");
		
		
		// Add floor 4 to visit
		tempReq[floorIndex] = 4;
		try {
			input.add(new DataPacket(OriginType.FLOOR, (byte) 1, SubsystemType.REQUEST, tempReq));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		System.out.println("output = " + output.toString() + "\n");
		
		
		// Add floor 5 to visit
		tempReq[floorIndex] = 5;
		try {
			input.add(new DataPacket(OriginType.FLOOR, (byte) 1, SubsystemType.REQUEST, tempReq));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		System.out.println("output = " + output.toString() + "\n");
		
		
		// Add floor 3 to visit
		tempReq[floorIndex] = 3;
		try {
			input.add(new DataPacket(OriginType.FLOOR, (byte) 1, SubsystemType.REQUEST, tempReq));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		System.out.println("output = " + output.toString() + "\n");
		
		
		// Add floor 7 to visit
		tempReq[floorIndex] = 7;
		try {
			input.add(new DataPacket(OriginType.FLOOR, (byte) 1, SubsystemType.REQUEST, tempReq));
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		Thread.sleep(100);
		System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
		
		System.out.println("output = " + output.toString() + "\n");

		
		DataPacket tempPacket, locationPacket;
		
		locationPacket = new DataPacket(OriginType.ELEVATOR, (byte) 0, SubsystemType.LOCATION, new byte[] {(byte) 0});
		boolean locationFlag = false;
		
		System.out.println("Starting loop");
		System.out.println("===================================================================================\n\n\n");
		
		for(int i = 0; i < 30; ++i) {
			
			System.out.println("\n\n\ninside 1st loop");
			
			tempPacket = new DataPacket(output.take().getBytes());
			
			System.out.println("removed packet = " + tempPacket.toString());
			
			// change tempPacket to elevator
			tempPacket.setOrigin(OriginType.ELEVATOR);
			
			// print tempPacket
			System.out.println("adding packet = " + tempPacket.toString());
			
			// change location if necessary
			if ((tempPacket.getSubSystem() == SubsystemType.MOTOR) && (tempPacket.getStatus()[0] != MotorState.OFF.getByte())){
				locationFlag = true;
			}
			
			// send echo back to scheduler
			input.add(new DataPacket(tempPacket.getBytes()));
			
			// print input
			System.out.println("input = " + input.toString());
			
			Thread.sleep(250);
			System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
			
			System.out.println("output = " + output.toString() + "\n");
			
			// If location needs to be sent...
			if (locationFlag == true){
				locationFlag = false;
				
				for(int j = scheduler.getCar(0).getPosition(); j <= scheduler.getCar(0).MAX_FLOOR; ++j) {
					System.out.println("\n\n\ninside 2nd loop");
					// create location packet
					if(tempPacket.getStatus()[0] == MotorState.UP.getByte()){
						// if motor is going up then set locationPacket to be one higher then current
						locationPacket.setStatus(new byte[] {(byte) (scheduler.car[0].getPosition() + 1)});
					} else if (tempPacket.getStatus()[0] == MotorState.DOWN.getByte()) {
						// if motor is going down then set locationPacket to be one lower then current
						locationPacket.setStatus(new byte[] {(byte) (scheduler.car[0].getPosition() - 1)});
					}
					
					// sending location packet
					System.out.println("adding locationPacket = " + locationPacket.toString() + "\n");
					input.add(new DataPacket(locationPacket.getBytes()));
					
					// print input
					System.out.println("input = " + input.toString());
					
					Thread.sleep(250);
					System.out.println("car = " + scheduler.getCar(0).toString() + "\n");
					
					System.out.println("output = " + output.toString() + "\n");
					
					if (output.isEmpty() != true){
						break;
					}
				}
			}
		}
		System.out.println("loop ended");
		
	}

}
