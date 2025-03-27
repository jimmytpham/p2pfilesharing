Assignment 2
Jimmy Pham
3711704
COMP 482
Athabascu University



P2P File Sharing Application 


Overview
This is a Peer-to-Peer (P2P) file-sharing app that uses CORBA for communication between the client and server. It connects to a MySQL database. The app allows users to share files with others by connecting to MySQL.


Files Included
The package includes the following files:
•	Java Code: The source code for the server and client, including file-sharing handling.
•	Database Export: A SQL dump file (p2pfilesharing.sql) to set up the MySQL database.
•	Dependencies: Required libraries are included.
•	Batch File: A batch file (run_program.bat) to easily run the entire application.


Prerequisites
Before you begin, make sure you have the following installed:
•	Java (JDK 8): Needed to compile and run the Java code.
•	MySQL Database: MySQL server running locally or remotely.
•	Maven: To manage dependencies and build the project.


Setting Up the Database
•	Install MySQL: Make sure to have MySQL installed.
•	Create a Database: Log in to your MySQL instance and create the required database:
CREATE DATABASE p2pfilesharing;
•	Run the Database Script:
•	Inside the downloaded package, you’ll find a file called p2pfilesharing.sql. Run this script to create the necessary tables and schema (e.g., users, files).
•	Replace username with your MySQL username
•	Update Database Connection Information:
•	In the Server.java and ClientGUI.java files, ensure the database connection information matches your setup.
•	Update the following with your MySQL server details:

-	String url = "jdbc:mysql://localhost:3306/p2pfilesharing";  // URL of your MySQL database
-	String username = "your_username";                          // Your MySQL username
-	String password = "your_password";                          // Your MySQL password


Building and Running the Application
1. Building the Project with Maven
To build the project and download dependencies, run the command from the project directory:
  •mvn clean install
This will compile the project and get all required dependencies.

2. Running the Application with the Batch File
Instead of running everything  manually, you can use the provided run_program.bat file to start the app.
To use the batch file:
1.	Navigate to the folder where the run_program.bat file is located.
2.	Double-click the run_program.bat file to start the CORBA Naming Service, server, and client automatically.
The batch file will:
•	Start the CORBA Naming Service.
•	Start the Server.
•	Start the Client GUI.


How to Use the Application
Client Interaction:
•	Login: When you start the client, you can enter your username. You can create a new user by entering a username that is not registered yet.
•	Upload Files: You can upload/remove files to the P2P network after logging in. You must include the file type in the text field (ex. .txt, .pdf, etc.)
•	Download Files: Search for files uploaded by other peers and download them.

Server Functionality:
•	The server handles multiple client connections and manages file uploads.
•	It stores file information in the MySQL database. The client communicates with the server to retrieve file metadata and access shared files.


File Structure
The project is organized as follows:
P2PFileSharing/
│├ -.idea
│├ -lib
|├ out
|├ shared_files  	# holds the downloaded files
│├── src/                      
|  ├ main
|	├idl
|	   ├ FileSharing
|	   ├ filesharing.idl
|	├ java
│                 ├── Server.java             # Main server code
│   	    ├── ClientGUI.java             # Main client code
│   	   ├── FileSharingHandler.java      # Corba file sharing service
|  ├ test│  
│
├── database.sql                # SQL script for setting up the database
├── target/                     # Maven build output directory
├── pom.xml                     # Maven project configuration file
├── Run_program.bat               # Batch file to run the application
└── README.txt                  # Project documentation (this file)


Troubleshooting
•	Database Connection Errors: If you get connection errors, check that the database credentials in the Java files are correct and that your MySQL server is running.
•	ClassNotFoundException: Ensure that the MySQL connector is listed in the pom.xml file and has been properly downloaded by Maven.