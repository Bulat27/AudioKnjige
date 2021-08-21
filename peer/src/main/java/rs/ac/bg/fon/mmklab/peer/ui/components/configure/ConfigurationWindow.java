package rs.ac.bg.fon.mmklab.peer.ui.components.configure;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.ListExchanger;
import rs.ac.bg.fon.mmklab.exception.InvalidBooksFolderException;
import rs.ac.bg.fon.mmklab.exception.InvalidConfigurationException;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.config_service.ConfigurationService;
import rs.ac.bg.fon.mmklab.peer.service.server_communication.ServerCommunicator;
import rs.ac.bg.fon.mmklab.peer.service.stream.send.Sender;
import rs.ac.bg.fon.mmklab.peer.service.util.BooksFinder;
import rs.ac.bg.fon.mmklab.peer.ui.components.alert.ErrorDialog;
import rs.ac.bg.fon.mmklab.peer.ui.components.request_books.RequestBooksWindow;

import java.io.IOException;
import java.net.InetAddress;

public class ConfigurationWindow extends Stage {

    private static Configuration configuration;

    public static Configuration getConfiguration() {
        return configuration;
    }

    public boolean isAllNumber(String str) {
        char ch;
        boolean usp = true;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (Character.isDigit(ch)) {
                usp = true;
            } else return false;

            return usp;
        }
        return usp;
    }

    public static void display() {
        Stage window = new Stage();

        BorderPane windowContent = new BorderPane();
        windowContent.setPadding(new Insets(10, 50, 50, 50));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label localPortTCP = new Label("Unesite lokalni broj porta za tcp vezu:");
        TextField localPortTCPTxt = new TextField();
        Label localPortUDP = new Label("Unesite lokalni broj porta za udp vezu:");
        TextField localPortUDPTxt = new TextField();
        Label pathToFolder = new Label("Unesite putanju do fascikle gde se nalaze audio knjige: ");
        final TextField pathToFolderTxt = new TextField();
        pathToFolderTxt.setText("/home/lumar26/Public/AudioBooks");
        Button submitBtn = new Button("Potvrdi");

        //design labela i txtbox
        pathToFolder.setStyle("-fx-font-family:'Courier New'; -fx-font-size: 12px;-fx-font-weight: BOLD; ");
        localPortTCP.setStyle("-fx-font-family:'Courier New'; -fx-font-size: 12px;-fx-font-weight: BOLD; ");
        localPortUDP.setStyle("-fx-font-family:'Courier New'; -fx-font-size: 12px;-fx-font-weight: BOLD; ");
        pathToFolderTxt.setStyle("-fx-font-family:'Courier New'; -fx-font-size: 11px;");
        localPortTCPTxt.setStyle("-fx-font-family:'Courier New'; -fx-font-size: 11px;");
        localPortUDPTxt.setStyle("-fx-font-family:'Courier New'; -fx-font-size: 11px;");

        //Dodavanje komponenta na grid pane
        gridPane.add(localPortTCP, 2, 0);
        gridPane.add(localPortTCPTxt, 2, 1);
        gridPane.add(localPortUDP, 2, 2);
        gridPane.add(localPortUDPTxt, 2, 3);
        gridPane.add(pathToFolder, 2, 4);
        gridPane.add(pathToFolderTxt, 2, 5);
        gridPane.add(submitBtn, 2, 7);

        //Dodavanje naslova
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        Text naslov = new Text("AUDIO BOOKS");
        naslov.setFont(Font.font("Courier New", FontWeight.BOLD, 33));
        naslov.setEffect(dropShadow);

        //Stilizacija dugmeta i dodavanje funkcionalnosti
        submitBtn.setStyle("-fx-background-color: linear-gradient(lightgrey, gray ); -fx-text-fill:BLACK;-fx-font-weight: BOLD ");
        RequestBooksWindow.style(submitBtn);

        submitBtn.setOnAction(submit -> {

            String tcp = localPortTCPTxt.getText();
            String udp = localPortUDPTxt.getText();
            String path = pathToFolderTxt.getText();
            ConfigurationWindow pom = new ConfigurationWindow();
            if (tcp.trim().equals("") || !pom.isAllNumber(tcp) ||
                    udp.trim().equals("") || !pom.isAllNumber(udp) ||
                    path.trim().equals("")) {

                new ErrorDialog("Neispravan unos!", "Proverite da li su sva polja ispravno uneta.").show();

            } else {
                configuration = ConfigurationService.getConfiguration(localPortTCPTxt.getText(), localPortUDPTxt.getText(), pathToFolderTxt.getText());
                RequestBooksWindow.updateConfiguration(configuration); // svaki put kad dodje do promene u knfiguraciji ona mora da se apdejtuje
//                  onog trenutka kad popunimo konfiguracije svakako cemo da saljemo serveru sve
                //ako slanje nije uspesno jer nije lepo uneta oknfiguracija ne nastavljamo dalje vec ostavljamo korisnika da pravilno unese
                try {
                    if (!ServerCommunicator.getInstance(configuration).sendListOfBooks())
                        return;
                } catch (IOException e) {
                    e.printStackTrace();
                    new ErrorDialog("Neispravan unos!", "Proverite da li su sva polja ispravno uneta.").show();

                }

//                  kad smo definisali konfiguraciju i poslali listu knjiga koju nudimo tada ocekujemo poziv od primaoca
                (new Sender(configuration)).start();

//                kad zavrsimo sa unosom menjamo sadrzaj prozora u prikaz liste dostupnih knjiga
                windowContent.setCenter(RequestBooksWindow.display());
            }

        });


        //final
        Scene scene = new Scene(windowContent, 660, 600);
        window.setScene(scene);
        window.setTitle("Konfiguracija");
        windowContent.setTop(naslov);
        BorderPane.setAlignment(naslov, Pos.CENTER);
        windowContent.setCenter(gridPane);
        window.show();

    }
}




