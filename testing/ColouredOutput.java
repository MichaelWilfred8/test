package testing;

public class ColouredOutput {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
	public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
	public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
	public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
	public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
	public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
	public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
	public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
	
	
	public static void printColouredText(String text, String colour){
		System.out.println(colour + text);
	}
	
	public static void main(String[] args){
		printColouredText("This is ANSI_BLACK", ANSI_BLACK);
		printColouredText("This is ANSI_RED", ANSI_RED);
		printColouredText("This is ANSI_GREEN", ANSI_GREEN);
		printColouredText("This is ANSI_YELLOW", ANSI_YELLOW);
		printColouredText("This is ANSI_BLUE", ANSI_BLUE);
		printColouredText("This is ANSI_PURPLE", ANSI_PURPLE);
		printColouredText("This is ANSI_CYAN", ANSI_CYAN);
		printColouredText("This is ANSI_WHITE", ANSI_WHITE);
		
		
	}
}
