package Comunicazione.asta;

import Comunicazione.messagges.*;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

// classe gestore che si occcupa della gestione dell'asta e modifica il db in funzione dell'asta
public class GestoreAsta implements Runnable {

    private Connection con;
    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private Gson converter;
    private String datiProdotto;
    private MonitorVincitore mv;
    private LinkedList<String> partecipanti;

    public GestoreAsta(Socket richiedenteAsta, String DB_URL, String password, String user, LinkedList<String> partecipanti) {
        this.client = richiedenteAsta;
        this.converter = new Gson(); // Inizializza sempre il converter
        this.partecipanti = new LinkedList<>();

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

        // Aggiorna lo stato del prodotto nel database
        String update = "UPDATE prodotto SET stato='in_asta' WHERE id=?";
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
        try (MulticastSocket multicastSocket = new MulticastSocket(prod.getPortaMulticast())) {

            InetAddress groupAddress = InetAddress.getByName(prod.getIndirizzoMulticast());
            try {
                multicastSocket.joinGroup(groupAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("GestoreAsta: in attesa di partecipanti...");


            System.out.println(groupAddress);

            while (partecipanti.size() < 2) {

                byte[] buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                System.out.println(partecipanti.size()+" "+partecipanti.toString());
                multicastSocket.receive(receivePacket);




                String messaggio = new String(receivePacket.getData(), 0, receivePacket.getLength());

                try {
                    MessaggioPresenza messaggioPresenza = this.converter.fromJson(messaggio, MessaggioPresenza.class);

                    if (messaggioPresenza.getType().equals("presenza")) {
                        String username = messaggioPresenza.getUsername();

                        if (!username.equals(prod.getUsername()) && !partecipanti.contains(username)) {
                            partecipanti.add(username);
                            System.out.println("Utente aggiunto: " + username);
                            System.out.println(partecipanti.size()+" "+partecipanti.toString());
                            // invia al client un messaggio di attesa
                            Response wait = new Response(Result.in_attesa, TypeOfMes.attesa_partecipanti);
                            //output.println(this.converter.toJson(wait));

                        }

                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Messaggio non valido ricevuto: " + messaggio);
                }

            }



            //una volta raggiunto il numero minimo di persone si invia
            // un pachetto multicast per far iniziare l'asta
            byte[] pk=new byte[512];
            
            Response inizio = new Response(Result.ok, TypeOfMes.start_asta);
            System.out.println("sono fuori dal while");
            String start=this.converter.toJson(inizio,Response.class);
            System.out.println("ho creato la risposta");
            DatagramPacket outputMulticast = new DatagramPacket(start.getBytes(), start.length(), groupAddress, prod.getPortaMulticast());
            DatagramPacket inputMulticast = new DatagramPacket(start.getBytes(), start.length());


            try {
                multicastSocket.send(outputMulticast);
                System.out.println("ho inviato il messaggio");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            outputMulticast=new DatagramPacket(pk,pk.length,groupAddress, prod.getPortaMulticast());

           // output.println(this.converter.toJson(inizio));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Asta per prodotto: " + prod.getNome() + " iniziata!");
            mv = new MonitorVincitore(prod.getUsername(), prod.getPrezzoBase());
//            letturaAsta = new ThreadLetturaAsta(prod.getIndirizzoMulticast(), prod.getPortaMulticast(), mv);
//            letturaAsta.start();

//            // aspetto che termini il thread
//            letturaAsta.join();
            multicastSocket.setSoTimeout(6000);

            while (!this.mv.isAstaTerminata()) {
                try {
                    multicastSocket.receive(inputMulticast);
                    String dati = new String(inputMulticast.getData(), 0, inputMulticast.getLength());

                    if (dati.contains("offerta")) {
                        Offerta offerta = this.converter.fromJson(dati, Offerta.class);
                        this.mv.aggiornaOfferta(offerta.getUsename(), offerta.getImporto());
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("asta finita ha vinto: "+this.mv.getUsername()+
                            " con un importo di "+this.mv.getOffertaMassima());

                    FineAsta fineAsta = new FineAsta(prod.getId(), mv.getUsername(), mv.getOffertaMassima());
                    String js = this.converter.toJson(fineAsta);

                    outputMulticast.setData(js.getBytes());

                    multicastSocket.send(outputMulticast);
                    this.mv.setFineAsta(true);
                    multicastSocket.close();

                    String idUtente = mv.getUsername();

                    String insert = "INSERT INTO aggiudicazioni (id_prodotto, id_utente, prezzo_finale, data_offerta) "
                            + "VALUES (?, ?, ?, ?)";

                    try (PreparedStatement pA = con.prepareStatement(insert)) {
                        pA.setInt(1, prod.getId());
                        pA.setString(2, idUtente); //da cambiare nel db il tipo id utente
                        pA.setDouble(3, mv.getOffertaMassima());
                        String now = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        pA.setString(4, now);
                        pA.executeUpdate();

                    } catch (SQLException e3) {
                        throw new RuntimeException(e3);
                    }

                    String up = "UPDATE prodotto SET stato='chiuso' WHERE id = ?";
                    try (PreparedStatement pP = con.prepareStatement(up)) {
                        pP.setInt(1, prod.getId());
                        pP.executeUpdate();
                    } catch (SQLException e2) {
                        throw new RuntimeException(e2);
                    }

                }  catch(SocketException e){
                    System.out.println("connessione chiusa ");
                    this.mv.setFineAsta(true);
                    multicastSocket.close();
                } catch (IOException e) {
                    System.out.println("errore nell IO");
                    this.mv.setFineAsta(true);
                    multicastSocket.close();
                }


            }

    } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    private void sendAstaCreationError() {
        Response resp = new Response();
        resp.setType(TypeOfMes.creazione_asta);
        resp.setEsito(Result.erroreCreazioneAsta);
        String jsonResp = this.converter.toJson(resp);
        this.output.println(jsonResp);
    }

    public void closeConnections(){
        try {
            if (this.client != null && !this.client.isClosed()) {
                this.client.close();
            }
        } catch (IOException e) {
            System.err.println("Errore nella chiusura della connessione: " + e.getMessage());
        }
    }
}




