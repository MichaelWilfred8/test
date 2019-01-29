
public class FloorButton extends Button {
	
	private FloorButtonDirection direction;//direction of the button
	
	FloorButton(FloorButtonDirection direction){
		state = false;//instantiate button to false
		this.direction = direction;//instantiate direction as input direction
	}
	
	/**
	 * 
	 * @return direction of the button
	 */
	public FloorButtonDirection getDirection() {//getter for direction
		return direction;
	}
	
}
