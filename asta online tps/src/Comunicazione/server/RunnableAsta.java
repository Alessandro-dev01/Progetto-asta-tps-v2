package Comunicazione.server;

import Comunicazione.asta.IniziallizatoreAsta;
import Comunicazione.client.ThreadClientMulticast;
import Comunicazione.messagges.*;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.sound.sampled.Port;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;
import java.util.LinkedList;

public class RunnableAsta implements Runnable {

    private double importoFinale = 0.0;
    private String vincitore = "";
    private String username;
    private Socket client;
    private Connection con;
    private Gson converter;
    private BufferedReader input;
    private PrintWriter output;
    private IniziallizatoreAsta gestioneAsta;


    // Multicast Socket per inviare messaggi
    private MulticastSocket multicastSocket;

    public RunnableAsta(Socket client, String DB_URL, String password, String user) {
        this.gestioneAsta=new IniziallizatoreAsta();
        this.gestioneAsta.start();
        this.client = client;
        this.converter = new Gson();

        // Connessione al database per interagire con i dati relativi agli utenti e prodotti
//        try {
//            this.con = DriverManager.getConnection(DB_URL, user, password);
//            this.multicastSocket = new MulticastSocket();
//
//        } catch (SQLException e) {
//            System.err.println("Errore connessione al database: " + e.getMessage());
//
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }

