package testing;

import java.util.concurrent.LinkedBlockingQueue;

import scheduler.Scheduler;
import shared.DataPacket;

public class NewSchedulerTest {
	
	public static void main(String[] args) {
		LinkedBlockingQueue<DataPacket> inBuf = new LinkedBlockingQueue<DataPacket>();
		LinkedBlockingQueue<DataPacket> outBuf = new LinkedBlockingQueue<DataPacket>();
		
		final int NUM_ELEVATORS = 1;
		
		SchedulerTest test = new SchedulerTest(inBuf, outBuf);
		Thread testThread = new Thread(test);
		
		Scheduler scheduler = new Scheduler(inBuf, outBuf, NUM_ELEVATORS, 9);
		Thread schThread = new Thread(scheduler);
		
		ElevatorTestThread car = new ElevatorTestThread(inBuf, outBuf, 0, scheduler);
		Thread carThread = new Thread(car);
		
		schThread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		carThread.start();
		testThread.start();
	}
}
