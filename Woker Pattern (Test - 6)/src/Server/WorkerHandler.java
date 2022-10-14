package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Touhid
 */
public class WorkerHandler implements Runnable {

    private int CALL_COUNT = 0;
    private String GLOBAL_TEXT_FROM_SERVER;
    private PrintWriter OUT_TO_WORKER;

    WorkerHandler(Socket workerSocket, int callCount, String globalTextFromServer) {
        this.CALL_COUNT = callCount;
        this.GLOBAL_TEXT_FROM_SERVER = globalTextFromServer;
        try {
            OUT_TO_WORKER = new PrintWriter(workerSocket.getOutputStream(), true);
        } catch (IOException ex) {
            System.err.println("IOException : " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Global Text Sent : " + GLOBAL_TEXT_FROM_SERVER); //for debug
        OUT_TO_WORKER.println(GLOBAL_TEXT_FROM_SERVER + "," + CALL_COUNT);
    }
}
