package floor;

import Enums.Direction;
import shared.Button;

public class FloorButton extends shared.Button {

	private Direction direction;//direction of the button


	public FloorButton(Direction direction) {
		state = false;//instantiate button to false
		this.direction = direction;//instantiate direction as input direction
	}

	/**
	 * @return direction of the button
	 */
	public Direction getDirection() {//getter for direction
		return direction;
	}

	/**
	 * @return state of buttons
	 */
	public boolean getState() {
		return state;
	}

	/**
	 * @return String representation of status
	 */
	public String getStateString() {
		if (state) {
			return "PRESSED";
		}else {
			return "UNPRESSED";
		}
	}

}
