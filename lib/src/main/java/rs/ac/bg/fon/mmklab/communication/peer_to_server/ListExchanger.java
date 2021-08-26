package rs.ac.bg.fon.mmklab.communication.peer_to_server;

import rs.ac.bg.fon.mmklab.book.AudioBook;
import rs.ac.bg.fon.mmklab.util.JsonConverter;

import java.io.*;
import java.util.List;

public class ListExchanger {

    //    metoda kojom pri unosu i potvrdi konfiguracije u nasoj aplikaciji saljemo serveru listu knjiga koje mi nudimo
    public static void sendAvailableBooks(List<AudioBook> listOfBooks, PrintStream streamToServer) throws IOException {

        System.out.println("Lista knjiga koja se salje serveru ima sledeci oblik: " + JsonConverter.toJSON(listOfBooks));

        streamToServer.println(Request.POST_BOOKS);

        streamToServer.println(JsonConverter.toJSON(listOfBooks));

    }

    //  metoda kojom od servera dovlacimo listu knjiga koje nude trenutno drugi peer-ovi
    public static List<AudioBook> getAvailableBooks(BufferedReader fromServer, PrintStream toServer) {
        List<AudioBook> result = null;
        toServer.println(Request.GET_BOOKS);
        try {
            String jsonList = fromServer.readLine();
            result = JsonConverter.jsonToBookList(jsonList);
            System.out.println(" Knjige su pristigle nakon zahteva koji smo poslali serveru ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
