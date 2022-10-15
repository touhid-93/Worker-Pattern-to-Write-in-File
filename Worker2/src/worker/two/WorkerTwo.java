package worker.two;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Touhid
 */
public class WorkerTwo {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;
    private static ArrayList<String> INTERNAL_TEXT_LIST = null;
    private static String GLOBAL_FILE_PATH = null;

    public static void main(String[] args) {

        INTERNAL_TEXT_LIST = GetInternalTextList();
        GLOBAL_FILE_PATH = GetGlobalFilePath();

        try {
            Socket workerSocket = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
            String serverText = serverReader.readLine();
            String[] split = serverText.split(",");
            String globalText = split[0];
            int callCount = Integer.parseInt(split[1]);

            FileWriter fileWriter = new FileWriter(GLOBAL_FILE_PATH, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            FileWriter fileWriterLog = new FileWriter("workerTwo_LOG.txt");
            BufferedWriter bufferedWriterLog = new BufferedWriter(fileWriterLog);

            //setting worker in busy state..
            boolean isTrue = updateStateBusy();
            if (isTrue == false) {
                System.err.println("Database Update Failed!");
            } else {
                System.out.println("Worker-2 is busy!");
            }

            bufferedWriter.write("worker - 2 - " + globalText + " - " + getDateTime());
            bufferedWriter.newLine();
            bufferedWriter.write("*5s delay*");

            TimeUnit.SECONDS.sleep(5); //keeping workerOne running for 5 seconds

            bufferedWriter.newLine();
            bufferedWriter.write("worker - 2 - " + INTERNAL_TEXT_LIST.get(callCount) + " - " + getDateTime());
            bufferedWriter.newLine();

            //setting worker as free
            isTrue = updateStateFree();
            if (isTrue == false) {
                System.err.println("Database update failed!");
            } else {
                System.out.println("Worker-2 is free!");
            }

            bufferedWriter.close();
            bufferedWriterLog.close();
            fileWriter.close();
            fileWriterLog.close();
            serverReader.close();
            workerSocket.close();

        } catch (IOException ex) {
            System.err.println("IOException (PSVM) : " + ex.getMessage());
        } catch (InterruptedException ex) {
            System.err.println("InterruptedException (PSVM) : " + ex.getMessage());
        }
    }

    private static Connection DBConnect() {
        String username = "root";
        String password = "2222";
        String url = "jdbc:mysql://localhost:3306/worker_test";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);

            return connection;
        } catch (SQLException ex) {
            System.err.println("SQLException (DBConnect()) : " + ex.getMessage());
            return null;
        } catch (ClassNotFoundException ex) {
            System.err.println("ClassNotFoundException (DBConnect()) : " + ex.getMessage());
            return null;
        }
    } //DBConnect end

    private static boolean updateStateBusy() {
        try {
            Connection connection = DBConnect();
            if (connection != null) {
                Statement sqlCommand = (Statement) connection.createStatement();
                int resultset = sqlCommand.executeUpdate("UPDATE worker SET state = 'busy' WHERE worker_name = 'worker-2'");

                sqlCommand.close();
                connection.close();

                return resultset > 0;
            }
        } catch (SQLException ex) {
            System.err.println("SQLException (updateStateBusy()) : " + ex.getMessage());
            return false;
        }
        System.err.println("Error : OutOfScope (updateStateBusy())");
        return false;
    } //updateStateBusy end

    private static boolean updateStateFree() {
        try {
            Connection connection = DBConnect();
            if (connection != null) {
                Statement sqlCommand = (Statement) connection.createStatement();
                int resultset = sqlCommand.executeUpdate("UPDATE worker SET state = 'free' WHERE worker_name = 'worker-2'");

                sqlCommand.close();
                connection.close();

                return resultset > 0;
            }
        } catch (SQLException ex) {
            System.err.println("SQLException (updateStateFree()) : " + ex.getMessage());
            return false;
        }
        System.err.println("Error : OutOfScope (updateStateFree())");
        return false;
    } //updateStateFree end

    private static ArrayList<String> GetInternalTextList() {

        try {
            Connection connection = DBConnect();
            if (connection != null) {
                Statement sqlCommand = (Statement) connection.createStatement();
                ResultSet resultset = sqlCommand.executeQuery("SELECT text1, text2, text3, text4 FROM worker WHERE worker_name = 'worker-2'");

                ArrayList<String> textList = new ArrayList<>();

                while (resultset.next()) {
                    textList.add(resultset.getString("text1"));
                    textList.add(resultset.getString("text2"));
                    textList.add(resultset.getString("text3"));
                    textList.add(resultset.getString("text4"));
                }
                resultset.close();
                sqlCommand.close();
                connection.close();

                return textList;
            }

        } catch (SQLException ex) {
            System.err.println("SQLException (GetInternalTextList()) : " + ex.getMessage());
            return null;
        }
        System.err.println("Error : OutOfScope (GetInternalTextList())");
        return null;
    } //GetInternalTextList end

    private static String GetGlobalFilePath() {
        try {
            Connection connection = DBConnect();
            if (connection != null) {
                Statement sqlCommand = (Statement) connection.createStatement();
                ResultSet resultset = sqlCommand.executeQuery("SELECT file_path FROM worker WHERE worker_name = 'worker-2'");

                String filePath = null;

                while (resultset.next()) {
                    filePath = resultset.getString("file_path");
                }
                resultset.close();
                sqlCommand.close();
                connection.close();

                return filePath;
            }

        } catch (SQLException ex) {
            System.err.println("SQLException (GetGlobalFilePath()) : " + ex.getMessage());
            return null;
        }
        System.err.println("Error : OutOfScope (GetGlobalFilePath())");
        return null;
    } //GetGlobalFilePath end

    private static String getDateTime() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("d/MMM/y h:m a").format(calendar.getTime());
    } //getDateTime end

    public static void PrintInternalTexts(int index) {
        ArrayList<String> internalTextList = GetInternalTextList();
        int internalTextList_size = internalTextList.size();
        String globalFilePath = GetGlobalFilePath();

        try {
            FileWriter fileWriter = new FileWriter(globalFilePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.newLine();
            if (index < internalTextList_size) {
                for (int i = index; i < internalTextList_size; i++) {
                    bufferedWriter.write("worker - 2 - " + internalTextList.get(i) + " - " + getDateTime());
                    bufferedWriter.newLine();
                }
                bufferedWriter.write("worker - 2 - Task Finished");
                bufferedWriter.newLine();
            } else {
                bufferedWriter.write("worker - 2 - Task Finished");
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException ex) {
            System.err.println("IOException (PrintInternalTexts()) : " + ex.getMessage());
        }
    }
}
