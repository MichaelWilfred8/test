package elevator;

import Enums.MotorState;

public class FloorChangerThread implements Runnable {
	
	Elevator car;
	
	private static final int timeBetweenFloors = 3000;
	
	public FloorChangerThread(Elevator car) {
		this.car = car;
	}
	
	@Override
	public void run() {
		
		if (car.motorState == MotorState.UP){
			// Increment floors until car has reached max floor or thread is interrupted
			while((car.getCurrentFloor() <= car.getMAX_FLOOR()) && (Thread.interrupted() == false)) {
				try {
					Thread.sleep(timeBetweenFloors);
				} catch (InterruptedException e) {
					System.out.println("Thread interrupted");
					Thread.currentThread().interrupt();
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
				}
				
				car.setCurrentFloor(car.getCurrentFloor() - 1);											// Decrement floor of the elevator
				System.out.println("Elevator " + car.getId() + " : I am at  "+ car.getCurrentFloor());	// Print location
				car.sendLocation();																		// Send a packet back to Scheduler with location
			}
		}
	}

}
