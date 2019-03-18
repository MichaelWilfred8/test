package testing;

import java.util.concurrent.LinkedBlockingQueue;

import scheduler.Scheduler;
import scheduler.SchedulerHandler;
import shared.DataPacket;
import shared.GenericThreadedListener;
import shared.GenericThreadedSender;
import shared.SocketPort;

public class NewSchedulerTest {
	
	public static void main(String[] args) {
		LinkedBlockingQueue<DataPacket> inBuf = new LinkedBlockingQueue<DataPacket>();
		LinkedBlockingQueue<DataPacket> outBuf = new LinkedBlockingQueue<DataPacket>();
		
		final int NUM_ELEVATORS = 1;
		final int NUM_FLOORS = 12;
		
		SchedulerTest test = new SchedulerTest(inBuf, outBuf);
		Thread testThread = new Thread(test);
		
		Scheduler scheduler = new Scheduler(inBuf, outBuf, NUM_ELEVATORS, NUM_FLOORS);
		Thread schThread = new Thread(scheduler);
		
		GenericThreadedListener listener = new GenericThreadedListener(inBuf, SocketPort.SCHEDULER_LISTENER.getValue(), false);
		Thread listenerThread = new Thread(listener);
		
		GenericThreadedSender sender = new GenericThreadedSender(outBuf, SchedulerHandler.ELEVATOR_PORT_NUMBER, SchedulerHandler.SCHEDULER_PORT_NUMBER, SchedulerHandler.FLOOR_PORT_NUMBER, false);
		Thread senderThread = new Thread(sender);
		
		schThread.start();
		listenerThread.start();
		senderThread.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//testThread.start();
	}
}
