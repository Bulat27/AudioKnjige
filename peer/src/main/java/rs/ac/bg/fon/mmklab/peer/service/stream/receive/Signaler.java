package rs.ac.bg.fon.mmklab.peer.service.stream.receive;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;
import rs.ac.bg.fon.mmklab.peer.ui.components.audio_player.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
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
                    case TERMINATE: {
                        receiverInstance.getToSender().println(Signal.TERMINATE); // saljemo posiljaocu da bi znao da prestane i on
                        receiver.terminate();
                        System.out.println("Prenos prekinut");
                    }
                    break;
                    case PAUSE:
                        receiverInstance.getToSender().println(Signal.PAUSE);
                        break;
                    case RESUME:
                        receiverInstance.getToSender().println(Signal.RESUME);
                        break;
                    case REWIND: {
                        receiverInstance.getToSender().println(Signal.REWIND);
                        receiver.terminate(); // jer cemo svakako novog receiver-a da pokrenemo
                    }
                    break;
                    default:
                        break;
                }
                return null;
            }
        };
    }
}
