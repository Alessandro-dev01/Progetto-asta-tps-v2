package Comunicazione.asta;

import StrutturaOggetti.Prodotto;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// classe inizializzatore che si occupa di ricevere le richieste di creazione di un'asta
// e di creare il Thread lettura per gestire l'asta
public class InizializzatoreAsta {

    // Dati per la connessione al database
    private String DB_URL = "jdbc:mysql://127.0.0.1:3306/asteonline";
    private String password = "";
    private String user = "root";
    private Connection con;

    private ExecutorService executorService;
    private ServerSocket serverAsta;
    private threadIniAsta Iniaste;


    public InizializzatoreAsta() {
        this.Iniaste=new threadIniAsta();
        this.Iniaste.start();
        this.executorService = Executors.newCachedThreadPool();
        try {
            serverAsta = new ServerSocket(4000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.con = DriverManager.getConnection(DB_URL, user, password);
        } catch (SQLException e) {
            System.err.println("Errore connessione al database: " + e.getMessage());
            throw new RuntimeException("Errore connessione al database", e);
        }
    }

    public void avvioServerAsta() {
        Socket richiedenteAsta;

        while (true) {
            try {
                richiedenteAsta = this.serverAsta.accept();
                System.out.println("mi sono connesso");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            LinkedList<String> partecipanti = new LinkedList<>();
            this.executorService.submit(
                    new GestoreAsta(richiedenteAsta, this.DB_URL, this.password, this.user, partecipanti)
            );
            System.out.println("ho creato il task");
        }
    }

    public static void main(String[] args) {
        InizializzatoreAsta i = new InizializzatoreAsta();
        i.avvioServerAsta();
    }

}
