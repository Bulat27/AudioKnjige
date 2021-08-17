package rs.ac.bg.fon.mmklab.server;

import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.communication.peer_to_server.Request;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class


ClientHandler extends Thread {
    Socket socket;
    PrintStream toPeer;
    BufferedReader fromPeer;
    private InetAddress clientAddress;

    public ClientHandler(Socket socket) throws IOException {

        this.socket = socket;
        toPeer = new PrintStream(socket.getOutputStream());
        fromPeer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        clientAddress = socket.getInetAddress();

    }


    @Override
    public void run() {

        /*ovde treba da ocekujemo od klijenta da nam posalje listu knjiga koje on moze da ponudi, to bismo mogli iz nekog JSON-a da izvucemo te podatke*/

        List<AudioBook> list = null;
        try {
            Request req = Request.valueOf(fromPeer.readLine());
            switch (req) {
                case POST_BOOKS: { // korisnik hoce da postavi knjige
                    String jsonList = fromPeer.readLine();
                    if (JsonConverter.isValidListOfBooks(jsonList)) {
                        System.out.println("Primljeni json sadrzaj se poklapa sa json semom");
                        list = JsonConverter.jsonToBookList(jsonList);
                        Server.updateAvailableBooks(list);
//                        list.forEach(e -> System.out.println(e.toString()));
                    } else System.err.println("Korisnik nema knjiga na raspolaganju u folderu koji je naveo");
                }
                break;
                case GET_BOOKS: {
                    toPeer.println(JsonConverter.toJSON(Server.getAvailableBooks()));
                }
                break;
                case LOG_OUT: {
                    List<AudioBook> forRemoving = null;
                    String jsonList = fromPeer.readLine();
                    System.out.println("Lista knjiga za uklanjanje:   " + jsonList);
                    if (JsonConverter.isValidListOfBooks(jsonList))
                        forRemoving = JsonConverter.jsonToBookList(jsonList);
                    if (forRemoving != null){
                        Server.reduceBookList(forRemoving);
                        System.out.println("Knjige uklonjene nakon odjave klijenta");
                    }
                    else
                        System.err.println("(ClientHandler) korisnik koje ni poslao listu knjiga, lista je null");
                }
                break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        /*nakon toga ide azuriranje liste u klasi rs.ac.bg.fon.mmklab.app.server*/
        /*slanje korisniku liste dostupnih knjiga, isto neki json format |||||  samo na osnovu zahteva!!!*/
        /*za slucaj kad korisnik javi da je zauzet da se sklone iz liste dostupnih knjiga sve one koje taj korisnik nudi*/
        super.run();
    }
}
