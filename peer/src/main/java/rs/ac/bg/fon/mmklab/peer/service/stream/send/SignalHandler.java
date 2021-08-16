package rs.ac.bg.fon.mmklab.peer.service.stream.send;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

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
//                    System.out.println(">>    Pokrenut je Signal handler; ocekujemo zahtev; nit: " + Thread.currentThread());
                    Signal signal = Signal.valueOf(handler.getInstance().getFromReceiver().readLine());
                    System.out.println("Prijem signala -----> " + signal + "; nit: " + Thread.currentThread());
                    System.out.println("Hash code hendlera u SignalHandler-u: " + handler.hashCode());

                    synchronized (handler){
                        switch (signal) {
                            case TERMINATE:
                                handler.getInstance().setSignal(Signal.TERMINATE);
                                break;
                            case PAUSE: {
                                handler.getInstance().setSignal(Signal.PAUSE);
                                System.out.println("Postavljen signal na pause");
                            }
                            break;
                            case RESUME: {
                                System.out.println("Usli smo u signalHandler switch -> resume");
                                handler.getInstance().setSignal(Signal.RESUME);
                                handler.notify();
                                System.out.println("Postavljen signal na resume");
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


//    private void terminate() {
//        System.out.println("Pokrenuta metoda nakon signala : terminate");
//
//        handler.getInstance().getToReceiver().println("Signal accepted");
//        handler.cancel();
////        ne moramo da prekidamo ovde nikakvo slanje jer ce svakako peer da zatvori sourceDataLine i onda stajemo sa slanjem
//    }
//
//    private void pause(){
//
//
//        System.out.println("Pokrenuta metoda nakon signala : pause");
//
//        handler.getInstance().getToReceiver().println("Signal accepted");
//        try {
//            handler.getInstance().setFramesSent(Integer.parseInt(handler.getInstance().getFromReceiver().readLine()));
//        } catch (IOException e) {
////            e.printStackTrace();
//            System.err.println("(SignalHandler --> pause): neuspesan prijem broja procitanih frejmova od strane primaoca");
//        }
//
//        System.out.println("(Signal handler): korisnik prekinuo video, do sada je porcitano frejmova: " + handler.getInstance().getFramesSent());
//        try {
//            handler.getInstance().getAudioInputStream().close();
//            System.out.println(">>>>>>>>>>>>>>>>>>> audio tok od fajla zatvoren");
//        } catch (IOException e) {
////            e.printStackTrace();
//            System.err.println("(SignalHandler -> pause); nije moguce zatvoriti audioInputStream na strani posiljaoca");
//        }
//    }
//
//    private void resume(){
//        System.out.println("Pokrenuta metoda nakon signala : resume");
//
//        try {
//            int frameSize = handler.getInstance().getAudioInputStream().getFormat().getFrameSize();
//
//            handler.getInstance().setAudioInputStream(AudioSystem.getAudioInputStream(handler.getInstance().getAudioFile()));
//            handler.getInstance().getAudioInputStream().skip(handler.getInstance().getFramesSent() * frameSize); // preskace onaj deo koji je bio strimovan nakon pauziranja
//
////            proba
//            new SignalHandler(handler).start();
//            System.out.println("Trenutni PeerHandler u metodi resume: " + handler.getInstance().hashCode() + ", audioInputStream za trenutni hendelr: " + handler.getInstance().getAudioInputStream().hashCode());
//
//            handler.send();
//            handler.closeTCPConnection(handler.getInstance().getSocket(), handler.getInstance().getToReceiver(), handler.getInstance().getFromReceiver());
//            handler.closeUDPConnection(handler.getInstance().getDatagramSocket());
//
//
//        } catch (UnsupportedAudioFileException e) {
////            e.printStackTrace();
//            System.err.println("(SignalHandler -> resume): nismo mogli da ponovo napravimo tok od audio fajla, jer fajl nije podrzan");
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("(SignalHandler -> resume): nismo mogli da ponovo napravimo tok od audio fajla");
//        }
//    }

}
