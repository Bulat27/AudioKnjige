package rs.ac.bg.fon.mmklab.peer.ui.components.alert;

import javafx.scene.control.Alert;

public class ErrorDialog {
    private Alert alert;

    public ErrorDialog(String title, String message) {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
    }

    public void show(){
        alert.showAndWait();
    }
}
