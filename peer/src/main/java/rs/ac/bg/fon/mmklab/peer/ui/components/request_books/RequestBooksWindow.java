package rs.ac.bg.fon.mmklab.peer.ui.components.request_books;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.ListExchanger;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.server_communication.ServerCommunicator;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.Receiver;
import rs.ac.bg.fon.mmklab.peer.ui.components.audio_player.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class RequestBooksWindow {
    private static Configuration configuration;

    public static void updateConfiguration(Configuration newConfiguration) {
        configuration = newConfiguration;
    }

    public static BorderPane display() {
        BorderPane windowContent = new BorderPane();
        Button sendRequestBtn = new Button("Get available books");
        sendRequestBtn.setStyle("-fx-background-color: linear-gradient(lightgrey, gray ); -fx-text-fill:BLACK;-fx-font-weight: BOLD ");
        DropShadow shadow = new DropShadow();
        sendRequestBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> sendRequestBtn.setEffect(shadow));
        sendRequestBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> sendRequestBtn.setEffect(null));

        ScrollPane scrollPane = new ScrollPane();
        VBox availableBooks = new VBox(10);
        availableBooks.setPadding(new Insets(20, 50, 20, 50));

        windowContent.setMargin(sendRequestBtn, new Insets(12, 25, 12, 25));
        windowContent.setMargin(scrollPane, new Insets(12, 25, 12, 25));

        sendRequestBtn.setOnAction(action -> showAvailableBooks(availableBooks));

        scrollPane.setContent(availableBooks);

        windowContent.setTop(sendRequestBtn);
        BorderPane.setAlignment(sendRequestBtn, Pos.CENTER);
        windowContent.setCenter(scrollPane);
        BorderPane.setAlignment(availableBooks, Pos.CENTER);
        windowContent.setPadding(new Insets(30, 0, 0, 0));

        return windowContent;


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
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Setver nedostupan");
            alert.setContentText("Server trenutno nije dostupan,\nmolimo pokušajte kasnije");
            alert.showAndWait();
            return;
        }
        if (list != null) {
            availableBooks.getChildren().clear(); // removeAll(); mora da ima argumenta, a to nam ne odgovara
            list.forEach(book -> {
                Button bookBtn = new Button(book.getBookInfo().getAuthor() + " - " + book.getBookInfo().getTitle());
                bookBtn.setStyle("-fx-background-color: #8abec6; -fx-text-fill:BLACK; ");
                style(bookBtn);
                availableBooks.getChildren().add(bookBtn);
                bookBtn.setPrefWidth(400);
                bookBtn.setOnAction(e -> {
                    try {
                        Receiver receiver = Receiver.createInstance(book, configuration);
                        receiver.start();
                        AudioPlayer.setReceiver(receiver);

//                    kad kliknemo na knjigu koju zelimo da slusamo automatski nam se otvara audio plejer koji blokira ovaj tab
                        AudioPlayer.display();
                    }  catch (LineUnavailableException lineUnavailableException) {
//                        lineUnavailableException.printStackTrace();
                        System.err.println("(booksBtn.setOnAction): prilikom kreiranja ReceiverInstance nije se mogla otvoriti audio linija iz fajla");
                    } catch (IOException ioException) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Pošiljalac nedostupan");
                        alert.setContentText("Pošiljalac audio zapisa više nije \ndostupan, molimo pokušajte \nsa osveženom listom knjiga");
                        alert.showAndWait();

                        showAvailableBooks(availableBooks);
                    }
                });
            });
        } else
            System.err.println("Greska (RequestBooksTab -> showAvailableBooks): Lista nije popunjena, ostala je null");
    }

    public static void style(Button bookBtn) {
        DropShadow shadow = new DropShadow();

        bookBtn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> bookBtn.setEffect(shadow));

        bookBtn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> bookBtn.setEffect(null));
    }
}
