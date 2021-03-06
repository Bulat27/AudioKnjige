package rs.ac.bg.fon.mmklab.peer.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.Request;
import rs.ac.bg.fon.mmklab.exception.InvalidBooksFolderException;
import rs.ac.bg.fon.mmklab.peer.service.server_communication.ServerCommunicator;
import rs.ac.bg.fon.mmklab.peer.service.util.BooksFinder;
import rs.ac.bg.fon.mmklab.peer.ui.components.configure.ConfigurationWindow;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ConfigurationWindow.display(); //prvo prikazujemo unos podataka za konfiguraciju pa se nakon toga nadovezuju ostali delovi aplikacije
    }

    @Override
    public void stop() {
//        Ovde cemo da implementiramo komunikaciju sa najpre peer-om ukoliko je prestanak rada aplikacije nasilan, tj pre završetka strimovanja
//        Nakon toga komunikacija sa serverom u smislu odjavljivanja sa servera

        try {
            Configuration configuration = ConfigurationWindow.getConfiguration();
            ServerCommunicator communicator = ServerCommunicator.getInstance(configuration);
            communicator.getStreamToServer().println(Request.LOG_OUT); // obavestenje da cemo da se izlogujemo
            List<AudioBook> list = BooksFinder.fetchBooks(configuration);
            communicator.getStreamToServer().println(JsonConverter.toJSON(list)); // slanje knjiga koje treba ukloniti sa liste
        } catch (IOException e) {
            System.out.println("Zatvaranje aplikacije zbog nedostupnosti servera");
        } catch (RuntimeException re) {
            System.out.println("Korisnik zatvorio aplikaciju pre potvrde konfiguracije");
        } catch (InvalidBooksFolderException e) {
        }
    }
}
