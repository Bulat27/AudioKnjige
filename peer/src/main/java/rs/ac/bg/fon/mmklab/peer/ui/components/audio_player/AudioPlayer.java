package rs.ac.bg.fon.mmklab.peer.ui.components.audio_player;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.ReceiverInstance;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.Receiver;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.Signaler;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

public class AudioPlayer extends Stage{
    private static Receiver receiver;

    public static void setReceiver(Receiver r) {
        receiver = r;
    }

    private static Slider timeSlider;

    public static void display() {

        Stage primaryStage = new Stage();
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("Audio Player");

        HBox buttonBar = new HBox();
        HBox mediaBar = new HBox(10);
        mediaBar.setAlignment(Pos.CENTER);
        buttonBar.setAlignment(Pos.CENTER);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(buttonBar, mediaBar);
        vbox.setPadding(new Insets(15, 10, 10, 10));
        vbox.setSpacing(12);

        final Button playButton = new Button(" >> ");
        final Button pauseButton = new Button("  ||  ");

        Label space = new Label("    ");


        buttonBar.getChildren().addAll(playButton, space, pauseButton);

        Label timeLabel = new Label("Time: ");

        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(40);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.setMin(0);
        timeSlider.setMax(receiver.getInstance().getAudioBook().getAudioDescription().getLengthInFrames()); // koliko audio zapis ima frejmova tolika je maksimalna vrednost slajdera

        final Button forwardBtn = new Button(" >| ");
        final Button backwardBtn = new Button(" |< ");
        mediaBar.getChildren().addAll(timeLabel, timeSlider, backwardBtn, forwardBtn);


//        ponasanje: pokretanje niti koja ce poslati signal pošiljaocu da li da pauzira, nastavi ili prekine slanje audio zapisa
        playButton.setOnAction(click -> (new Signaler(Signal.RESUME, receiver)).start());

        pauseButton.setOnAction(click -> (new Signaler(Signal.PAUSE, receiver)).start());

        forwardBtn.setOnAction(forward -> restartReceiver(receiver, (receiver.getInstance().getFramesRead() + timeSlider.getMax() / 20)));

        backwardBtn.setOnAction(backward -> restartReceiver(receiver, (receiver.getInstance().getFramesRead() - timeSlider.getMax() / 20)));

        primaryStage.setOnCloseRequest(windowEvent -> {
            (new Signaler(Signal.TERMINATE, receiver)).start();

//            zatvaranje soketa je neophodno u slucaju da je posiljalac iznenadno postao nedostupan pa razmena signala nije bila uspesna
            receiver.closeUDPConnection();
            receiver.closeTCPConnection();
        });

        timeSlider.setOnMouseReleased(release ->{
            restartReceiver(receiver, timeSlider.getValue());
        });





//        stilizacija

        vbox.setStyle("-fx-background-color:#76a3aa; -fx-font-family:'Times New Roman'; ");
        playButton.setStyle("-fx-background-color:#76a3aa; -fx-text-fill: Black;-fx-font-weight: BOLD; -fx-font-size: 13; -fx-border-style: solid; -fx-border-radius: 25; ");
        pauseButton.setStyle("-fx-background-color:#76a3aa;-fx-text-fill: Black;-fx-font-weight: BOLD; -fx-font-size: 13; -fx-border-style: solid; -fx-border-radius: 25;");


//        postavljanje scene
        Scene scene = new Scene(vbox, 450, 110);
//        iz nekog razloga ne radi sa strelicama na tastaturi
//        scene.setOnKeyPressed(event -> {
//            switch (event.getCode()) {
//                case LEFT:  restartReceiver(receiver, (receiver.getInstance().getFramesRead() - timeSlider.getMax() / 20)); break;
//                case RIGHT:  restartReceiver(receiver, (receiver.getInstance().getFramesRead() + timeSlider.getMax() / 20)); break;
//            }
//        });
        primaryStage.setScene(scene);
        primaryStage.showAndWait();
    }

    private static void restartReceiver(Receiver receiver, double value) {
        ReceiverInstance receiverInstance = receiver.getInstance();
        //        parametri potrebni za pokretanje novog receivera
        AudioBook book = receiverInstance.getAudioBook();
        Configuration config = receiverInstance.getConfiguration();

//      kad zatvorimo konekciju ubijamo dosadasnjeg receivera da bismo pokrenuli novog
        receiverInstance.getSourceLine().stop();
        receiver.closeUDPConnection();
        receiver.closeTCPConnection();
        receiver.cancel();


//        instanciranje novog receivera
        try {
            receiver = Receiver.createInstance(book, config);
            receiver.getInstance().setFramesRead((long) value);
            receiver.start();
            AudioPlayer.setReceiver(receiver);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nije moguce pokrenuti novog receivera, verovatno zbog zauzetog porta");
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.err.println("Nije moguce pokrenuti novog receivera jer je zauzeta sourceDataLine");
        }
    }

    public static void updateTimeSlider(ReceiverInstance instance){
        timeSlider.setValue(instance.getFramesRead());
    }
}
