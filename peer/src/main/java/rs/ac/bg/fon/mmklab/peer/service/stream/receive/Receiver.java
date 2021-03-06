package rs.ac.bg.fon.mmklab.peer.service.stream.receive;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;
import rs.ac.bg.fon.mmklab.peer.ui.components.alert.ErrorDialog;
import rs.ac.bg.fon.mmklab.peer.ui.components.audio_player.AudioPlayer;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import javax.sound.sampled.*;
import java.io.*;
import java.net.DatagramPacket;

public class Receiver extends Service<AudioBook> {
//    nit slusaoca krece sa radom onog trenutka kad korisnik odabere knjigu koju hoce da slusa

    //    instance je objekat koji sadrzi reference svih tokova, soketa, audio fajla, adrese primaoca, udp port primaoca i evideciju o broju procitanih frejmova
    private final ReceiverInstance instance;

    private Receiver(ReceiverInstance receiverInstance) {
        this.instance = receiverInstance;
    }

    public static Receiver createInstance(AudioBook book, Configuration configuration) throws IOException, LineUnavailableException {
        return new Receiver(ReceiverInstance.createReceiverInstance(book, configuration));
    }


    public ReceiverInstance getInstance() {
        return instance;
    }

    @Override
    protected Task<AudioBook> createTask() {
        return new Task<>() {
            @Override
            protected AudioBook call() throws Exception {
                establishConnection();
                receive();
                return null;
            }
        };
    }

    private void establishConnection() {
        instance.getToSender().println(Signal.CHECK_AVAILABILITY);
        try {
            Signal res = Signal.valueOf(instance.getFromSender().readLine());
            if (res.equals(Signal.SPECIFY_BOOK)) {
                instance.getToSender().println(JsonConverter.toJSON(instance.getAudioBook()));
                instance.getToSender().println(instance.getConfiguration().getLocalPortUDP());
                if (Signal.valueOf(instance.getFromSender().readLine()).equals(Signal.GET_STARTING_FRAME))
                    instance.getToSender().println(instance.getFramesRead());
                System.out.println("Posiljaocu poslat startni frejm: " + instance.getFramesRead());

                System.out.println("Poslata knjiga posiljaocu: " + JsonConverter.toJSON(instance.getAudioBook()));
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Greska (Receiver -> establishConnection): sender nije poslao poruku na nas odgovor");

        }
    }

    private void receive() throws IOException, LineUnavailableException {

//        datagram soket, sourceLine i audio format su postavljeni pri kreiranju instance Receiver-a,
//        na osnovu parametara audioBook i configuration koji se prosledjuju prilikom kreiranja listener-a za dugme knjige

        instance.getSourceLine().open(instance.getAudioFormat()); // otvaranje linije ka mikseru
        instance.getSourceLine().start();

        int framesize = instance.getAudioBook().getAudioDescription().getFrameSizeInBytes();
        byte[] receiveBuffer = new byte[1024 * framesize]; //i ovde bi trebalo da nam se posalje koja je velicina
        byte[] confirmationBuffer = Signal.DATAGRAM_RECEIVED.toString().getBytes();
        System.out.println("Krece prijem datagram paketa sa mreze");


        /* definisanje paketa koje saljemo i primamo*/
        DatagramPacket receivePacket;
        DatagramPacket signalPacket;


        /* ova petlja vrti dok god ne dodjemo do kraja knjige, a koliko je duga knjiga znamo na osnovu podataka koje smo pokupili od servera metodom getAvailableBooks*/
        while (instance.getFramesRead() < instance.getAudioBook().getAudioDescription().getLengthInFrames()) {
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            instance.setFramesRead(instance.getFramesRead() + receiveBuffer.length / framesize);

            try {
                instance.getDatagramSocket().receive(receivePacket);
            } catch (IOException e) {
                new ErrorDialog("Problem na mre??i", "Po??iljalac postao nedostupan,\nponovo odaberite knjigu za slu??anje").show();
//                e.printStackTrace();
            }

            try {
                instance.getSourceLine().write(receiveBuffer, 0, receiveBuffer.length);
                AudioPlayer.updateTimeSlider(instance);
            } catch (Exception e) {
//                e.printStackTrace();
                System.err.println("Nije moguce upisati nista na liniju");
                new ErrorDialog("Problem pri reprodukciji", "Nije mogu??e pristupiti mikseru,\nponovo pokrenite aplikaciju").show();
            }
            signalPacket = new DatagramPacket(
                    confirmationBuffer, confirmationBuffer.length, receivePacket.getAddress(), receivePacket.getPort());
            instance.getDatagramSocket().send(signalPacket); //paket potvrde omogu??ava da po??iljalac ne ??alje pakete odmah, ve?? da sa??eka da se ceo bafer isprazni i ode ka mikseru
        }
//        nakon zavrsenog prijema ubijamo receiver nit
        terminate();
        System.out.println("Kraj prenosa, soketi i tokovi zatvoreni na strani klijenta");

    }

    public void restart(double startingPoint){
        //        parametri potrebni za pokretanje novog receivera
        AudioBook book = instance.getAudioBook();
        Configuration config = instance.getConfiguration();

//      kad zatvorimo konekciju ubijamo dosadasnjeg receivera da bismo pokrenuli novog
        terminate();

//        instanciranje novog receivera
        try {
            Receiver receiver = Receiver.createInstance(book, config);
            receiver.getInstance().setFramesRead((long) startingPoint);
            receiver.start();
            AudioPlayer.setReceiver(receiver);
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Nije moguce pokrenuti novog receivera, verovatno zbog zauzetog porta");
            new ErrorDialog("Problem na mre??i", "Po??iljalac postao nedostupan,\nponovo u??itajte dostupne knjige ").show();

        } catch (LineUnavailableException e) {
//            e.printStackTrace();
            System.err.println("Nije moguce pokrenuti novog receivera jer je zauzeta sourceDataLine");
            new ErrorDialog("Problem pri reprodukciji", "Nije mogu??e pristupiti mikseru,\nponovo pokrenite aplikaciju").show();
        }
        System.out.println("Ponovo pokrenut receiver");
    }

    public void terminate(){
        instance.getSourceLine().stop();
        this.closeUDPConnection();
        this.closeTCPConnection();
        this.cancel(); // ubijanje receiver niti
    }

    private void closeUDPConnection() {
        instance.getDatagramSocket().close();
    }

    private void closeTCPConnection() {
        try {
            instance.getSocket().close();
            instance.getToSender().close();
            instance.getFromSender().close();
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Nije moguce zatvoriti TCP konekciju");
        }
    }


}
