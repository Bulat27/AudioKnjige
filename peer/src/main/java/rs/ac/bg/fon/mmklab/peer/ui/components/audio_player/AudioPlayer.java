package rs.ac.bg.fon.mmklab.peer.ui.components.audio_player;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.ReceiverInstance;
import rs.ac.bg.fon.mmklab.peer.service.stream.signal.Signal;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.Receiver;
import rs.ac.bg.fon.mmklab.peer.service.stream.receive.Signaler;

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
        HBox mediaBar = new HBox();
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
        mediaBar.getChildren().add(timeLabel);

        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(40);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.setMin(0);
        timeSlider.setMax(receiver.getInstance().getAudioBook().getAudioDescription().getLengthInFrames()); // koliko audio zapis ima frejmova tolika je maksimalna vrednost slajdera
        mediaBar.getChildren().add(timeSlider);


//        ponasanje: pokretanje niti koja ce poslati signal pošiljaocu da li da pauzira, nastavi ili prekine slanje audio zapisa
        playButton.setOnAction(click -> (new Signaler(Signal.RESUME, receiver)).start());

        pauseButton.setOnAction(click -> (new Signaler(Signal.PAUSE, receiver)).start());

        primaryStage.setOnCloseRequest(windowEvent -> {
            (new Signaler(Signal.TERMINATE, receiver)).start();

//            zatvaranje soketa je neophodno u slucaju da je posiljalac iznenadno postao nedostupan pa razmena signala nije bila uspesna
            receiver.closeUDPConnection();
            try {
                receiver.closeTCPConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });




//        stilizacija

        vbox.setStyle("-fx-background-color:#76a3aa; -fx-font-family:'Times New Roman'; ");
        playButton.setStyle("-fx-background-color:#76a3aa; -fx-text-fill: Black;-fx-font-weight: BOLD; -fx-font-size: 13; -fx-border-style: solid; -fx-border-radius: 25; ");
        pauseButton.setStyle("-fx-background-color:#76a3aa;-fx-text-fill: Black;-fx-font-weight: BOLD; -fx-font-size: 13; -fx-border-style: solid; -fx-border-radius: 25;");


//        postavljanje scene
        Scene scene = new Scene(vbox, 450, 110);
        primaryStage.setScene(scene);
        primaryStage.showAndWait();
    }

    public static void updateTimeSlider(ReceiverInstance instance){
        timeSlider.setValue(instance.getFramesRead());
    }
}
