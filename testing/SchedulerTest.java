package testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Enums.*;
import floor.*;
import shared.DataPacket;

public class SchedulerTest {
	FloorHandler handler;//Scheduler of
	
	LinkedBlockingQueue<DataPacket> input, output;
	

	SchedulerTest(){
		this.input = new LinkedBlockingQueue<DataPacket>();
		this.output = new LinkedBlockingQueue<DataPacket>();
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
				
				// If it is formatting an error
				if(tokens[2].toString().equals("ERROR"))
					{	
					//Changes here
						DatagramSocket sender = new DatagramSocket();
						InetAddress elev = InetAddress.getLocalHost();
						SocketAddress elevatorport = new InetSocketAddress(68);
						SubsystemType type = SubsystemType.ERROR.toSubsystem(Integer.parseInt(tokens[3]));
						int elevator = Integer.parseInt(tokens[1]);
						byte id = (byte) elevator;
						byte[] status=tokens[0].getBytes(); 
						DataPacket request = new DataPacket(OriginType.ERROR,id,type,status);
						byte[] errorbyte = request.getBytes();
						
						DatagramPacket packet = new DatagramPacket(errorbyte,errorbyte.length,elev,68);
						sender.send(packet);
						//error.addError(request);
						System.out.println("Error found "+type+ " Packet "+request);
						//Send to elevator to process
					}
				else {
				inputLines.add(tokens);
				}//add to the list of lines
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

		String[][] returnString = new String[inputLines.size()][];	//2D array of strings, first dimension is number of lines within the input file, second is details of the line
		for (int i = 0; i < returnString.length; i++) {				//copy the lengths of each internal array to the 1st dimension
			returnString[i] = new String[inputLines.get(i).length];
		}
		for(int i=0; i<inputLines.size(); i++){
			for (int j = 0; j < inputLines.get(i).length; j++) {	//copy contents of ArrayList to array
				returnString[i][j] = inputLines.get(i)[j];
			}
		}

		return returnString;

	}
	
	
	private void createRequest(String[] input) {
		System.out.println("Input = " + Arrays.toString(input));
		if (input[2].equalsIgnoreCase("ERROR")) {	// if an error is sent from the .csv file
			byte[] errorPacketContents = {(byte) Integer.parseInt(input[3]), 1}; // adds the byte of the integer representation of the Enum of the system that has failed
			DataPacket errorPacket = new DataPacket(OriginType.ELEVATOR, (byte) Integer.parseInt(input[1]),SubsystemType.ERROR, errorPacketContents); //forms error packet to be sent
			
			this.input.add(errorPacket); // places error message in queue to be sent to scheduler	
			
		} else {
			/*
			for (int i = 0; i < floors.length; i++) {
				if (floors[i].getFloorNumber() == Integer.parseInt(input[1])) {
					floors[i].newRequest(input);

				}
			}*/
		}
	}
	
	
	/**
	 * Create a request to be sent to the scheduler
	 * @param request String containing request information (time, floor, direction)
	 */
	/*public void newRequest(String[] request) {
		
		DataPacket message = requestElevator(request);
		DataPacket destination = destinationRequest(request);

		if(!floorButtons[message.getStatus()[16]-1].getState()) {//if the button indicating the direction the elevator travelling is not yet on
			floorButtons[message.getStatus()[16]-1].toggle();//switch it on
			System.out.println("Floor " + floorNumber + " is toggling it's " + floorButtons[message.getStatus()[16]-1].getDirection().toString() + " button on.");
			System.out.println("Floor "+ floorNumber +" lamp facing " + floorButtons[message.getStatus()[16]-1].getDirection().toString() + " is now " + floorButtons[message.getStatus()[16]-1].getStateString());	
		}

		if(!requested) {//if no request has been made for this floor
			sendRequest(message);//request an elevator
			if (requests[destination.getStatus()[17]]==-1){//if there is not already a destination request
				requests[destination.getStatus()[17]] = 1;//indicate that the floor @ requests[i] is a destination
				requestCount++;
			}
			requested = true;//a request now has been made
		}else {
			if (requests[destination.getStatus()[17]]==-1){//if there is not already a destination request
				requests[destination.getStatus()[17]] = 1;//indicate that the floor @ requests[i] is a destination
				requestCount++;
			}
		}
	}*/
	
	
	public void organizer(String x [][], FloorHandler handler) throws InterruptedException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
		System.out.println(x[0][0]);

		Date date = null;	//Variables used to compare timestamps
		Date date1 = null;

		for(int i=0;i<x.length-1;i++) {
			try {
				date = dateFormat.parse(x[i][0]);
				date1 = dateFormat.parse(x[i+1][0]);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			long formattedDate = date1.getTime()-date.getTime(); //calculates the time difference between the current and the next

			//handler.createRequest(x[i]);
			this.createRequest(x[i]);
			
			//System.out.println("WAITING");
			TimeUnit.MILLISECONDS.sleep(formattedDate); //sleeps for the time difference
			//System.out.println("DONE WAITING\n");

		}
	}
	
	
	public void runTest() {

		String fileToParse = "test.csv"; //Input file which needs to be parsed, change * to the path of the csv file
		String [][] testLines = getFile(fileToParse); //test strings from .csv
		
		try {
			organizer(testLines, handler);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		handler.listen();
	}
	

	public static void main(String[] args) {
		SchedulerTest t = new SchedulerTest();
		t.runTest();
	}

}
