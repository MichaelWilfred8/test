import Enums.Direction;

public class FloorButton extends Button {

	private Direction direction;//direction of the button


	public FloorButton(Direction down) {
		state = false;//instantiate button to false
		this.direction = direction;//instantiate direction as input direction
	}

	/**
	 * 
	 * @return direction of the button
	 */
	public Direction getDirection() {//getter for direction
		return direction;
	}

}
