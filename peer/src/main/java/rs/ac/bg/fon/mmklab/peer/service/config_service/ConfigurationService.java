package rs.ac.bg.fon.mmklab.peer.service.config_service;

import rs.ac.bg.fon.mmklab.peer.domain.Configuration;
import rs.ac.bg.fon.mmklab.properties.PropertiesCache;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Locale;

public class ConfigurationService {
    public static Configuration getConfiguration(String localPortTCP, String localPortUDP, String pathToFolder) {

        PropertiesCache props = PropertiesCache.getInstance();

        // kod portova ako se desi greska vraca -1
        int serverPort = getValidPort(props.getProperty("server_port")); // port na kome osluskuje server
//        int localPortTcp = getValidPort(localPortTCP);// port na kome osluskuje nasa sender nit
//        int localPortUdp = getValidPort(localPortUDP); // port na kome primamo audio tok
        int localPortTcp = getGeneratedPort();
        int localPortUdp = getGeneratedPort();

        // kod adrese ako se desi greska vraca null
        InetAddress serverAddress = getValidInetAddress(props.getProperty("server_address"));
        String extension = getValidAudioExtension(props.getProperty("audio_extension"));
        String path = pathToFolder.trim(); // nikakva validacija za sad
        return new Configuration(serverAddress.getCanonicalHostName(), serverPort, localPortTcp, localPortUdp, extension, path);


    }

    private static String getValidAudioExtension(String audioExtension) {
        audioExtension = audioExtension.trim().toLowerCase(Locale.ROOT);
        if (audioExtension == null){
            System.err.println("Greska (ConfigurationService -> getValidAudioExtension): audio ekstenzija nije uspesno procitana iz konfiguracionog fajla");
            return null;
        }
        if(audioExtension.length() != 4 || !audioExtension.startsWith(".")) {
            System.err.println("Greska (ConfigurationService -> getValidAudioExtension): audio ekstenzija nije u odgovarajucem formatu");
            return null;
        }

        return audioExtension;
    }

    private static InetAddress getValidInetAddress(String serverName) {
        InetAddress res = null;
        try {
            res = InetAddress.getByName(serverName);
        } catch (UnknownHostException e) {
//            e.printStackTrace();
            System.err.println("Greska (ConfigurationService -> getValidInetAddress): adresa servera navedena u konfiduracionom fajlu nije validna");
        }
        return res;
    }

    public static int getValidPort(String portTxt) {
        int result = -1;
        if (portTxt == null) {
            System.err.println("Greska (ConfigurationService -> getServerPort): port na kom osluskuje server nije dobro ucitan iz fajla, ili udp port nije dobro ucitan iz gui-ja");
            return result;
        }
        try {
            result = Integer.parseInt(portTxt.trim());
        } catch (NumberFormatException e) {
//            e.printStackTrace();
            System.err.println("Greska (ConfigurationService -> getServerPort): u fajlu nije unet broj tipa int, ili korisnik nije uneo udp port tipa int");
            return result;
        }
        if (result > 1023)
            return result;
        System.err.println("Greska (ConfigurationService -> getServerPort): Broj porta u fajlu, ili onog koji je korisnik uneo nije veci od 1024 pa postoji opasnost da bude zauzet");
        return -1;

    }

    public static int getGeneratedPort(){
        int min =1050;
        int max = 7000;
        int port = 0;
        boolean pronadjenPort = false;

        while(!pronadjenPort){
            port =(int) Math.floor(Math.random() * (max - min + 1) + min);
            if(available(port)) pronadjenPort = true;
        }
        return port;
    }

    public static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
}
