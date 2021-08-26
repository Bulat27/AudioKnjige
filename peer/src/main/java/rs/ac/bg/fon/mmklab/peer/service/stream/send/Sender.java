package rs.ac.bg.fon.mmklab.peer.service.stream.send;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Sender extends Service<Configuration> {
    private ServerSocket receiveSocket;
    private Socket communicationSocket;

    private final Configuration configuration;

    public Sender(Configuration configuration) {
        this.configuration = configuration;
    }



    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    receiveSocket = new ServerSocket(configuration.getLocalPortTCP());
                    System.out.println("Sender: STARTED");

                    while (true) {
                        communicationSocket = receiveSocket.accept();
                        PeerHandler handler = PeerHandler.createHandler(communicationSocket, configuration);  /// treba ovde try/catch
                        handler.start();
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                    System.err.println("Greska: Nije moguce pokrenuti PeerHandler pri zahtevu za konekcijom");
                }
                return null;
            }
        };
    }
}