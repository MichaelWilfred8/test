import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;


public class Test{
	

	public void runTest() {
		String fileToParse = "/test.csv"; //Input file which needs to be parsed, change * to the path of the csv file
		String [] tests = getFile(fileToParse); //test strings from .csv
		System.out.println(Arrays.toString(tests));
	}

	public String[] getFile(String fileName) {//returns an array of strings containing the lines of the .csv
		
		ArrayList<String> testLines = new ArrayList<>(11);

		BufferedReader fileReader = null;		
		final String DELIMITER = ",";//Delimiter used in CSV file
		try{
			String line = "";

			fileReader = new BufferedReader(new FileReader(fileName));//Create the file reader

			int counter=0;

			String passenger="";


			while ((line = fileReader.readLine()) != null){//Read the file line by line

				//Get all tokens available in line
				String[] tokens = line.split(DELIMITER);

				//Print all tokens
				for(String t : tokens){
					if(counter==4){
						Test test = new Test();
						counter=0;
						testLines.add(passenger);
						passenger="";
					}
					passenger+=t;

					counter++;
				}
				Test test = new Test();
				counter=0;
				testLines.add(passenger);
				passenger="";

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String[] returnString = testLines.toArray(new String[testLines.size()]);
		return returnString;

	}

	public void sendReceive(String passenger) throws IOException{

		byte[] pass = passenger.getBytes();

		DatagramSocket toserver;
		toserver = new DatagramSocket();
		InetAddress intserver = InetAddress.getByName("localhost");
		int intserverSocket = 33;

		//Creates a Datagram packet sending the request in bytes, with the length of the request array(beginning), to address localhost, and using port 69
		DatagramPacket send = new DatagramPacket(pass,pass.length,intserver,intserverSocket);
		toserver.send(send);
		System.out.println("Sending the following to the Server: " +new String(send.getData()));
		toserver.close();

	}
	
	public static void main(String[] args) {

	}
}

