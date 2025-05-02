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


    public InizializzatoreAsta() {
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

        creaAste();

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

    public void creaAste(){



        String query="SELECT * FROM prodotto WHERE stato='in_asta' OR stato='non_venduto'";

        try {
            Statement stm=this.con.createStatement();
           ResultSet res=stm.executeQuery(query);

            while (res.next()){

                Prodotto p=new Prodotto(
                        res.getInt("id"),
                        res.getString("stato"),
                        res.getString("nome_categoria"),
                        res.getInt("porta_multicast"),
                        res.getString("username"),
                        res.getString("indirizzo_multicast"),
                        res.getDouble("prezzo_base"),
                        res.getString("descrizione"),
                        res.getString("nome")
                );

                this.executorService.submit(new GestoreAstaNoInput(this.DB_URL,this.password,this.user,p));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }




    }


    public static void main(String[] args) {
        InizializzatoreAsta i = new InizializzatoreAsta();
        i.avvioServerAsta();
    }

}
