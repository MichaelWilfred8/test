import java.nio.ByteBuffer;

/*
 * Timestamp class for the elevator input packet.
 * 
 */
public class TimeStamp {
	private int hours;
	private int minutes;
	private int seconds;
	private int milliseconds;
	
	public TimeStamp(int hours, int minutes, int seconds, int milliseconds){
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		this.milliseconds = milliseconds;
	}
	
	
	// Constructor for TimeStamp Class. Takes String in HH:mm:ss:SSS format and parses it to convert it into a timestamp
	public TimeStamp(String timeStamp){
		this.hours = Integer.parseInt(timeStamp.substring(0, 2));
		this.minutes = Integer.parseInt(timeStamp.substring(3, 5));
		this.seconds = Integer.parseInt(timeStamp.substring(6, 8));
		this.milliseconds = Integer.parseInt(timeStamp.substring(9, 12));
	}
	
	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public int getMilliseconds() {
		return milliseconds;
	}

	public String toString(){
		return String.format("%02d:%02d:%02d:%03d", this.hours, this.minutes, this.seconds, this.milliseconds);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hours;
		result = prime * result + milliseconds;
		result = prime * result + minutes;
		result = prime * result + seconds;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeStamp other = (TimeStamp) obj;
		if (hours != other.hours)
			return false;
		if (milliseconds != other.milliseconds)
			return false;
		if (minutes != other.minutes)
			return false;
		if (seconds != other.seconds)
			return false;
		return true;
	}
	
	/**
	 * Converts object into 2D byte array
	 * @return 2D byte array (4X4) containing TimeStamp parameters as bytes
	 */
	public byte[] toBytes(){
		byte [] hourBytes = ByteBuffer.allocate(4).putInt(hours).array();
		byte [] minBytes = ByteBuffer.allocate(4).putInt(minutes).array();
		byte [] secBytes = ByteBuffer.allocate(4).putInt(seconds).array();
		byte [] milliBytes = ByteBuffer.allocate(4).putInt(milliseconds).array();
		
		byte [] timestampBytes = {hourBytes[3],minBytes[3],secBytes[3],milliBytes[3]};
	    return timestampBytes;
	}
	
	/**
	 * 
	 * @param timeBytes array of array of time bytes, 4 arrays one for each time parameter
	 * @return timestamp containing array numbers
	 */
	public static TimeStamp bytesToTimeStamp(byte[] timeBytes) {
		int newHours = timeBytes[0];
		int newMin = timeBytes[1];
		int newSec = timeBytes[2];
		int newMilli = timeBytes[3];
		TimeStamp returnStamp = new TimeStamp(newHours, newMin, newSec, newMilli);
		return returnStamp;
	}
	
	// TODO: add isBefore and isAfter methods?
	
	public static void main(String[] args){
		TimeStamp ts = new TimeStamp("03:45:01:123");
		
		System.out.println("Timestamp = " + ts.toString());
	}
}
