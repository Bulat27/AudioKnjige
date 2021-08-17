package rs.ac.bg.fon.mmklab.peer.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.ListExchanger;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.Request;
import rs.ac.bg.fon.mmklab.peer.service.server_communication.ServerCommunicator;
import rs.ac.bg.fon.mmklab.peer.service.util.BooksFinder;
import rs.ac.bg.fon.mmklab.peer.ui.components.audio_player.AudioPlayer;
import rs.ac.bg.fon.mmklab.peer.ui.components.configure.ConfigurationTab;
import rs.ac.bg.fon.mmklab.peer.ui.components.request_books.RequestBooksTab;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import java.net.InetAddress;
import java.util.List;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {


        BorderPane root = ConfigurationTab.display();
        primaryStage.setTitle("Audio Books");
        primaryStage.setWidth(600);
        primaryStage.setHeight(550);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
//        Ovde cemo da implementiramo komunikaciju sa najpre peer-om ukoliko je prestanak rada aplikacije nasilan, tj pre završetka strimovanja
//        Nakon toga komunikacija sa serverom u smislu odjavljivanja sa servera

        Configuration configuration = ConfigurationTab.getConfiguration();
        ServerCommunicator communicator = ServerCommunicator.getInstance(InetAddress.getByName(configuration.getServerName()), configuration.getServerPort());
        communicator.getStreamToServer().println(Request.LOG_OUT); // obavestenje da cemo da se izlogujemo
        List<AudioBook> list = BooksFinder.fetchBooks(configuration);
        communicator.getStreamToServer().println(JsonConverter.toJSON(list)); // slanje knjiga koje treba ukloniti sa liste
    }
}
