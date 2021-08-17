package rs.ac.bg.fon.mmklab.peer.service.stream.receive;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;

import java.io.IOException;

public class Signaler extends Service<Signal> {

    private final Signal signal;
    private final ReceiverInstance receiverInstance;
    private final Receiver receiver;

    public Signaler(Signal signal, Receiver receiver) {
        this.signal = signal;
        this.receiver = receiver;
        this.receiverInstance = this.receiver.getInstance();
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                switch (signal) {
                    case TERMINATE:
                        terminate();
                    break;
                    case PAUSE:
                        pause();
                    break;
                    case RESUME:
                        resume();
                    break;
                    default:
                        break;
                }
                return null;
            }
        };
    }

    private void terminate(){
        receiverInstance.getToSender().println(Signal.TERMINATE);
        try {
            if (receiverInstance.getFromSender().readLine().equals("Signal accepted")) {
                System.out.println("Posiljalac prihvatio signal za prekid");
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Posiljalac nije prihvatio signal za prekid");
        }
        receiverInstance.getSourceLine().stop();
        receiver.closeUDPConnection();
        try {
            receiver.closeTCPConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Signaler -> terminate: nije moguce zatvoriti tcp konekciju");
        }
    }

    private void pause() {
        receiverInstance.getToSender().println(Signal.PAUSE);
        try {
            if (receiverInstance.getFromSender().readLine().equals("Signal accepted")) {
                System.out.println("Posiljalac prihvatio signal za pauzu");
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Posiljalac nije prihvatio signal za pauzu");
        }
    }

    private void resume(){
        receiverInstance.getToSender().println(Signal.RESUME);
        System.out.println("Poslat signal posiljaocu da nastavi slanje");
    }
}
