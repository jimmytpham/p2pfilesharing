/* Jimmy Pham
 *  3711704
 * Assignment 2
 * COMP 489
 */

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;

import java.sql.Connection;
import java.sql.DriverManager;

/*
 * Program: Server
 * Description: This program initializes a CORBA server, connects to a MySQL database,
 *              and registers the file sharing handler with the Naming Service for P2P file sharing.
 * Expected Inputs: No user inputs. The program runs with default parameters passed to the ORB.
 * Expected Outputs/Results:
 *      - Initializes CORBA ORB.
 *      - Connects to the MySQL database 'p2pfilesharing'.
 *      - Registers the file sharing service in the Naming Service.
 *      - Prints "File Sharing main.java.com.example.FileSharingApp.Server is running..." when started.
 * Called by: This program is called directly from the main method.
 * Will Call:
 *      - POAHelper, POA, and CORBA components to manage CORBA references.
 *      - MySQL JDBC for database connection.
 *      - NamingContextExt for registration with the Naming Service.
 */

public class Server {
    public static void main(String[] args) {
        try {
            // Initialize ORB
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to MySQL
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/p2pfilesharing", "root", ""
            );

            // Create and register the file sharing handler
            FileSharingHandler fileSharingHandler = new FileSharingHandler(orb, connection);
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(fileSharingHandler);


            // Register with Naming Service
            NamingContextExt ncRef = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            NameComponent path[] = ncRef.to_name("FileSharingService");
            ncRef.rebind(path, ref);

            System.out.println("File Sharing Server is running...");
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

