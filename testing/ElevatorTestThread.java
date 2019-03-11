package testing;

import java.util.concurrent.LinkedBlockingQueue;

import Enums.MotorState;
import Enums.OriginType;
import Enums.SubsystemType;
import scheduler.Scheduler;
import shared.DataPacket;

public class ElevatorTestThread implements Runnable {
	LinkedBlockingQueue<DataPacket> input, output;
	DataPacket tempPacket, locationPacket;
	int id;
	int position;
	boolean locationFlag;
	Scheduler scheduler;
	
	
	public ElevatorTestThread(LinkedBlockingQueue<DataPacket> inBuf, LinkedBlockingQueue<DataPacket> outBuf, int id, Scheduler scheduler){
		this.input = inBuf;
		this.output = outBuf;
		this.position = 1;
		this.id = id;
		this.locationFlag = false;
		this.scheduler = scheduler;
	}
	
	
	private void sendLocation() throws InterruptedException{
		locationFlag = false;
		
		for(int j = scheduler.getCar(this.id).getPosition(); j <= scheduler.getCar(this.id).MAX_FLOOR; ++j) {
			System.out.println("\n\n\ninside 2nd loop");
			// create location packet
			
			if (tempPacket.getStatus()[0] == MotorState.UP.getByte()){
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
	
	
	private void mainLoop() throws InterruptedException{
		
		//if (output.peek().getId() == (byte) this.id){
		if (true){
			tempPacket = new DataPacket(output.take().getBytes());
			
			//System.out.println("removed packet = " + tempPacket.toString());
			
			// change tempPacket to elevator
			if (tempPacket.getSubSystem() == SubsystemType.FLOORLAMP){
				tempPacket.setOrigin(OriginType.FLOOR);
			} else {
				tempPacket.setOrigin(OriginType.ELEVATOR);
			}
			
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
			
			if (this.locationFlag){
				this.sendLocation();
			}
			
		}
		Thread.sleep(150);
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.locationPacket = new DataPacket(OriginType.ELEVATOR, (byte) 0, SubsystemType.LOCATION, new byte[] {(byte) 0});
		
		while(true){
			try {
				mainLoop();
			} catch (InterruptedException e) {
				System.err.println("InterruptedException");
				e.printStackTrace();
			}
		}
	}

}
