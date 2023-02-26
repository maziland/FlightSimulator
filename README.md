# JavaFX FlightGear Simulator Connector
This project is a JavaFX application that allows users to send data to FlightGear, an open-source flight simulator. The application takes input from a user-specified input file and sends that data to the simulator via a network connection.

## Requirements
* Java 8 or higher
* FlightGear flight simulator (version 2020.3.11 or higher)
## Installation
1. Clone the repository to your local machine.
2. Ensure that you have Java 8 or higher installed on your machine.
3. Download and install FlightGear flight simulator from the official website.
4. Navigate to the project directory on your machine.
5. Build the project using the following command:
`mvn package`
6. Launch the application using the following command:
`java -jar target/JavaFX-FlightGear-Simulator-Connector-1.0.jar`
## Usage
1. Launch FlightGear and start a new flight session.
2. Launch the JavaFX application.
3. Select the input file that contains the data you want to send to FlightGear.
4. Enter the network address and port number that FlightGear is listening on.
5. Click the "Connect" button to establish a connection to FlightGear.
6. Click the "Send Data" button to send the data from the input file to FlightGear.
