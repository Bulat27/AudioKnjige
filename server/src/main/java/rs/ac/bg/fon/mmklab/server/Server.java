package rs.ac.bg.fon.mmklab.server;

import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.properties.PropertiesCache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    /*Server od polja treba da ima listu dostupnih knjiga*/
    private static final List<AudioBook> availableBooks = new ArrayList<>();
    /*treba nam lista aktivnih niti koje hendluju klijente*/
    private static final List<ClientHandler> handlers = new ArrayList<>();

    public static void main(String[] args) {
        ServerSocket receiveSocket;
        Socket communicationSocket;

        try {
            receiveSocket = new ServerSocket(Integer.parseInt(PropertiesCache.getInstance().getProperty("server_port")));
            System.out.println("Server započeo sa radom, ime servara: " + InetAddress.getLocalHost().getHostName());

            while (true) {

                communicationSocket = receiveSocket.accept();
                ClientHandler handler = new ClientHandler(communicationSocket);  /// pokretanje novog hendlera
                handlers.add(handler);
                handler.start();
                System.out.println("Uspostavljena konekcija, broj konekcija: " + handlers.size());

            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not open receive socket on given port");
        }
    }

    public static void updateAvailableBooks(List<AudioBook> newBooks) {
        newBooks.forEach(book -> {
            if(!availableBooks.contains(book))
                availableBooks.add(book);
        });
    }

    public static void reduceBookList(List<AudioBook> forRemoving){
        forRemoving.forEach(availableBooks::remove);
    }

    public static List<AudioBook> getAvailableBooks() {
        return availableBooks;
    }
}
