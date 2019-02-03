


How to run the project:
To run the project, please run the classes in the following order:
FloorHandler.java
ElevatorHandler.java
Scheduler.java

This will demonstrate how the scheduler is able to move the elevator up one floor. Currently, the scheduler is not complete so it cannot take inputs from the csv file and use them to schedule the elevator. The Test.java file will accept inputs from the csv and send them to the floorhandler but scheduler cannot currently handle those inputs. 

Note: if there is an error saying that the java class version is too new, then make sure to compile the code before running it. 


=============================================================================

Files in Project:

Button.java: A button class used to represent a button and its state. 

DataPacket.java: A DataPacket format used to communicate between each subclass. Holds the origin subsystem, its ID, the element within the subsystem to be addressed and the staus of that element. Also has methods to convert the information to and from bytes.

Elevator.java: The elevator class. Used to contol each element in the elevator 

ElevatorHandler.java: The handler class for the elevator subsystem. Creates each instance of the elevator and handles incoming requests.


ElevatorInputPacket.java: A class that holds the format for the elevator input as it comes from the CSV file. Holds the timestamp, floor where request originated, the state of the floor buttons, and the button that was pressed within the car.


ElevatorStatus.java: A class that holds information about each elevator for the scheduler. It holds the state of each elevator subsystem as well as a set of floors for the elevator to visit. 

Floor.java: The floor subsystem. Handles incoming requests and communicates them with the scheduler. 

FloorButton.java: An extension of the button class that is used to represent the ip and down arrows outside of an elevator. Also has a field for the direction of the floor button

FloorHandler.java: The handler class for the floor subsystem. Creates each instance of floor and handles incoming requests

FloorLamp.java: A class that represents the direction lamps on the floor. Holds their direction and state.

Scheduler.java: The scheduler class. Gets requests from the floor and elevator subsystem and controls the elevators. 

Test.java: Used to read the csv input file and send requests to the floor

TimeStamp.java: Class that represents a timestamp with Hours, minutes, seconds and milliseconds fields

Direction.java: an Enumerated type that represents the direction that the elevator may travel (i.e. up and down). Also holds some methods for converting to and from byte values

DoorState.java: an Enumerated type that represents the state of the elevator doors (i.e. open and closed). Also holds some methods for converting to and from byte values

FloorLampState.java: an Enumerated type that represents the state of the direction lamps on the floors (i.e. up and down). Also holds some methods for converting to and from byte values

MotorState.java: an Enumerated type that represents the state of the elevator motor (i.e. up, down and off). Also holds some methods for converting to and from byte values

OriginType.java: an Enumerated type used in the DataPacket class that represents the subsystem from which the message originated. Is also used in Scheduler to identify where to send the message. Also holds some methods for converting to and from byte values

SubsystemType.java: an Enumerated type used in the DataPacket class that represents the element within the subsystem that the message is supposed to address (i.e. the motor element in elevator, or the FloorDirectionLamp on the floor). Also holds some methods for converting to and from byte values

====================================================================

Breakdown of responsibilities:

Hongbo Pang: Elevator.java

Rahul Patel: Button.java, Direction.java, ElevatorHandler.java, Floor.java, FloorButton.java, FloorHandler.java, FloorLamp.java, UML Class Diagram

Michael Wilfred: Test.java

Craig Worthington: DataPacket.java, DoorState.java, ElevatorInputPacket.java, ElevatorStatus.java, FloorLampState.java, MotorState.java, OriginType.java, Scheduler.java, SubsystemType.java, TimeStamp.java, README.txt

Haohan Zhang: