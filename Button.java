// Button class used for the buttons in the elevator and on the floors

public class Button {
	
	boolean state;	// state of the button. If the button is depressed or has been depressed, state is true. 
	
	// Constructor for base button class
	public Button(){
		this.state = false;
	}
	
	// Constructor for base button class that includes setting state
	public Button(boolean state){
		this.state = state;
	}
}
