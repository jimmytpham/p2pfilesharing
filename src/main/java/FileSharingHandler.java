/* Jimmy Pham
 *  3711704
 * Assignment 2
 * COMP 489
 */

import FileSharing.FileSharingServicePOA;
import org.omg.CORBA.ORB;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Program: FileSharingHandler
 * Description: This class implements the CORBA file sharing service, allowing users to register,
 *              remove, search, retrieve file owners, and download files.
 * Expected Inputs:
 *      - File names for registration, removal, search, and download.
 *      - User IDs for file ownership validation.
 * Expected Outputs/Results:
 *      - File registration, removal, and retrieval from the MySQL database.
 *      - File transfers using sockets over dynamically assigned ports.
 * Called by: CORBA FileSharingServicePOA implementation in the server program.
 * Will Call:
 *      - MySQL JDBC for database transactions.
 *      - ServerSocket and Socket for file transfers.
 */

public class FileSharingHandler extends FileSharingServicePOA {
    private ORB orb;
    private Connection connection;
    private static final int PORT = 5000; // Base port

    public FileSharingHandler(ORB orb, Connection connection) {
        this.orb = orb;
        this.connection = connection;
        if (this.connection == null) {
            System.err.println("Database connection is null!");
        }
    }

    // Register file in database
    @Override
    public void registerFile(String fileName, int userId) {
        try {
            String query = "INSERT INTO sharedfiles (filename) VALUES (?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, fileName);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "File registered successfully!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error registering file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Remove file from database if the user is the owner
    @Override
    public void removeFile(String fileName, int userId) {
        // Check if the user owns the file
        String checkOwnershipQuery = "SELECT f.file_id FROM sharedfiles f "
                + "JOIN fileowner fo ON f.file_id = fo.file_id "
                + "WHERE f.filename = ? AND fo.user_id = ?";

        // SQL statement
        try (PreparedStatement pstmt = connection.prepareStatement(checkOwnershipQuery)) {
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // File is found, and the user owns it, then delete it
                    String fileId = rs.getString("file_id");

                    // Delete the file from the fileowner table
                    String deleteFileOwnerQuery = "DELETE FROM fileowner WHERE file_id = ? AND user_id = ?";
                    try (PreparedStatement deletePstmt = connection.prepareStatement(deleteFileOwnerQuery)) {
                        deletePstmt.setString(1, fileId);
                        deletePstmt.setInt(2, userId);
                        deletePstmt.executeUpdate();
                    }

                    // Delete the file from the sharedfiles table
                    String deleteFileQuery = "DELETE FROM sharedfiles WHERE file_id = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM fileowner WHERE file_id = ?)";
                    try (PreparedStatement deleteSharedFilesPstmt = connection.prepareStatement(deleteFileQuery)) {
                        deleteSharedFilesPstmt.setString(1, fileId);
                        deleteSharedFilesPstmt.setString(2, fileId);
                        deleteSharedFilesPstmt.executeUpdate();
                    }

                    // File removal successful
                    JOptionPane.showMessageDialog(null, "File removed successfully!");
                } else {
                    // If owner doesn't match, show an error message
                    JOptionPane.showMessageDialog(null, "You are not the owner of this file, so it cannot be removed.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Search file in database
    @Override
    public String[] searchFile(String fileName) {
        List<String> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT filename FROM sharedfiles WHERE filename LIKE ?"
        )) {
            stmt.setString(1, "%" + fileName + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("filename"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results.toArray(new String[0]);
    }

    // Retreive file owner
    @Override
    public String getFileOwner(String fileName) {
        System.out.println("Searching for file owner: " + fileName);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT user_id FROM fileowner WHERE file_id = (SELECT file_id FROM sharedfiles WHERE filename = ?)"
        )) {
            stmt.setString(1, fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Integer.toString(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retreive file info
    public String getFileInfo(int fileId) {
        String fileInfo = "File not found.";

        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT filename, size FROM sharedfiles WHERE file_id = ?"
        )) {
            pstmt.setInt(1, fileId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                fileInfo = "Filename: " + rs.getString("filename") +
                        ", Size: " + rs.getLong("size") + " bytes";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fileInfo;
    }

    // Download file
    @Override
    public String downloadFile(String fileName) {
        try {
            String query = "SELECT file_id FROM sharedfiles WHERE filename = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, fileName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Retrieve the file data as BLOB
                    Blob fileBlob = rs.getBlob("file_id");

                    if (fileBlob != null) {
                        // Create a temporary file to store the downloaded data
                        File tempFile = new File(System.getProperty("user.dir") + File.separator + fileName);

                        // Write the BLOB data to the temporary file
                        try (InputStream inputStream = fileBlob.getBinaryStream();
                             FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        // Start file server
                        int port = PORT + (int) (Math.random() * 1000);

                        // Create a new thread to send the file over the socket
                        new Thread(() -> sendFile(tempFile, port)).start();

                        return "File " + fileName + " is available on port " + port;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "File not found in the database.";
    }

    private void sendFile(File file, int port) {
        try {
            // Send file over socket
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                Socket clientSocket = serverSocket.accept();

                // Stream the file to the client
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream())) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    bos.flush();
                    System.out.println("File sent successfully to client!");

                } catch (IOException e) {
                    System.err.println("Error reading or sending file: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.err.println("Error setting up server socket: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error in sendFile method: " + e.getMessage());
            e.printStackTrace();
        }
    }
}