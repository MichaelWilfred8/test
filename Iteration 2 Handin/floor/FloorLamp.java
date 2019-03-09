package floor;

import Enums.Direction;

public class FloorLamp {

	private boolean state;
	private Direction direction;

	/**
	 * Generic constructor
	 */
	FloorLamp(Direction direction){
		state = false;
		this.direction=direction;
	}

	/**
	 * Constructor with initial state
	 * @param state initial state
	 */
	FloorLamp(boolean state, Direction direction){
		this.state = state;
		this.direction = direction;
	}

	/**
	 * Toggle state (switches from T->F, or does the opposite)
	 */
	public void toggle() {
		state = !state;
	}

	/**
	 * @return direction of lamp
	 */
	public Direction getDirection() {
		return direction;
	}
	
	/**
	 * @return if the lamp is on/off
	 */
	public boolean getState() {
		return state;
	}
	
	/**
	 * @return String representation of status
	 */
	public String getStateString() {
		if (state) {
			return "ON";
		}else {
			return "OFF";
		}
	}


}
