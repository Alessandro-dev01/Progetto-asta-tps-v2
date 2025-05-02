package Comunicazione.server;

import Comunicazione.messagges.*;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;
import java.util.LinkedList;

// classe handler che gestisce il menu dopoLogin, la partecipazioni ad un'asta, l'inviio della creazione di un'asta
// e della visualizzazione degli oggetti in possesso
public class AstaClientHandler implements Runnable {

    private double importoFinale = 0.0;
    private String vincitore = "";
    private String username;
    private Socket client;
    private Connection con;
    private Gson converter;
    private BufferedReader input;
    private PrintWriter output;

    // Multicast Socket per inviare messaggi
    public AstaClientHandler(Socket client, String DB_URL, String password, String user) {
        this.client = client;
        this.converter = new Gson();

        // Connessione al database per interagire con i dati relativi agli utenti e prodotti
        try {
            this.con = DriverManager.getConnection(DB_URL, user, password);


        } catch (SQLException e) {
            System.err.println("Errore connessione al database: " + e.getMessage());
        }
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

        Request res = new Request();
        LinkedList<Prodotto> list = new LinkedList<Prodotto>();
        String reply = "", sql = "";


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

        reply = this.converter.toJson(list);
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
                this.username = log.getUsername();

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
        vincitore = "";

        PartecipazioneRequest partecipazioneRequest = this.converter.fromJson(msg, PartecipazioneRequest.class);

        if (partecipazioneRequest != null && partecipazioneRequest.getType() != null && partecipazioneRequest.getType().contains("partecipa_asta")) {
            int idProdotto = partecipazioneRequest.getId_prodotto();
            Prodotto prodotto = visualizzaProdotto(idProdotto);

            if (prodotto != null) {
                PartecipazioneResponse partecipazioneResponse = new PartecipazioneResponse(prodotto.getIndirizzoMulticast(),
                        prodotto.getPortaMulticast(), prodotto.getPrezzoBase());
                String risposta = this.converter.toJson(partecipazioneResponse);
                output.println(risposta); // Risposta con indirizzo multicast per partecipare

                importoFinale = prodotto.getPrezzoBase(); // Imposta il prezzo base

            } else {
                RispostaErrore rispostaErrore = new RispostaErrore(Result.prodottoNonTrovato, TypeOfMes.prodottoNonTrovato, "Prodotto non trovato!");
                String rispostaJs = this.converter.toJson(rispostaErrore);
                output.println(rispostaJs);
            }
        }
    }

    public void creazioneAsta(String mes) {

        DatiAsta daR = this.converter.fromJson(mes, DatiAsta.class);

        String sql = "SELECT id, nome, descrizione, prezzo_base, indirizzo_multicast, username, porta_multicast, nome_categoria, stato FROM prodotto WHERE username= ? AND id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.username);
            ps.setInt(2, daR.getIdProdotto());

            try (ResultSet rs = ps.executeQuery()) {
                //controllo se quell'oggetto appartiene allo user che il thread sta gestende
                if (rs.next()) {
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

                    String prodJson = this.converter.toJson(p);

                    Socket sAsta = new Socket(InetAddress.getByName("127.0.0.1"), 4000);

                    PrintWriter outputAsta = new PrintWriter(sAsta.getOutputStream(), true);

                    outputAsta.println(prodJson);

                    Response resp=new Response(Result.ok,TypeOfMes.creazione_asta);
                    String js=this.converter.toJson(resp,Response.class);

                    this.output.println(js);

                } else {
                    Response r = new Response();
                    r.setType(TypeOfMes.Asta);
                    r.setEsito(Result.erroreCreazioneAsta);

                    String jsonRes = this.converter.toJson(r);

                    this.output.println(jsonRes);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        String query = "SELECT * FROM prodotto WHERE stato='in_asta' ORDER BY nome_categoria ";
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


}
