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
                    case TERMINATE:
                        terminate();
                        break;
                    case PAUSE:
                        pause();
                        break;
                    case RESUME:
                        resume();
                        break;
                    case REWIND:
                        rewind();
                        break;
                    default:
                        break;
                }
                return null;
            }
        };
    }

    private void rewind() {
//        prvo preko vec postojece konekcije saljemo signal za premotavanje a nakon toga odmah saljemo na koliko frejmova da se premota na osnovu pomerenog slajdera
        receiverInstance.getToSender().println(Signal.REWIND);
        try {
            if (!Signal.valueOf(receiverInstance.getFromSender().readLine()).equals(Signal.ACCEPT)) return;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Posiljalac nije prihvatio signal za premotavanje");
        }



    }

    private void terminate() {
        receiverInstance.getToSender().println(Signal.TERMINATE);
        try {
            if (Signal.valueOf(receiverInstance.getFromSender().readLine()).equals(Signal.ACCEPT)) {
                System.out.println("Posiljalac prihvatio signal za prekid");
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Posiljalac nije prihvatio signal za prekid");
        }
        receiverInstance.getSourceLine().stop();
        receiver.closeUDPConnection();
        receiver.closeTCPConnection();
        receiver.cancel();
    }

    private void pause() {
        receiverInstance.getToSender().println(Signal.PAUSE);
        try {
            if (Signal.valueOf(receiverInstance.getFromSender().readLine()).equals(Signal.ACCEPT)) {
                System.out.println("Posiljalac prihvatio signal za pauzu");
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Posiljalac nije prihvatio signal za pauzu");
        }
    }

    private void resume() {
        receiverInstance.getToSender().println(Signal.RESUME);
        System.out.println("Poslat signal posiljaocu da nastavi slanje");
    }
}
