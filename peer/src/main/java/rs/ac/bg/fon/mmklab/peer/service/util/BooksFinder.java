package rs.ac.bg.fon.mmklab.peer.service.util;

import rs.ac.bg.fon.mmklab.book.*;
import rs.ac.bg.fon.mmklab.exception.InvalidBooksFolderException;
import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.peer.ui.components.alert.ErrorDialog;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

public final class BooksFinder extends SimpleFileVisitor<Path> {

    private BooksFinder() {
    }

    public static List<AudioBook> fetchBooks(Configuration configuration) throws InvalidBooksFolderException {
        String booksFolder = configuration.getPathToBookFolder();
        String audioFormatExtension = configuration.getAudioExtension();
        InetSocketAddress localSocket = null;
        try {
            localSocket = new InetSocketAddress(InetAddress.getLocalHost(), configuration.getLocalPortTCP());
        } catch (UnknownHostException e) {
//            e.printStackTrace();
            System.err.println("Greska (BooksFinder -> fetchBooks): ne moze da nadje localhost adresu");
        }
        //TODO: Potrebno je bolje obraditi gresku ovde, ali za sada je samo poboljsana validacija

        if (!FileValidator.isValid(booksFolder)) {
//            ovde isto da se napravi neki exceptoin
            throw new InvalidBooksFolderException("Prosleđena putanja ka folderu sa audio knjigama nije validna");
        }

        Path pathToBooksFolder = Paths.get(booksFolder);

        //TODO: Proveriti da nisam ovde nesto pokvario. Samo sam sacuvao referencu ka streamu i stavio je u try-with-resources
        try (Stream<Path> pathStream = Files.walk(pathToBooksFolder, FOLLOW_LINKS)) {
//            trebalo bi da moze ovako odmah da se strim path-ova mapira u strim fajlova pa da se to sve ubaci u listu. Files.walk vraca stream
            List<File> filesInDirectory = pathStream.
                    map(path -> new File(path.toString()))
                    .collect(Collectors.toList());
            System.out.println("Broj knjiga u prosledjenom folderu: " + (filesInDirectory.size() - 1));

//            izbacujemo sve fajlove koji nisu u zadatom formatu, pretpostavka je da ce svi fajlovi bit u istom formatu: .wav
            List<File> booksInDirectory = filesInDirectory.stream().
                    filter(file -> file.getPath().endsWith(audioFormatExtension))
                    .collect(Collectors.toList());

            if (filesInDirectory.size() == 0) {
//                nema audio knjiga u direktorijumu
                new ErrorDialog("Bez knjiga u ponudi", "Navedeni folder nema ni jednu audio knjigu").show();
                return null;
            }

            InetSocketAddress finalLocalSocket = localSocket; // ovo je moralo zbog lambda izraza nesto, ne znam zbog cega
//            za svaku putanju napravimo po jedan objekat AudioBook i vratimo List<AudioBook>
            return booksInDirectory.stream().
                    map(bookFile -> new AudioBook(getAudioDescription(bookFile), getBookInfo(bookFile, audioFormatExtension), getBookOwner(finalLocalSocket)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Generalna greska (fetchBooks):  Nije prosao glavni try blok ");
//            e.printStackTrace();
        }
        return null;
    }

    private static BookInfo getBookInfo(File bookFile, String fileExtension) {
        Path filePath = Paths.get(bookFile.toString());
        String fileName = filePath.getFileName().toString();
        String bookName, bookAuthor;

//        odbacujemo deo koji oznacava ekstenziju
        fileName = fileName.substring(0, fileName.length() - fileExtension.length());
        String[] infos = fileName.split("-");
        bookName = infos[1].replace('_', ' ');
        bookAuthor = infos[0].replace('_', ' ');
        return new BookInfo(bookName, bookAuthor);
    }

    private static BookOwner getBookOwner(InetSocketAddress localSocket) {
        if (localSocket != null)
            return new BookOwner(localSocket.getAddress(), localSocket.getPort());
        else
//            ovo takodje mora da se resi sa nekim custom izuzetkom
            System.err.println("Greska (getBookOwner): Lokalni soket njije lepo prosledjen");
        return null;
    }

    private static AudioDescription getAudioDescription(File bookfile) {
//        odradi ovde neku drugaciju inicijalizaciju mislim da ovo nije dobro ovako...
        AudioInputStream audioInputStream;
        AudioFormat audioFormat = null;
        long lengthInFrames = 0;
        int frameSizeInBytes = 0;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(bookfile);
            audioFormat = audioInputStream.getFormat();
            lengthInFrames = audioInputStream.getFrameLength();
            frameSizeInBytes = audioFormat.getFrameSize();
        } catch (UnsupportedAudioFileException e) {
//            ako u getAudioInputStream prosledimo neodgovarajuci format fajla
            System.err.println("Greska (getAudioDescription): prosledjen je fajl koji nije audio");
//            e.printStackTrace();
        } catch (IOException e) {
//            za getAudioInputStream
            System.err.println("Greska (getAudioDescription): getAudioInputStream ne moze da povuce strim iz datog fajla");
//            e.printStackTrace();
        }
//zbog nemoguce serijalizacije objekta tipa AudioFormat moramo da prebacujemo u custom-made klasu za cuvanje audio formata
        assert audioFormat != null;
        return new AudioDescription(CustomAudioFormat.toCustom(audioFormat), lengthInFrames, frameSizeInBytes);
    }

    public static File getBook(AudioBook book, Configuration configuration) {

        Path pathToBooksFolder = Paths.get(configuration.getPathToBookFolder());

        if (Files.notExists(pathToBooksFolder)) {
//            ovde isto da se napravi neki exception
            new ErrorDialog("Nepostojeca putanja", "Putanja ka folderu sa knjigama nije pravilno unešena").show();
            return null;
        }

        String absolutePath = pathToBooksFolder + "/" + bookInfoToFileName(book.getBookInfo(), configuration);
        return new File(absolutePath);

    }

    private static String bookInfoToFileName(BookInfo bookInfo, Configuration configuration) {
        String author = bookInfo.getAuthor().replace(" ", "_");
        String title = bookInfo.getTitle().replace(" ", "_");
        return author + "-" + title + configuration.getAudioExtension();
    }

}
