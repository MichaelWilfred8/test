// Button class used for the buttons in the elevator and on the floors

package shared;

public class Button {

	protected boolean state;	// state of the button. If the button is depressed or has been depressed, state is true. 

	// Constructor for base button class
	public Button(){
		this.state = false;
	}

	// Constructor for base button class that includes setting state
	public Button(boolean state){
		this.state = state;
	}
	
	/**
	 * toggle a button
	 */
	public void toggle() {
		state = !state;
	}
}
