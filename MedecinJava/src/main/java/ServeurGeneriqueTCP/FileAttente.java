package ServeurGeneriqueTCP;

import java.net.Socket;
import java.util.LinkedList;

public class FileAttente {

    private final LinkedList<Socket> file = new LinkedList<>();

    public synchronized void deposerSocket(Socket s) {
        file.addLast(s);
        notifyAll(); // réveille un thread client en attente
    }

    public synchronized Socket retirerSocket() throws InterruptedException {
        while (file.isEmpty()) {
            wait(); // attend qu'une socket soit déposée
        }
        return file.removeFirst();
    }
}
