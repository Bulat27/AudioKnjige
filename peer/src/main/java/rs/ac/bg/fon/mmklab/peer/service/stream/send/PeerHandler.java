package rs.ac.bg.fon.mmklab.peer.service.stream.send;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.util.BooksFinder;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.*;

public class PeerHandler extends Service {

    //    instance je objekat koji sadrzi reference svih tokova, soketa, audio fajla, adrese primaoca, udp port primaoca i evideciju o broju procitanih frejmova
    private final PeerHandlerInstance instance;

    private PeerHandler(PeerHandlerInstance instance) {
        this.instance = instance;
    }

    public static PeerHandler createHandler(Socket socket, Configuration configuration) throws IOException {
        return new PeerHandler(PeerHandlerInstance.createHandlerInstance(socket, configuration));
    }

    public PeerHandlerInstance getInstance() {
        return instance;
    }

    //    pomocna metoda kako bi mogao u createTask da prosledim trenutni objekat
    private PeerHandler thisHandler() {
        return this;
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
//                prilikom uspostavljanja konekcije nam primalac salje koju tacno knjigu hoce da slusa
                AudioBook bookForStreaming = establishConnection();
//                System.out.println("Uspesan prijem informacije o knjizi koju slusalac zeli: " + bookForStreaming.toString());

                // pokretanje nove niti koja osluskuje signale od primaoca, koristi iste sokete kao i metoda establish connection
                new SignalHandler(thisHandler()).start();
                initiateSending(bookForStreaming);
                System.out.println("Zavrseno slanje audio zapisa");
                return null;
            }
        };
    }

    public AudioBook establishConnection() {

        System.out.println("establishConnection, nit: " + Thread.currentThread());


        try {
            String req = instance.getFromReceiver().readLine();
            if (req.equals("Available for streaming?")) {
                instance.getToReceiver().println("Yes, which book?");
                String jsonBookInfo = instance.getFromReceiver().readLine();
                instance.setRemotePortUDP(Integer.parseInt(instance.getFromReceiver().readLine().trim()));
//              podesavamo od kog frejma krece citanje, jer ukoliko dodje do premotavanja uspostavlja se nova konekcija
                instance.getToReceiver().println("Send starting frame");
                long startingFrame = Long.parseLong(instance.getFromReceiver().readLine());

                AudioBook bookForStreaming = JsonConverter.toOriginal(jsonBookInfo, AudioBook.class);

//                preskacemo odredjeni broj frejmova
                instance.setFramesSent(startingFrame);
                System.out.println("Preskoceno frejmova: " + startingFrame);
                return bookForStreaming;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Greska (PeerHandler -> establishConnection): problemi sa ulaznim tokom, nisu primljene ocekivane poruke");
        }
        return null;
    }

    private void initiateSending(AudioBook book) throws UnsupportedAudioFileException, IOException {

        /*  postavljamo audio fajl iz koga cemo da ucitavamo deo po deo i da saljemo na mrezu, kao i audioInputStream iz tog fajla */
        instance.setAudioFile(BooksFinder.getBook(book, instance.getConfiguration())); // ovde bi mogla da se odradi provera dal imamo tu knjigu kod nas
        //preko klase AudioSystem se pristupa svim resursima u sistemu, tipa fajlovi, mikrofon itd... takodje mozemo da vidimo koji je format audia
        try {
            instance.setAudioInputStream(AudioSystem.getAudioInputStream(instance.getAudioFile()));// UnsupportedFileException  // IOException
            System.out.println("");
            System.out.println("Stvarno preskoceno bajtova: " + instance.getAudioInputStream().skip(instance.getFramesSent() * instance.getAudioInputStream().getFormat().getFrameSize()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Krecemo sa slanjem; Adresa primaoca: " + instance.getReceiverAddress().toString() + "; port primaoca: " + instance.getRemotePortUDP());


//        ovo je proba da bismo mogli posle da nastavimo slanje
        send();


        // zatvaranje svih soketa i tokova
        closeUDPConnection();
        closeTCPConnection();


        System.out.println("Ceo fajl je procitan, i soket zatvoren");
    }

    public void send() throws UnsupportedAudioFileException, IOException {
        System.out.println("U toku je slanje iz niti: " + Thread.currentThread());
        System.out.println("Hash code hendlera u PeerHandler-u: " + this.hashCode());


        // veličina frejma
        int frameSize = instance.getAudioInputStream().getFormat().getFrameSize();

        //definisanje bafera koje saljemo na mrezu
        byte[] sendBuffer = new byte[1024 * frameSize]; // ovde da se proveri da li je okej da bude 1024 bajta
        byte[] confirmationMessage = new byte[10];
//        paket koji dobijamo od primaoca da bismo znali kad je završio sa obrađivanjem poslednjeg paketa koji smo mu poslali

        /* paketi koje saljemo na mrezu*/
        DatagramPacket confirmationPacket = new DatagramPacket(confirmationMessage, confirmationMessage.length);


        while (true) {  // IOException

            /*Prvo radimo proveru da li nam je stigo neki signal on primaoca*/

            synchronized (this) {
                switch (instance.getSignal()) {
                    case RUNNING:
                        break;
                    case PAUSE: {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case TERMINATE: {
                        instance.getAudioInputStream().close();
                        instance.getSocket().close();
                        instance.getDatagramSocket().close();
                    }
                    return;
                    default:
                        break;
                }
            }


            if ((instance.getAudioInputStream().read(sendBuffer)) == -1) {// kada se dodje do kraja toka vraca -1
                System.out.println("Dosli smo do kraja fajla!");
                return;
            }

//            kreiranje paketa koji saljemo na mrezu
            DatagramPacket dataPackage = new DatagramPacket(sendBuffer, sendBuffer.length, instance.getReceiverAddress(), instance.getRemotePortUDP());

            /* trenutak slanja paketa */
            instance.getDatagramSocket().send(dataPackage);  // IOException
            instance.setFramesSent(instance.getFramesSent() + sendBuffer.length / frameSize); // azuriramo broj poslatih frejmova

//            ovaj potvrdni paket nam omogućava da blokiramo slanje sve dok primalac ne primi paket, i dok ga ne prosledi na mikser
            instance.getDatagramSocket().receive(confirmationPacket); // IOException
        }
    }

    private void closeUDPConnection() {
        instance.getDatagramSocket().close();
    }

    private void closeTCPConnection() {
        try {
            instance.getSocket().close();
            instance.getFromReceiver().close();
            instance.getToReceiver().close();
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("(PeerHandler ---> startSending):  greska pri zatvaranju soketa nakon slanja knjige");
        }
    }

    public void terminate(){
        this.closeUDPConnection();
        this.closeTCPConnection();
        this.cancel();
    }


}
