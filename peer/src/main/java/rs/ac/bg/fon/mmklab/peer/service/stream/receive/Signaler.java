package rs.ac.bg.fon.mmklab.peer.service.stream.receive;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;

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
            protected Object call() {
                switch (signal) {
                    case REWIND:
                    case TERMINATE: {
                        receiverInstance.getToSender().println(signal); // saljemo posiljaocu da bi znao da prestane i on
                        receiver.terminate();
                    }
                    break;
                    case PAUSE:
                    case RESUME:
                        receiverInstance.getToSender().println(signal);
                        break;
                    default:
                        break;
                }
                return null;
            }
        };
    }
}
