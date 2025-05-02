package Comunicazione.asta;

import StrutturaOggetti.Prodotto;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class threadIniAsta extends Thread {

    private String DB_URL = "jdbc:mysql://127.0.0.1:3306/asteonline";
    private String password = "";
    private String user = "root";

    private ExecutorService service;
    private Connection con;


    public threadIniAsta() {
        this.service = Executors.newCachedThreadPool();

        try {
            this.con = DriverManager.getConnection(DB_URL, user, password);
        } catch (SQLException e) {
            System.err.println("Errore connessione al database: " + e.getMessage());
            throw new RuntimeException("Errore connessione al database", e);
        }
    }

    public void run() {

        String query = "SELECT * FROM prodotto WHERE stato='in_asta' OR stato='non_venduto'";

        try {
            Statement stm = this.con.createStatement();
            ResultSet res = stm.executeQuery(query);

            while (res.next()) {

                Prodotto p = new Prodotto(
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

                this.service.submit(new GestoreAstaNoInput(this.DB_URL, this.password, this.user, p));
                System.out.println("fatto task per "+p.getNome());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
