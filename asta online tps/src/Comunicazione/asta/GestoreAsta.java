package Comunicazione.asta;

import Comunicazione.messagges.DatiAsta;
import Comunicazione.messagges.Response;
import Comunicazione.messagges.Result;
import Comunicazione.messagges.TypeOfMes;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;


public class GestoreAsta implements Runnable {

    private Connection con;
    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private Gson converter;
    private String datiProdotto;
    private MonitorVincitore mv;
    private ThreadLetturaAsta letturaAsta;

    public GestoreAsta(Socket richiedenteAsta, String DB_URL, String password, String user) {
        this.client = richiedenteAsta;
        this.converter = new Gson(); // Inizializza sempre il converter

        try {
            this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            this.output = new PrintWriter(this.client.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException("Errore nella creazione degli stream I/O: " + e.getMessage(), e);
        }

        try {
            this.con = DriverManager.getConnection(DB_URL, user, password);
        } catch (SQLException e) {
            System.err.println("Errore connessione al database: " + e.getMessage());
            throw new RuntimeException("Errore connessione al database", e);
        }
    }

    @Override
    public void run() {
        try {
            this.datiProdotto = this.input.readLine();
            System.out.println("Ho ricevuto i dati: " + this.datiProdotto);
        } catch (IOException ex) {
            throw new RuntimeException("Errore nella lettura dei dati dal client", ex);
        }

        if (this.datiProdotto == null || this.datiProdotto.isEmpty()) {
            System.err.println("Dati prodotto vuoti o nulli.");
            closeConnections();
            return;
        }

        Prodotto prod;
        try {
            prod = this.converter.fromJson(this.datiProdotto, Prodotto.class);
        } catch (Exception e) {
            System.err.println("Errore durante la conversione JSON a Prodotto: " + e.getMessage());
            sendAstaCreationError();
            closeConnections();
            return;
        }

        if (prod == null) {
            System.err.println("Prodotto risultante dalla conversione è null");
            sendAstaCreationError();
            closeConnections();
            return;
        }

        System.out.println("Prodotto convertito: " + prod);

        // Inizializza MonitorVincitore e ThreadLetturaAsta
        this.mv = new MonitorVincitore(prod.getUsername(), prod.getPrezzoBase());
        this.letturaAsta = new ThreadLetturaAsta(prod.getIndirizzoMulticast(), prod.getPortaMulticast(), this.mv);

        // Aggiorna lo stato del prodotto nel database
        String update = "UPDATE prodotto SET stato='in_asta' WHERE id=?";
        System.out.println("Esecuzione della query di update");

        try (PreparedStatement stm = this.con.prepareStatement(update)) {
            stm.setInt(1, prod.getId());
            int res = stm.executeUpdate(); // Non passare nuovamente la query qui

            System.out.println("Query eseguita, righe aggiornate: " + res);
            if (res == 0) {
                System.err.println("Nessun prodotto aggiornato, errore nella creazione dell'asta");
                sendAstaCreationError();
                closeConnections();
                return;
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'esecuzione della query: " + e.getMessage());
            sendAstaCreationError();
            closeConnections();
            return;
        }

        // Collegamento al gruppo multicast per partecipare all'asta
        try (MulticastSocket multicastSocket = new MulticastSocket()) {
            InetAddress groupAddress = InetAddress.getByName(prod.getIndirizzoMulticast());
            multicastSocket.joinGroup(groupAddress);

            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    groupAddress, prod.getPortaMulticast());

            // Qui potresti voler inviare o ricevere pacchetti in relazione all'asta
            System.out.println("Connesso all'asta multicast: " + prod.getIndirizzoMulticast() +
                    ":" + prod.getPortaMulticast());
        } catch (IOException e) {
            System.err.println("Errore durante la connessione al gruppo multicast: " + e.getMessage());
        }

        // Potresti avviare il thread di lettura dell'asta se necessario:
        // new Thread(this.letturaAsta).start();
    }

    private void sendAstaCreationError() {
        Response resp = new Response();
        resp.setType(TypeOfMes.creazione_asta);
        resp.setEsito(Result.erroreCreazioneAsta);
        String jsonResp = this.converter.toJson(resp);
        this.output.println(jsonResp);
    }

    private void closeConnections() {
        try {
            if (this.client != null && !this.client.isClosed()) {
                this.client.close();
            }
        } catch (IOException e) {
            System.err.println("Errore nella chiusura della connessione: " + e.getMessage());
        }
    }
}




