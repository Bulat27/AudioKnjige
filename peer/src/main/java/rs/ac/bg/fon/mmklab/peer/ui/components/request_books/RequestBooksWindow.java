package rs.ac.bg.fon.mmklab.peer.ui.components.request_books;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.ListExchanger;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.server_communication.ServerCommunicator;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.Receiver;
import rs.ac.bg.fon.mmklab.peer.ui.components.audio_player.AudioPlayer;
//import rs.ac.bg.fon.mmklab.peer.ui.components.design.TabDesign;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class RequestBooksWindow {
    private static Configuration configuration;

    public static void updateConfiguration(Configuration newConfiguration) {
        configuration = newConfiguration;
    }

    public static void display() {
        BorderPane windowContent = new BorderPane();
        Button sendRequestBtn = new Button("Get available books");
        ScrollPane scrollPane = new ScrollPane();
        VBox availableBooks = new VBox(5);

        sendRequestBtn.setOnAction(action -> showAvailableBooks(availableBooks));

        scrollPane.setContent(availableBooks);

        windowContent.setTop(sendRequestBtn);
        BorderPane.setAlignment(sendRequestBtn, Pos.CENTER);
        windowContent.setCenter(scrollPane);
        windowContent.setPadding(new Insets(30, 0, 0, 0));

        Scene scene = new Scene(windowContent, 600, 300);
        Stage primaryStage = new Stage();
        primaryStage.setHeight(550);
        primaryStage.setWidth(600);
        primaryStage.setScene(scene);
        primaryStage.showAndWait();


//        stilizacija
        BorderPane.setMargin(sendRequestBtn, new Insets(12,12,12,12));
        sendRequestBtn.setStyle("-fx-background-color: linear-gradient(lightgrey, gray ); -fx-text-fill:BLACK;-fx-font-weight: BOLD ");
        DropShadow shadow = new DropShadow();
        sendRequestBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> sendRequestBtn.setEffect(shadow));
        sendRequestBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> sendRequestBtn.setEffect(null));

        BorderPane.setMargin(scrollPane, new Insets(12,12,12,12));
    }

    private static void showAvailableBooks(VBox availableBooks) {
        List<AudioBook> list = null;
        if (configuration == null) {
            System.err.println("Korisnik jos uvek nije odradio nikakvu konfiguraciju pa je nemoguce povuci listu knjiga sa servera");
            return;
        }
        try {
            ServerCommunicator communicator = ServerCommunicator
                    .getInstance(InetAddress.getByName(configuration.getServerName()), configuration.getServerPort());
            list = ListExchanger.getAvailableBooks(communicator.getStreamFromServer(), communicator.getStreamToServer());
            System.out.println("\n>>>> Dobili smo listu knjiga <<<<<<<<");
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Greska: nepoznat server");
        }
        if (list != null) {
            availableBooks.getChildren().clear(); // removeAll(); mora da ima argumenta, a to nam ne odgovara
            System.out.println();
            System.out.println(">>> Lista nije null <<<");
            list.forEach(book -> {
                Button bookBtn = new Button(book.getBookInfo().getAuthor() + " - " + book.getBookInfo().getTitle());
                bookBtn.setStyle("-fx-background-color: #8abec6; -fx-text-fill:BLACK; ");
                style(bookBtn);
                availableBooks.getChildren().add(bookBtn);
                bookBtn.setPrefWidth(500);
                bookBtn.setOnAction(e -> {
                    try {
                        Receiver receiver = Receiver.createInstance(book, configuration);
                        receiver.start();
                        AudioPlayer.setReceiver(receiver);

//                    kad kliknemo na knjigu koju zelimo da slusamo automatski nam se otvara audio plejer koji blokira ovaj tab
                        AudioPlayer.display();
                    } catch (IOException ioException) {
//                        ioException.printStackTrace();
                        System.err.println("Greska (RequestBookSTab -> showAvailableBooks): pri pokretanju Receiver niti je doslo do greske, korisnik od kog se zahtevaju knjige nije online");

                    } catch (LineUnavailableException lineUnavailableException) {
//                        lineUnavailableException.printStackTrace();
                        System.err.println("(booksBtn.setOnAction): prilikom kreiranja ReceiverInstance nije se mogla otvoriti audio linija iz fajla");
                    }
                });
            });
        } else
            System.err.println("Greska (RequestBooksTab -> showAvailableBooks): Lista nije popunjena, ostala je null");
    }

    public static void style(Button bookBtn) {
        DropShadow shadow = new DropShadow();
        bookBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                bookBtn.setEffect(shadow);
            }
        });

        bookBtn.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                bookBtn.setEffect(null);
            }
        });
    }
}
