/* Jimmy Pham
 *  3711704
 * Assignment 2
 * COMP 489
 */

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.io.*;
import java.sql.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import FileSharing.FileSharingService;
import FileSharing.FileSharingServiceHelper;

/*
 * Program: ClientGUI
 * Description: This program provides a graphical user interface (GUI) for the P2P file sharing client.
 *              It allows the user to interact with the file sharing server, browse for files,
 *              and send requests for file downloads or uploads.
 * Expected Inputs:
 *      - User selects files to upload or download via the GUI.
 *      - User interacts with buttons, text fields, and other GUI components to initiate actions.
 * Expected Outputs/Results:
 *      - Displays a GUI with buttons, text fields, and lists for file management.
 *      - Allows file selection, initiation of file transfer requests, and display of progress.
 *      - Prints status updates (e.g., "File uploaded successfully", "Download complete") in the GUI.
 * Called by: This program is called directly from the main method to initialize the GUI.
 * Will Call:
 *      - CORBA client components to communicate with the server.
 *      - File system functions to browse and manage files.
 *      - GUI components (JFrame, JButton, JTextField, etc.) for user interaction.
 */


public class ClientGUI {
    static FileSharingService fileSharing;

    // List for search files results
    private DefaultListModel<String> searchResultsModel;
    private JList<String> searchResultsList;

    // List for user owned files results
    private DefaultListModel<String> userFilesModel;
    private JList<String> userFilesList;

    // GUI buttons and text fields
    private JTextField searchField;
    private JTextField usernameField;
    private JButton downloadButton;
    private JButton registerButton;
    private JButton removeButton;
    private JButton signInButton;

    // store current user ID
    private int currentUserId = -1;

    // Set up GUI
    public ClientGUI() {
        setupUI();
    }

    private void setupUI() {
        JFrame frame = new JFrame("P2P File Sharing");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Sign On Panel
        JPanel signOnPanel = new JPanel(new BorderLayout());
        usernameField = new JTextField(20);
        signInButton = new JButton("Enter");

        signInButton.addActionListener(e -> signInUser());
        signOnPanel.add(usernameField, BorderLayout.NORTH);
        signOnPanel.add(signInButton, BorderLayout.SOUTH);

        // Search and Download Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);

        searchButton.addActionListener(e -> searchFiles());

        downloadButton = new JButton("Download");
        downloadButton.addActionListener(e -> downloadFile());

        JPanel searchButtonsPanel = new JPanel();
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(downloadButton);

        searchPanel.add(searchField, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(searchResultsList), BorderLayout.CENTER);
        searchPanel.add(searchButtonsPanel, BorderLayout.SOUTH);

        // Register and Remove files Panel
        JPanel registerPanel = new JPanel(new BorderLayout());
        JTextField fileActionField = new JTextField(20);
        registerButton = new JButton("Register File");
        removeButton = new JButton("Remove File");

        // User Files Panel
        userFilesModel = new DefaultListModel<>();
        userFilesList = new JList<>(userFilesModel);

        // Panel to show user's files
        JPanel userFilesPanel = new JPanel();
        userFilesPanel.add(new JScrollPane(userFilesList));

        registerButton.addActionListener(e -> registerFiles(fileActionField.getText()));
        removeButton.addActionListener(e -> removeFile(fileActionField.getText()));

        JPanel registerButtonsPanel = new JPanel();
        registerButtonsPanel.add(registerButton);
        registerButtonsPanel.add(removeButton);

        registerPanel.add(fileActionField, BorderLayout.NORTH);
        registerPanel.add(registerButtonsPanel, BorderLayout.CENTER);
        registerPanel.add(userFilesPanel, BorderLayout.SOUTH); // Show user's files

        // Sign On Panel at the top, Search/Registration panels below
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, signOnPanel, new JPanel());
        topSplitPane.setDividerLocation(100);

