package elevator;

import java.util.concurrent.atomic.AtomicBoolean;

import Enums.MotorState;

/**
 * @author craig
 *
 */
public class FloorChangerThread implements Runnable {
	
	Elevator car;
	private Thread worker;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean stopped = new AtomicBoolean(true);
	private static final int TIME_BETWEEN_FLOORS = 3000;
	
	/**
	 * Constructor for FloorChangerThread
	 * @param car	Reference to the Elevator object that this thread will modify
	 */
	public FloorChangerThread(Elevator car) {
		this.car = car;
	}
	
	
	/**
	 * Start a new FloorChangerThread
	 */
	public void start(){
		worker = new Thread(this);
		worker.start();
	}
	
	
	/**
	 * Stop the thread from running
	 */
	public void stop(){
		running.set(false);
	}
	
	
	/**
	 * Interrupt the thread
	 */
	public void interrupt(){
		running.set(false);
		worker.interrupt();
	}
	
	
	/**
	 * @return True if the thread is running
	 */
	boolean isRunning() {
		return running.get();
	}
	
	
	/**
	 * @return True if the thread has been stopped
	 */
	boolean isStopped() {
		return stopped.get();
	}
	
	
	/**
	 * Main run function. Increases or decreases the floor of the elevator as necessary
	 */
	public void run() {
		running.set(true);
		stopped.set(false);
		
		while(running.get()) {
			try {
				Thread.sleep(TIME_BETWEEN_FLOORS);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.out.println("Thread was interrupted, no longer moving floors");
			}
			
			if (car.getMotorState() == MotorState.UP) {
				car.setCurrentFloor(car.getCurrentFloor() + 1);											// Increment floor of the elevator
				System.out.println("Elevator " + car.getId() + " : I am at  "+ car.getCurrentFloor());	// Print location
				car.sendLocation();																		// Send a packet back to Scheduler with location
			} else if (car.getMotorState() == MotorState.DOWN) {
				car.setCurrentFloor(car.getCurrentFloor() - 1);											// Decrement floor of the elevator
				System.out.println("Elevator " + car.getId() + " : I am at  "+ car.getCurrentFloor());	// Print location
				car.sendLocation();																		// Send a packet back to Scheduler with location
			}
		}
		
		stopped.set(true);
	}
	
	/*
	@Override
	public void run() {
		if (car.motorState == MotorState.UP){
			// Increment floors until car has reached max floor or thread is interrupted
			while((car.getCurrentFloor() <= car.getMAX_FLOOR()) && (Thread.interrupted() == false) && (this.exitFlag == false)) {
				try {
					Thread.sleep(timeBetweenFloors);
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted");
					Thread.currentThread().interrupt();
					break;
				}
				
				if (this.exitFlag == true) {
					break;
				}
				
				car.setCurrentFloor(car.getCurrentFloor() + 1);											// Increment floor of the elevator
				System.out.println("Elevator " + car.getId() + " : I am at  "+ car.getCurrentFloor());	// Print location
				car.sendLocation();																		// Send a packet back to Scheduler with location
			}
		} else if (car.motorState == MotorState.DOWN){
			// Increment floors until car has reached max floor or thread is interrupted
			while((car.getCurrentFloor() <= 0) && (Thread.interrupted() == false)) {
				try {
					Thread.sleep(timeBetweenFloors);
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted");
					Thread.currentThread().interrupt();
					break;
				}
				
				
				if (this.exitFlag == true) {
					break;
				}
				
				car.setCurrentFloor(car.getCurrentFloor() - 1);											// Decrement floor of the elevator
				System.out.println("Elevator " + car.getId() + " : I am at  "+ car.getCurrentFloor());	// Print location
				car.sendLocation();																		// Send a packet back to Scheduler with location
			}
		}
	}*/

}
