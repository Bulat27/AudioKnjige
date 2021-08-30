package rs.ac.bg.fon.mmklab.peer.service.stream.send;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;


public class SignalHandler extends Service {

    private final PeerHandler handler;

    public SignalHandler(PeerHandler handler) {
        this.handler = handler;
    }


    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                while (!handler.getInstance().getSocket().isClosed()) {
                    Signal signal = Signal.valueOf(handler.getInstance().getFromReceiver().readLine());
                    System.out.println("Prijem signala -----> " + signal + "; nit: " + Thread.currentThread());

                    synchronized (handler) { // zbog notify, postavljanje lock-a na handler objektu
                        handler.getInstance().setSignal(signal);
                        if (signal.equals(Signal.RESUME))
                            handler.notify(); // u slucaju nastavljanja moramo da pokrenemo ponovo hendler nit koja je metodom wait() bila zaustavljena
                    }
                }
                return null;
            }
        };
    }


}