        // Left (Search Panel) and Right (Register Panel)
        JSplitPane leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchPanel, registerPanel);
        leftRightSplitPane.setDividerLocation(300);

        // Top and Bottom panel split
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplitPane, leftRightSplitPane);
        mainSplitPane.setDividerLocation(150);

        frame.add(mainSplitPane);
        frame.setVisible(true);
    }

    // Sign in user based on username
    private void signInUser() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a username.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/p2pfilesharing", "root", "")) {
            // Check if the username already exists
            String checkUserQuery = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkUserQuery)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // If username exists, sign the user in
                    currentUserId = rs.getInt("user_id");
                    JOptionPane.showMessageDialog(null, "User signed in with ID: " + currentUserId);
                    displayUserFiles(); // update displayed files
                } else {
                    // If username doesn't exist, create new user into database
                    String insertUserQuery = "INSERT INTO users (username) VALUES (?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, username);
                        insertStmt.executeUpdate();
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            currentUserId = generatedKeys.getInt(1);
                            JOptionPane.showMessageDialog(null, "User signed up with ID: " + currentUserId);
                            displayUserFiles(); //update displayed files
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error signing in user: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void searchFiles() {
        String fileName = searchField.getText();
        searchResultsModel.clear();
        try {
            String[] results = fileSharing.searchFile(fileName);
            for (String file : results) {
                searchResultsModel.addElement(file); // add search results to the list
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error searching for files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void downloadFile() {
        String selectedFile = searchResultsList.getSelectedValue();
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please Select a File to Download");
            return;
        }
        try {
            String ownerId = fileSharing.getFileOwner(selectedFile);
            if (ownerId != null) {
                // Request download and get port
                String response = fileSharing.downloadFile(selectedFile);
                if (response.startsWith("File not found")) {
                    JOptionPane.showMessageDialog(null, "File not found on the server.");
                    return;
                }

                // Get server response for port
                String[] parts = response.split(" ");
                try {
                    int port = Integer.parseInt(parts[parts.length - 1]); // get port number
                    // Connect to file server and receive file
                    receiveFile("127.0.0.1", port, selectedFile);
                    JOptionPane.showMessageDialog(null, "Download complete: " + selectedFile);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Error from server: " + response);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error downloading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void receiveFile(String serverIP, int serverPort, String fileName) {
        try (Socket socket = new Socket(serverIP, serverPort);
             InputStream in = socket.getInputStream()) {

            String appDirectory = System.getProperty("user.dir");

            // download to a shared_files folder
            String sharedFilesFolderPath = appDirectory + File.separator + "shared_files";

            File folder = new File(sharedFilesFolderPath);

            if (!folder.exists()) {
                folder.mkdir();  // Create the folder if it doesn't exist
            }

            File file = new File(sharedFilesFolderPath + File.separator + fileName);


            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    // Write file data
                    bos.write(buffer, 0, bytesRead);
                }
            }

        } catch (IOException e) {
            System.err.println("Error receiving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerFiles(String fileName) {
        if (fileName.isEmpty() || currentUserId == -1) {
            JOptionPane.showMessageDialog(null, "Please sign in and enter a file name.");
            return;
        }

        try {
            // Get file size based on file type
            long fileSize = getFileSize(fileName);
            if (fileSize == -1) {
                JOptionPane.showMessageDialog(null, "Unsupported file type.");
                return;
            }

            // Get checksum
            String checksum = fileName.substring(0, fileName.lastIndexOf('.'));

            // Insert into sharedfiles table
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/p2pfilesharing", "root", "")) {
                String insertFileQuery = "INSERT INTO sharedfiles (filename, size, checksum, user_id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertFileQuery, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, fileName);
                    stmt.setLong(2, fileSize);
                    stmt.setString(3, checksum);
                    stmt.setLong(4, currentUserId);
                    stmt.executeUpdate();

                    // Create file_id
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int fileId = generatedKeys.getInt(1);

                        // Insert into fileowner table
                        String insertOwnerQuery = "INSERT INTO fileowner (user_id, file_id) VALUES (?, ?)";
                        try (PreparedStatement ownerStmt = conn.prepareStatement(insertOwnerQuery)) {
                            ownerStmt.setInt(1, currentUserId);
                            ownerStmt.setInt(2, fileId);
                            ownerStmt.executeUpdate();
                            JOptionPane.showMessageDialog(null, "File registered successfully!");
                            displayUserFiles();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error registering file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // automated file size based on file extension
    private long getFileSize(String fileName) {
        String fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        switch (fileType) {
            case "txt":
                return 1024;
            case "mp4":
                return 2048000;
            case "pdf":
                return 512000;
            default:
                return -1; // Default
        }
    }

    private void removeFile(String fileName) {
        if (fileName.isEmpty() || currentUserId == -1) {
            JOptionPane.showMessageDialog(null, "Please sign in and enter a file name.");
            return;
        }

        // Insert into sharedfiles table
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/p2pfilesharing", "root", "")) {
            // Check if signed on user owns the file
            String checkOwnershipQuery = "SELECT f.file_id FROM sharedfiles f "
                    + "JOIN fileowner fo ON f.file_id = fo.file_id "
                    + "WHERE f.filename = ? AND fo.user_id = ?";

            // Prepare the SQL statement
            try (PreparedStatement pstmt = conn.prepareStatement(checkOwnershipQuery)) {
                pstmt.setString(1, fileName);
                pstmt.setInt(2, currentUserId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // File is found, and the user owns it, then delete it
                        String fileId = rs.getString("file_id");

                        // Delete the file from the fileowner table
                        String deleteFileOwnerQuery = "DELETE FROM fileowner WHERE file_id = ? AND user_id = ?";
                        try (PreparedStatement deletePstmt = conn.prepareStatement(deleteFileOwnerQuery)) {
                            deletePstmt.setString(1, fileId);
                            deletePstmt.setInt(2, currentUserId);
                            deletePstmt.executeUpdate();
                        }

                        // Delete the file from the sharedfiles table
                        String deleteFileQuery = "DELETE FROM sharedfiles WHERE file_id = ? AND NOT EXISTS "
                                + "(SELECT 1 FROM fileowner WHERE file_id = ?)";
                        try (PreparedStatement deleteSharedFilesPstmt = conn.prepareStatement(deleteFileQuery)) {
                            deleteSharedFilesPstmt.setString(1, fileId);
                            deleteSharedFilesPstmt.setString(2, fileId);
                            deleteSharedFilesPstmt.executeUpdate();
                        }

                        // File removal successful
                        JOptionPane.showMessageDialog(null, "File removed successfully!");
                        displayUserFiles();
                    } else {
                        // If owner doesn't match, show an error message
                        JOptionPane.showMessageDialog(null, "You are not the owner of this file, so it cannot be removed.");
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayUserFiles() {
        // Clear list and display updated list
        userFilesModel.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/p2pfilesharing", "root", "")) {
            String query = "SELECT f.filename FROM sharedfiles f " +
                    "JOIN fileowner fo ON f.file_id = fo.file_id " +
                    "WHERE fo.user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, currentUserId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    userFilesModel.addElement(rs.getString("filename"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching user files: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            fileSharing = FileSharingServiceHelper.narrow(ncRef.resolve_str("FileSharingService"));
            new ClientGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
