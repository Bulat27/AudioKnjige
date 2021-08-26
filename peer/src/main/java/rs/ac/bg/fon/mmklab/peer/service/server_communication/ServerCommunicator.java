package rs.ac.bg.fon.mmklab.peer.service.server_communication;


import rs.ac.bg.fon.mmklab.communication.peer_to_server.ListExchanger;
import rs.ac.bg.fon.mmklab.exception.InvalidBooksFolderException;
import rs.ac.bg.fon.mmklab.exception.InvalidConfigurationException;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.util.BooksFinder;
import rs.ac.bg.fon.mmklab.peer.ui.components.alert.ErrorDialog;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class ServerCommunicator {

    private static ServerCommunicator instance;

    private final Socket communicationSocket;
    private final PrintStream streamToServer;
    private final BufferedReader streamFromServer;
    private final Configuration configuration;

    //  ----------------------------------------------------------------------------------------------------------------
//  posto za jednog peer-a hocemo da postoji samo jedan jedini ServerCommunicator iskoristicemo Singleton pattern
//    da bismo obezbedili da se radi sa jednom istom instancom
    private ServerCommunicator(Configuration configuration) throws IOException {
        this.configuration = configuration;

        this.communicationSocket = new Socket(InetAddress.getByName(configuration.getServerName()), configuration.getServerPort()); // u ovom trenutku je odradjena accept metoda na serverskoj strani i pokrenuo se hendler, ako ne baci izuzetak
        this.streamToServer = new PrintStream(communicationSocket.getOutputStream());
        this.streamFromServer = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
    }

    public static ServerCommunicator getInstance(Configuration configuration) throws IOException {
        if (instance == null)
            instance = new ServerCommunicator(configuration);
        return instance;
    }

    public PrintStream getStreamToServer() {
        return streamToServer;
    }

    public BufferedReader getStreamFromServer() {
        return streamFromServer;
    }

    public boolean sendListOfBooks() {
        //            onog trenutka kad popunimo konfiguracije svakako cemo da saljemo serveru sve
        try {
            if (configuration == null) {
//                    korisnik nije lepo uneo konfiguraciju
                throw new InvalidConfigurationException("Loš unos konfiguracije");
            }
            ListExchanger.sendAvailableBooks(
                    BooksFinder.fetchBooks(configuration),
                    instance.getStreamToServer());
        } catch (IOException e) {
//                e.printStackTrace();
            System.err.println("Greska(): ili je nepostojeca adresa servera prosledjena, ili los port, ili ");
            return false;
        } catch (InvalidBooksFolderException e) {
            new ErrorDialog("Loša putanja", "Unesite validnu putanju \nka folderu sa audio knjigama.").show();
            return false;
        } catch (InvalidConfigurationException e) {
            new ErrorDialog("Loša konfiguracija", "Unesite validnu konfiguraciju\nproverite sva polja za unos").show();
            return false;
        }
        return true;
    }
}
