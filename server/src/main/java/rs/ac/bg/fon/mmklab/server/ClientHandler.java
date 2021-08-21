package rs.ac.bg.fon.mmklab.server;

import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.Request;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class


ClientHandler extends Thread {
    Socket socket;
    PrintStream toPeer;
    BufferedReader fromPeer;
//    private final InetAddress clientAddress;

    public ClientHandler(Socket socket) throws IOException {

        this.socket = socket;
        toPeer = new PrintStream(socket.getOutputStream());
        fromPeer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        clientAddress = socket.getInetAddress();

    }


    @Override
    public void run() {

        while (true) {
            try {
                String reqName = fromPeer.readLine();
                if (reqName == null) return;
                Request req = Request.valueOf(reqName);
                switch (req) {
                    case POST_BOOKS:  // korisnik hoce da postavi knjige
                        appendNewBooks();
                        break;
                    case GET_BOOKS:
                        sendBooksToClient(); //korisnik hoce da mu se posalju knjige
                        break;
                    case LOG_OUT:
                        handleLoggingOut(); // korisnik izasao iz aplikacije
                        closeResources();
                        return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        zatvaranje soketa i tokova
    }

    private void sendBooksToClient() {
        toPeer.println(JsonConverter.toJSON(Server.getAvailableBooks()));
    }

    private void handleLoggingOut() throws IOException {
        List<AudioBook> forRemoving = null;
        String jsonList = fromPeer.readLine();
        if (jsonList == null) return; // ne vidim koji bi drugi nacin bio da se ovo resi
        System.out.println("Lista knjiga za uklanjanje:   " + jsonList);
        if (JsonConverter.isValidListOfBooks(jsonList))
            forRemoving = JsonConverter.jsonToBookList(jsonList);
        if (forRemoving != null) {
            Server.reduceBookList(forRemoving);
            System.out.println("Knjige uklonjene nakon odjave klijenta");
        } else
            System.err.println("(ClientHandler) korisnik koje ni poslao listu knjiga, lista je null");
    }

    private void appendNewBooks() throws IOException {
        String jsonList = fromPeer.readLine();
        if (JsonConverter.isValidListOfBooks(jsonList)) {
            System.out.println("Primljeni json sadrzaj se poklapa sa json semom");
            List<AudioBook> list = JsonConverter.jsonToBookList(jsonList);
            Server.updateAvailableBooks(list);
//                        list.forEach(e -> System.out.println(e.toString()));
        } else System.err.println("Korisnik nema knjiga na raspolaganju u folderu koji je naveo");
    }

    private void closeResources(){
        try {
            socket.close();
            fromPeer.close();
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Nije moguće zatvoriti soket i tokove ka klijentu");
        }
        toPeer.close();
    }
}
