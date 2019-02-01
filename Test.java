import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Test{
	Scheduler scheduler;//Scheduler of 

	Test(){
		scheduler = new Scheduler();//instantiate Scheduler
		
	}

	public void runTest() {
		String fileToParse = "test.csv"; //Input file which needs to be parsed, change * to the path of the csv file
		String [][] testLines = getFile(fileToParse); //test strings from .csv
		Floor [] floors = scheduler.floorHandler.getFloors();
		for (int i=0; i<testLines.length;i++) {
			for (int j=0;j<floors.length;j++) {
				//System.out.println("LALALA " + Arrays.toString(testLines[i]));
				if(Integer.parseInt(testLines[i][1])==floors[j].getFloorNumber()) {
					floors[j].newRequest(testLines[i]);
				}
			}

		}
		
		for (int i=0;i<floors.length;i++) {
			floors[i].purgeRequests();
		}
	}

	public String[][] getFile(String fileName) {//returns an array of strings containing the lines of the .csv

		ArrayList<String[]> inputLines = new ArrayList<>(11);//arrayList of String arrays, each string array is a line from the input file

		BufferedReader fileReader = null;//instantiate file reader	
		final String DELIMITER = " ";//Delimiter used in CSV file
		try{
			String line = "";//build string into line

			fileReader = new BufferedReader(new FileReader(fileName));//Create the file reader

			while ((line = fileReader.readLine()) != null){//Read the file line by line
				//Get all tokens available in line
				String[] tokens = line.split(DELIMITER);//create an array of strings, represents the line of file
				inputLines.add(tokens);//add to the list of lines
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				fileReader.close();//close BufferedReader
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String[][] returnString = new String[inputLines.size()][];//2D array of strings, first dimension is number of lines within the input file, second is details of the line
		for (int i = 0; i < returnString.length; i++) {//copy the lengths of each internal array to the 1st dimension
			returnString[i] = new String[inputLines.get(i).length];
		}
		for(int i=0; i<inputLines.size(); i++){
			for (int j = 0; j < inputLines.get(i).length; j++) {//copy contents of ArrayList to array
				returnString[i][j] = inputLines.get(i)[j];
			}
		}

		return returnString;

	}


	public static void main(String[] args) {
		Test t = new Test();
		t.runTest();

	}
}