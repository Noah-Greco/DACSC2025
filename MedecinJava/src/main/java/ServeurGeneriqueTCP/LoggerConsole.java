package ServeurGeneriqueTCP;

public class LoggerConsole implements Logger {

    @Override
    public void Trace(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }
}
