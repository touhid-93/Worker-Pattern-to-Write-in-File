package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import worker.one.WorkerOne;
import worker.two.WorkerTwo;
import worker.three.WorkerThree;

/**
 *
 * @author Touhid
 */
public class Server {

    private static final int PORT = 9090;
    private static ArrayList<String> GLOBAL_CONFIG_TEXT_LIST = null;
    private static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(5);
    private static int callCount_1 = -1;
    private static int callCount_2 = -1;
    private static int callCount_3 = -1;

    public static void main(String[] args) throws InterruptedException {
        GLOBAL_CONFIG_TEXT_LIST = getGlobalTextList();

        for (int i = 0; i < GLOBAL_CONFIG_TEXT_LIST.size(); i++) {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);

                String freeWorker = CheckWhoIsFree();
                System.out.println("Free Worker : " + freeWorker); //for debug
                int callCount = startWorker(freeWorker); //starting the free worker
                if(callCount == 100) break;

                System.out.println("[Server] Waiting for worker to connect...");
                Socket workerSocket = serverSocket.accept();
                System.out.println("[Server] " + freeWorker + " connected " + (callCount+1) + " times");

                WorkerHandler newFreeWorker = new WorkerHandler(workerSocket, callCount, GLOBAL_CONFIG_TEXT_LIST.get(i));
                THREAD_POOL.execute(newFreeWorker);

                TimeUnit.SECONDS.sleep(1); //delay a little bit to let stream be finished before closing the sockets

                workerSocket.close();
                serverSocket.close();

            } catch (IOException ex) {
                System.err.println("IOException : " + ex.getMessage());
            } catch (InterruptedException ex) {
                System.err.println("InterruptedException : " + ex.getMessage());
            }
            System.out.println();
            System.out.println();
        }
        
        TimeUnit.SECONDS.sleep(5); //delay a little bit to let the stream be finished
        WorkerOne.PrintInternalTexts(callCount_1+1);
        WorkerTwo.PrintInternalTexts(callCount_2+1);
        WorkerThree.PrintInternalTexts(callCount_3+1);
        
        System.exit(0);
    } //psvm end

    private static ArrayList<String> getGlobalTextList() {
        try {
            Connection connection = new DBConnect().getConnection();
            if (connection != null) {
                Statement sqlCommand = (Statement) connection.createStatement();
                ResultSet resultset = sqlCommand.executeQuery("SELECT text FROM global_writting_config");

                ArrayList<String> textList = new ArrayList<>();

                while (resultset.next()) {
                    textList.add(resultset.getString("text"));
                }
                resultset.close();
                sqlCommand.close();
                connection.close();

                return textList;
            }

        } catch (SQLException ex) {
            System.err.println("SQLException : " + ex.getMessage());
            return null;
        }
        return null;
    } // getGlobalTextList end

    private static String CheckWhoIsFree() {
        while (true) {
            try {
                Connection connection = new DBConnect().getConnection();
                if (connection != null) {
                    Statement sqlCommand = (Statement) connection.createStatement();
                    ResultSet resultset = sqlCommand.executeQuery("SELECT worker_name FROM worker WHERE state = 'free' LIMIT 1");

                    String freeWorkerName = null;

                    while (resultset.next()) {
                        freeWorkerName = resultset.getString("worker_name");
                    }
                    resultset.close();
                    sqlCommand.close();
                    connection.close();

                    if (freeWorkerName == null) { //keeps the loop running until the method finds a free worker
                        continue;
                    }

                    return freeWorkerName;
                }
            } catch (SQLException ex) {
                System.err.println("SQLException : " + ex.getMessage());
                return null;
            }
            return null;
        }
    } //checkWhoIsFree end

    private static int startWorker(String freeWorker) {
        System.out.println("Starting " + freeWorker + "...");
        ProcessBuilder processBuilder = new ProcessBuilder();

        if (freeWorker.equals("worker-1")) {
            callCount_1++;
            String arg = "java -cp Worker1.jar worker.one.WorkerOne";
            processBuilder.command("cmd.exe", "/c", arg);

            try {
                processBuilder.start();
                return callCount_1;
            } catch (IOException ex) {
                System.err.println("IOException : " + ex.getMessage());
                return 100;
            }
        }
        
        if (freeWorker.equals("worker-2")) {
            callCount_2++;
            String arg = "java -cp Worker2.jar worker.two.WorkerTwo";
            processBuilder.command("cmd.exe", "/c", arg);

            try {
                processBuilder.start();
                return callCount_2;
            } catch (IOException ex) {
                System.err.println("IOException : " + ex.getMessage());
                return 100;
            }
        }
        
        if (freeWorker.equals("worker-3")) {
            callCount_3++;
            String arg = "java -cp Worker3.jar worker.three.WorkerThree";
            processBuilder.command("cmd.exe", "/c", arg);

            try {
                processBuilder.start();
                return callCount_3;
            } catch (IOException ex) {
                System.err.println("IOException : " + ex.getMessage());
                return 100;
            }
        }
        return 100;
    } //startWorker end
}
