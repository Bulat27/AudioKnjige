package rs.ac.bg.fon.mmklab.peer.ui.components.configure;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.ListExchanger;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.config_service.ConfigurationService;
import rs.ac.bg.fon.mmklab.peer.service.server_communication.ServerCommunicator;
import rs.ac.bg.fon.mmklab.peer.service.stream.send.Sender;
import rs.ac.bg.fon.mmklab.peer.service.util.BooksFinder;
import rs.ac.bg.fon.mmklab.peer.ui.components.request_books.RequestBooksTab;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ConfigurationTab {

    private static Configuration configuration;

    public static void display(TabPane root) {
        Tab configTab = new Tab();
        configTab.setText("Enter configurations");

        Label localPortTCP = new Label("Unesite lokalni broj porta za tcp vezu");
        TextField localPortTCPTxt = new TextField();
        Label localPortUDP = new Label("Unesite lokalni broj porta za tcp vezu");
        TextField localPortUDPTxt = new TextField();
        Label pathToFolder = new Label("Unesite Putanju do fascikle gde se nalaze audio knjige: ");
        TextField pathToFolderTxt = new TextField();
        pathToFolderTxt.setText("/home/lumar26/Public/AudioBooks");

//svaka stavka za unos ce biti poseban horizontal box koji sadrzi po labelu i polje za unos

        VBox labels = new VBox(25);
        labels.getChildren().addAll(localPortTCP, localPortUDP, pathToFolder);
        VBox textFields = new VBox(15);
        textFields.getChildren().addAll(localPortTCPTxt, localPortUDPTxt, pathToFolderTxt);

//        Submit dugme
        Button submitBtn = new Button("Potvrdi");
        submitBtn.setOnAction(a -> {
            configuration = configurationFactory(localPortTCPTxt, localPortUDPTxt, pathToFolderTxt);
            RequestBooksTab.updaTeConfiguration(configuration); // svaki put kad dodje do promene u knfiguraciji ona mora da se apdejtuje
//            onog trenutka kad popunimo konfiguracije svakako cemo da saljemo serveru sve
            sendListOfBooks(configuration);
            clearInputContent(textFields);
//            (new Sender(configuration)).run(); //kad smo definisali konfiguraciju i poslali listu knjiga koju nudimo tada ocekujemo poziv od primaoca
            (new Sender(configuration)).start();
        });


        HBox configLayout = new HBox();
        configLayout.getChildren().addAll(labels, textFields, submitBtn);
        configTab.setContent(configLayout);

//        dodavanje korenom elementu
        root.getTabs().add(configTab);
    }

    private static Configuration configurationFactory(TextField localPortTCPTxt, TextField localPortUDPTxt, TextField pathToFolderTxt) {
        return ConfigurationService.getConfiguration(localPortTCPTxt.getText(), localPortUDPTxt.getText(), pathToFolderTxt.getText());
    }

    private static void sendListOfBooks(Configuration configuration) {
        //            onog trenutka kad popunimo konfiguracije svakako cemo da saljemo serveru sve
        try {
            if (configuration == null) {
//                    korisnik nije lepo uneo konfiguraciju
                System.err.println("Vrati korisnika na prvi tab da lepo unese konfiguraciju i naznaci mu sta je zeznuo");
                return;
            }

            ServerCommunicator communicator = ServerCommunicator.getInstance(InetAddress.getByName(configuration.getServerName()), configuration.getServerPort());

            ListExchanger.sendAvailableBooks(
                    BooksFinder.fetchBooks(configuration),
                    communicator.getStreamToServer(),
                    communicator.getStreamFromServer());
        } catch (IOException e) {
//                e.printStackTrace();
            System.err.println("Greska(): ili je nepostojeca adresa servera prosledjena, ili los port, ili ");
        }
    }

    private static void clearInputContent(VBox textFields) {
        textFields.getChildren().forEach(field -> ((TextField) field).setText("")); // ovo je mozda rizicno ali ovde znamo da su tu sigurno samo ta polja za unos teksta
    }


}