package rs.ac.bg.fon.mmklab.peer.service.stream.send;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.Socket;

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

                    synchronized (handler) { // zbog notify
                        switch (signal) {
                            case TERMINATE: {

                                handler.getInstance().setSignal(Signal.TERMINATE);
                                handler.getInstance().getToReceiver().println(Signal.ACCEPT);
                                handler.closeTCPConnection();
                                handler.closeUDPConnection();
                                handler.cancel();
                            }
                            break;
                            case PAUSE: {
                                handler.getInstance().setSignal(Signal.PAUSE);
                                handler.getInstance().getToReceiver().println(Signal.ACCEPT);
                            }
                            break;
                            case RESUME: {
                                handler.getInstance().setSignal(Signal.RESUME);
                                handler.notify();
                            }
                            break;
                            case REWIND: {
//                                prijem signala za premotavanje
                                handler.getInstance().getToReceiver().println(Signal.ACCEPT);

//                                ubijanje dosadasnjeg hendlera, novi handler bi trebalo da se napravi pi pravljenju ReceiverInstance
                                handler.closeTCPConnection();
                                handler.closeUDPConnection();
//                                handler.getInstance().getAudioInputStream().close();
                                handler.cancel();

                            }
                            break;
                            default:
                                break;
                        }
                    }
                }
                return null;
            }
        };
    }


}
