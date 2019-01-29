import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

 
public class Test
{
	public static void main(String[] args)
    {
        //Input file which needs to be parsed
        String fileToParse = "/Users/michaelwilfred/Desktop/test.csv";
        BufferedReader fileReader = null;
         
        //Delimiter used in CSV file
        final String DELIMITER = ",";
        try
        {
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileToParse));
            
             int a=0;
             
             String passenger="";
            //Read the file line by line
            
            while ((line = fileReader.readLine()) != null)
            {
            	
                //Get all tokens available in line
                String[] tokens = line.split(DELIMITER);
                
               
                    //Print all tokens
                for(String t : tokens) {
                	if(a==4)
                	{
                		Test test = new Test();
                		a=0;
                		test.sendReceive(passenger);
                		passenger="";
                	}
                	passenger+=t;
                	System.out.println(a+":"+passenger+" "+t.length());
                	a++;
                }
                Test test = new Test();
        		a=0;
        		test.sendReceive(passenger);
        		passenger="";
                
            }
           
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        finally
        {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	public void sendReceive(String passenger) throws IOException
	{
		System.out.println(passenger);
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
}
 
