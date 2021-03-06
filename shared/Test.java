package shared;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import Enums.OriginType;
import Enums.SubsystemType;
import floor.*;

// TODO: Get test to send requests when time is specified, assuming first one is moment test is started
public class Test{
	
	FloorHandler handler;//Scheduler of
	Test(){
		
		handler = FloorHandler.getHandler();

	}
	
	public void runTest() {

		String fileToParse = "schedulerTest.csv"; //Input file which needs to be parsed, change * to the path of the csv file
		String [][] testLines = getFile(fileToParse); //test strings from .csv
		
		Thread fhThread = new Thread(handler);
		fhThread.start();
		
		
		try {
			organizer(testLines, handler);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public String[][] getFile(String fileName) {//returns an array of strings containing the lines of the .csv
		//Test should send the error packet to the elevator by reading the 3rd column and seeing if it's an error
		ArrayList<String[]> inputLines = new ArrayList<>(11);//arrayList of String arrays, each string array is a line from the input file

		BufferedReader fileReader = null;//instantiate file reader
		final String DELIMITER = ",";//Delimiter used in CSV file
		try{
			String line = "";//build string into line

			fileReader = new BufferedReader(new FileReader(fileName));//Create the file reader

			while ((line = fileReader.readLine()) != null){//Read the file line by line
				//Get all tokens available in line
				String[] tokens = line.split(DELIMITER);//create an array of strings, represents the line of file
				
				inputLines.add(tokens);
				//add to the list of lines
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

	public void sendReceive(String passenger) throws IOException{

		byte[] pass = passenger.getBytes();

		DatagramSocket toserver;
		toserver = new DatagramSocket();
		InetAddress intserver = InetAddress.getByName("localhost");
		int intserverSocket = 32;

		// Creates a Datagram packet sending the request in bytes, with the length of the request array(beginning), to address localhost, and using port 69
		DatagramPacket send = new DatagramPacket(pass,pass.length,intserver,intserverSocket);
		toserver.send(send);
		System.out.println("Sending the following to the Server: " + new String(send.getData()));
		toserver.close();

	}
	
	public static void errorSender(String x []) throws IOException
	{
		DatagramSocket sender = new DatagramSocket();
		InetAddress elev = InetAddress.getLocalHost();
		SocketAddress elevatorport = new InetSocketAddress(68);
		
		//Gets what subsytem has the error from the 4th index of the array
		SubsystemType type = SubsystemType.ERROR.toSubsystem(Integer.parseInt(x[3]));
		
		//Sets which elevator has the error from the 2nd index of the array
		int elevator = Integer.parseInt(x[1]);
		byte id = (byte) elevator;
		
		//Sets the status to the timestamp of the error from the 1st index of the array
		byte[] status=x[0].getBytes(); 
		
		//Creates the packet and sends it to the elevator handler
		DataPacket request = new DataPacket(OriginType.ERROR,id,type,status);
		byte[] errorbyte = request.getBytes();
		DatagramPacket packet = new DatagramPacket(errorbyte,errorbyte.length,elev,68);
		sender.send(packet);
		//error.addError(request);
		System.out.println("Error found "+type+ " Packet "+request);
		//Send to elevator to process
	}

	public static void organizer(String x [][], FloorHandler handler) throws InterruptedException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
		System.out.println(x[0][0]);

		Date date = null;	//Variables used to compare timestamps
		Date date1 = null;

		for(int i=0;i<x.length-2;i++) {
			try {
				date = dateFormat.parse(x[i][0]);
				date1 = dateFormat.parse(x[i+1][0]);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			long formattedDate = date1.getTime()-date.getTime(); //calculates the time difference between the current and the next
			if(x[i][2].toString().equals("ERROR"))
			{
				System.out.println("Sending Error");
				try {
					errorSender(x[i]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
			handler.createRequest(x[i]);
			}
			//System.out.println("WAITING");
			TimeUnit.MILLISECONDS.sleep(formattedDate); //sleeps for the time difference
			//System.out.println("DONE WAITING\n");

		}
	}


	public static void main(String[] args) {
		Test t = new Test();
		t.runTest();
	}
}