    @Override
    public void run() {
        String mes;
        try {
            this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            this.output = new PrintWriter(this.client.getOutputStream(), true); // Crea gli stream di input/output
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        do {
            try {
                mes = input.readLine(); // Legge il messaggio dal client
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Gestisce diverse richieste ricevute dal client
            if (mes.contains("loginRequest")) {
                loginRequest(); // Gestisce la richiesta di login
            } else if (mes.contains("partecipa_asta")) {
                partecipaAsta(mes); // Gestisce la partecipazione all'creazione_asta
            } else if (mes.contains("registrazione")) {
                registrazioneRequest(mes); // Gestisce la registrazione di un nuovo utente
            } else if (mes.contains("visualizza_prodotti")) {
                inviaListaProdotti();
            } else if (mes.contains("creazione_asta")) {
                creazioneAsta(mes);
            } else if (mes.contains("possedutiRequest")) {
                cercaPosseduti(mes);
            }
        } while (true);

    }

    private void cercaPosseduti(String msg) {

        Request res=new Request();
        LinkedList<Prodotto> list=new LinkedList<Prodotto>();
        String reply="",sql="";


         sql = "SELECT id, nome, descrizione, prezzo_base, indirizzo_multicast, username, porta_multicast, nome_categoria, stato FROM prodotto WHERE username= ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {  // Usa while per iterare su tutte le righe
                    list.add(new Prodotto(
                            rs.getInt("id"),
                            rs.getString("stato"),
                            rs.getString("nome_categoria"),
                            rs.getInt("porta_multicast"),
                            rs.getString("username"),
                            rs.getString("indirizzo_multicast"),
                            rs.getDouble("prezzo_base"),
                            rs.getString("descrizione"),
                            rs.getString("nome")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        reply=this.converter.toJson(list);

        this.output.println(reply);
    }

    // Gestisce la procedura di login del client
    public void loginRequest() {
        Request request = new Request();
        request.setType(TypeOfMes.loginRequest);

        String mes = this.converter.toJson(request);
        output.println(mes); // Invia la richiesta di login al client

        String reply = "";
        try {
            reply = input.readLine(); // Riceve la risposta dal client
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(reply);

        dataUser log = this.converter.fromJson(reply, dataUser.class);

        // Query SQL per verificare se l'utente esiste nel database
        String queryUtente = "SELECT * FROM utente WHERE username='" + log.getUsername() + "' AND password='" + log.getPassword() + "'";

        try (Statement stm = this.con.createStatement();
             ResultSet resQuery = stm.executeQuery(queryUtente)) {

            if (resQuery.next()) { // Se l'utente esiste nel database
                Response response = new Response();
                response.setType(TypeOfMes.loginResponse);
                response.setEsito(Result.okLogin);
                String logres = this.converter.toJson(response);
                this.output.println(logres); // Risposta positiva al login
                this.username=log.getUsername();

            } else { // Se l'utente non esiste
                Response response = new Response();
                response.setType(TypeOfMes.loginResponse);
                response.setEsito(Result.erroreLogin);
                String logres = this.converter.toJson(response);
                this.output.println(logres); // Risposta negativa al login
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Gestisce la registrazione di un nuovo utente
    public void registrazioneRequest(String mes) {
        dataUser reg = this.converter.fromJson(mes, dataUser.class);

        String queryUtente = "SELECT * FROM utente WHERE username='" + reg.getUsername() + "'";

        try (Statement stm = this.con.createStatement();
             ResultSet resQuery = stm.executeQuery(queryUtente)) {

            if (resQuery.next()) { // Se l'utente esiste già nel database
                Response response = new Response();
                response.setType(TypeOfMes.registrazioneResponse);
                response.setEsito(Result.erroreRegistrazione);
                String logres = this.converter.toJson(response);
                this.output.println(logres); // Risposta negativa alla registrazione
            } else {
                // Se l'utente non esiste, registralo nel database
                String insert = "INSERT INTO utente(username, password) VALUES('" + reg.getUsername() + "','" + reg.getPassword() + "')";
                int regS = stm.executeUpdate(insert);

                Response response = new Response();
                response.setType(TypeOfMes.registrazioneResponse);
                response.setEsito(regS != 0 ? Result.okRegistrazione : Result.erroreRegistrazione);
                String logres = this.converter.toJson(response);
                this.output.println(logres); // Risposta positiva o negativa alla registrazione
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Gestisce la partecipazione di un client ad un'creazione_asta
    public void partecipaAsta(String msg) {
        try {

            long tempoMax = 60000; // Tempo massimo per l'creazione_asta (60 secondi)
            long tempoInizio = System.currentTimeMillis();

            System.out.println("Richiesta ricevuta: " + msg);

            JsonObject jsonReq = JsonParser.parseString(msg).getAsJsonObject();


            if (jsonReq.has("type") && jsonReq.get("type").getAsString().equals("partecipa_asta")) {

                int idProdotto = jsonReq.get("id_prodotto").getAsInt();
                Prodotto prodotto = visualizzaProdotto(idProdotto);

                if (prodotto != null) {

                    JsonObject risposta = new JsonObject();
                    risposta.addProperty("type", "ok");
                    risposta.addProperty("indirizzo_multicast", prodotto.getIndirizzoMulticast());
                    risposta.addProperty("porta_multicast", prodotto.getPortaMulticast());
                    output.println(risposta); // Risposta con indirizzo multicast per partecipare



//                    importoFinale = prodotto.getPrezzoBase(); // Imposta il prezzo base
//                    monitoraAsta(idProdotto,tempoMax);
//
//                    //avvioThread(idProdotto, prodotto.getIndirizzoMulticast(), prodotto.getPortaMulticast()); // Avvio thread multicast per gestire offerte
//
//                    //         new Thread(() -> {
//                    //                        monitoraAsta(idProdotto, tempoMax);
//                    //                    }).start();
//
//
//                    while (System.currentTimeMillis() - tempoInizio > tempoMax) {
//                        String offertaRicevuta = input.readLine();
//                        if (offertaRicevuta != null && !offertaRicevuta.isEmpty()) {
//                            JsonObject jsOfferta = JsonParser.parseString(offertaRicevuta).getAsJsonObject();
//                            if (jsOfferta.has("type") && jsOfferta.get("type").getAsString().equals("offerta")) {
//                                double importo = jsOfferta.get("importo").getAsDouble();
//                                String utente = jsOfferta.get("utente").getAsString();
//
//                                System.out.println(importo+" "+utente);
//
//                                if (importo > importoFinale) {
//                                    importoFinale = importo; // Aggiorna l'importo finale se l'offerta è maggiore
//                                    vincitore = utente; // Aggiorna il vincitore
//                                    System.out.println("Nuova offerta! Utente: " + utente + " con importo: " + importo);
//                                    try {
//                                        aggiornamentoOfferta(importoFinale, vincitore, prodotto.getIndirizzoMulticast(), prodotto.getPortaMulticast()); // Aggiorna l'offerta
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                } else {
//                                    System.out.println("Offerta rifiutata: troppo bassa");
//                                }
//                            }
//                        }
//                    }

                    System.out.println("Asta terminata! Tempo massimo raggiunto.");
                    fineAsta(idProdotto, vincitore, importoFinale);

                } else {
                    JsonObject rispostaErr = new JsonObject();
                    rispostaErr.addProperty("esito", "errore");
                    rispostaErr.addProperty("messaggio", "prodotto non trovato!");
                    output.println(rispostaErr); // Risposta con errore se il prodotto non è trovato
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void creazioneAsta(String mes){

        DatiAsta daR=this.converter.fromJson(mes,DatiAsta.class);

      String sql = "SELECT id, nome, descrizione, prezzo_base, indirizzo_multicast, username, porta_multicast, nome_categoria, stato FROM prodotto WHERE username= ? AND id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.username);
            ps.setInt(2,daR.getIdProdotto());

            try (ResultSet rs = ps.executeQuery()) {
                //controllo se quell'oggetto appartiene allo user che il thread sta gestende
               if (rs.next()) {
                    Prodotto p=new Prodotto(
                            rs.getInt("id"),
                            rs.getString("stato"),
                            rs.getString("nome_categoria"),
                            rs.getInt("porta_multicast"),
                            rs.getString("username"),
                            rs.getString("indirizzo_multicast"),
                            rs.getDouble("prezzo_base"),
                            rs.getString("descrizione"),
                            rs.getString("nome")
                    );

                    String prodJson=this.converter.toJson(p);

                    Socket sAsta=new Socket(InetAddress.getByName("127.0.0.1"),5001);

                  PrintWriter outputAsta=new PrintWriter(sAsta.getOutputStream(),true);

                  outputAsta.println(prodJson);



                  outputAsta.close();
                }
               else {
                   Response r=new Response();
                   r.setType(TypeOfMes.Asta);
                   r.setEsito(Result.erroreCreazioneAsta);

                   String jsonRes=this.converter.toJson(r);

                   this.output.println(jsonRes);

               }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    public void avvioThread(int idProdotto, String indirizzoMulticast, int portaMulticast) {
        try {
           // this.thread = new ThreadClientMulticast(idProdotto, indirizzoMulticast, portaMulticast);
            //thread.start(); // Avvia il thread
        } catch (Exception e) {
            System.out.println("Errore nell'avvio del thread multicast: " + e.getMessage());
        }
    }

    // Gestisce l'aggiornamento dell'offerta
    public void aggiornamentoOfferta(double importo, String username, String indirizzoMulticast, int portaMulticast) throws IOException {
        JsonObject offerta = new JsonObject();
        offerta.addProperty("type", "nuova_offerta");
        offerta.addProperty("utente", username);
        offerta.addProperty("importo", importo);

        // Gestione multicast per inviare l'offerta a tutti i partecipanti
        byte[] buffer = offerta.toString().getBytes();
        InetAddress multicastIp = InetAddress.getByName(indirizzoMulticast);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastIp, portaMulticast);
        //multicastSocket.send(packet); // Invia il pacchetto multicast
    }

    // Carica le informazioni di un prodotto dal database dato l'ID
    public Prodotto visualizzaProdotto(int idProdotto) {
        String sql = "SELECT id, nome, descrizione, prezzo_base, indirizzo_multicast, username, porta_multicast, nome_categoria, stato FROM prodotto WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProdotto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Prodotto(
                        rs.getInt("id"),
                        rs.getString("stato"),
                        rs.getString("nome_categoria"),
                        rs.getInt("porta_multicast"),
                        rs.getString("username"),
                        rs.getString("indirizzo_multicast"),
                        rs.getDouble("prezzo_base"),
                        rs.getString("descrizione"),
                        rs.getString("nome")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Invio della lista dei prodotti all'creazione_asta
    public void inviaListaProdotti() {
        String query = "SELECT * FROM prodotto ORDER BY nome_categoria";
        try (Statement stm = con.createStatement();
             ResultSet rs = stm.executeQuery(query)) {

            JsonObject ris = new JsonObject();
            ris.addProperty("type", "lista_prodotti");

            JsonObject prodottiPerCat = new JsonObject();

            JsonArray prodottoCat = new JsonArray();
            String categoriaCorr = "";

            while (rs.next()) {

                String categoria = rs.getString("nome_categoria");

                Prodotto p = new Prodotto(
                        rs.getInt("id"),
                        rs.getString("stato"),
                        rs.getString("nome_categoria"),
                        rs.getInt("porta_multicast"),
                        rs.getString("username"),
                        rs.getString("indirizzo_multicast"),
                        rs.getDouble("prezzo_base"),
                        rs.getString("descrizione"),
                        rs.getString("nome")
                );

                if (!categoria.equals(categoriaCorr)) {
                    if (!categoriaCorr.equals("")) {
                        prodottiPerCat.add(categoriaCorr, prodottoCat);
                    }
                    categoriaCorr = categoria;
                    prodottoCat = new JsonArray();
                }

                prodottoCat.add(new Gson().toJsonTree(p));
            }

            // aggiungo l'ultima categoria rimasta fuori dal ciclo
            if (!categoriaCorr.equals("")) {
                prodottiPerCat.add(categoriaCorr, prodottoCat);
            }

            ris.add("prodotti", prodottiPerCat);
            output.println(ris);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Monitora il tempo dell'creazione_asta
    public void monitoraAsta(int idProdotto, long tempoMax) {
        long tempoInizio = System.currentTimeMillis();
        long tempoFine = tempoInizio + tempoMax;

        while (System.currentTimeMillis() < tempoFine) {
            // Continua l'creazione_asta
            try {
                Thread.sleep(1000); // Riduce il carico sul processore
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            fineAsta(idProdotto, vincitore, importoFinale); // Chiama la fine dell'creazione_asta
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Gestisce la fine dell'creazione_asta
    public void fineAsta(int idProdotto, String vincitore, double importoFinale) throws IOException {
        JsonObject fineAsta = new JsonObject();
        fineAsta.addProperty("type", "fine_asta");
        fineAsta.addProperty("id_prodotto", idProdotto);
        fineAsta.addProperty("vincitore", vincitore);
        fineAsta.addProperty("importo_finale", importoFinale);

        Prodotto prodotto = visualizzaProdotto(idProdotto);

        String indirizzoMulticast = prodotto.getIndirizzoMulticast();
        int portaMulticast = prodotto.getPortaMulticast();

        byte[] buffer = fineAsta.toString().getBytes();
        InetAddress multicastAdd = InetAddress.getByName(indirizzoMulticast);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastAdd, portaMulticast);

        //multicastSocket.send(packet); // Invia il pacchetto multicast che segnala la fine dell'creazione_asta
    }

}
