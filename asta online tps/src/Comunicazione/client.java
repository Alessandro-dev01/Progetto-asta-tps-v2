package Comunicazione;
import StrutturaOggetti.Prodotto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class client {

    private int id;
    private String username;
    private Socket connSer;
    private LinkedList<Prodotto> prodotti;

    public client(String username) {
        this.username = username;
        try {
            this.connSer = new Socket(InetAddress.getByName("127.0.0.1"),5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.prodotti = new LinkedList<Prodotto>();
    }

    
}
