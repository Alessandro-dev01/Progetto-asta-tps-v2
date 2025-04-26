package Comunicazione.client;

import Comunicazione.messagges.*;
import Comunicazione.messagges.dataUser;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {

    private int id;
    private String username;
    private Socket connSer;
    private LinkedList<Prodotto> prodotti;
    private Socket clientTcp;
    private ThreadClientMulticast th;
    private BufferedReader input;
    private PrintWriter output;
    private Gson converter;

    public Client() {
        try {
            // Crea il socket TCP per comunicare con il server
            this.clientTcp = new Socket(InetAddress.getByName("127.0.0.1"), 5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            // Inizializza gli stream di input/output
            this.input = new BufferedReader(new InputStreamReader(this.clientTcp.getInputStream()));
            this.output = new PrintWriter(this.clientTcp.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Inizializza il Gson per la conversione JSON
        this.converter = new Gson();
    }

    public void avvio() {
        Scanner scanner = new Scanner(System.in);
        String mes = "";
        int menu = -1;

        do {
            System.out.println(
                    "Inserisci una delle seguenti opzioni: \n" +
                            "1) Login\n" +
                            "2) Registrazione\n" +
                            "3) Carica oggetti posseduti (non implementato)\n" +
                            "4) Partecipa asta (non implementato)"
            );
            menu = scanner.nextInt();
            scanner.nextLine(); // Consuma il newline rimasto

            switch (menu) {
                case 1:
                    login(); // Effettua login
                    break;
                case 2:
                    registrazione(); // Effettua registrazione
                    break;
                case 3:
                    System.out.println("Caricamento oggetti ancora non implementato.");
                    break;
                case 4:
                    partecipaAsta();
                    break;
                default:
                    System.out.println("Opzione non valida, riprova.");
            }
        } while (true);
    }

    public void login() {
        Gson data = new Gson();
        Request re = new Request();
        int operation = -1;
        Scanner scanL = new Scanner(System.in);

        String username = " ", password = " ", mes = "";

        // Prepara la richiesta di login
        re.setType(TypeOfMes.loginRequest);
        String req = this.converter.toJson(re);
        this.output.println(req);

        String reply = "";

        do {
            try {
                reply = this.input.readLine();

                // Capisce che tipo di risposta ha ricevuto
                if (reply.contains("loginRequest")) {
                    operation = 1;
                } else if (reply.contains("loginResponse")) {
                    operation = 2;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            switch (operation) {
                case 1:
                    // Chiede all'utente username e password
                    System.out.println("Inserisci il nome utente:");
                    username = scanL.nextLine();

                    System.out.println("Inserisci la password:");
                    password = scanL.nextLine();

                    // Invia username e password
                    mes = data.toJson(new dataUser(username, password));
                    output.println(mes);

                    break;

                case 2:
                    // Analizza la risposta
                    if (reply.contains("erroreLogin")) {
                        System.out.println("Login non effettuato, dati errati.");

                        // Manda una nuova richiesta di login
                        Request err = new Request();
                        err.setType(TypeOfMes.loginRequest);
                        String StringErr = data.toJson(err);
                        output.println(StringErr);

                    } else if (reply.contains("okLogin")) {
                        System.out.println("Login riuscito!");

                        // Dopo il login: permette di partecipare all'asta
                        partecipaAsta();
                    }
                    break;
            }
        } while (true);
    }

    private void registrazione() {
        String password = "", mes = "", reply = "";
        Gson data = new Gson();
        Scanner scanR = new Scanner(System.in);

        do {
            // Chiede username e password per registrarsi
            System.out.println("Inserisci il nome utente:");
            username = scanR.nextLine();

            System.out.println("Inserisci la password:");
            password = scanR.nextLine();

            // Crea oggetto dataUser e imposta tipo messaggio registrazione
            dataUser d = new dataUser(username, password);
            d.setType(TypeOfMes.registrazione);

            mes = data.toJson(d);
            output.println(mes);

            try {
                reply = input.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Verifica esito registrazione
            if (reply.contains("okRegistrazione")) {
                System.out.println("Registrazione avvenuta con successo!");
                break;
            } else if (reply.contains("erroreRegistrazione")) {
                System.out.println("Errore nella registrazione, riprova.");
            }

        } while (true);
    }

    // Metodo per far partecipare ad un'asta dopo il login

    private void partecipaAsta() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Inserisci l'ID del prodotto per partecipare all'asta:");
        int idProdotto = scanner.nextInt();
        scanner.nextLine(); // Consuma newline rimasto

        // Manda richiesta di partecipazione
        String richiesta = creaRichiestaPartecipazione(idProdotto);
        output.println(richiesta);

        System.out.println("Hai partecipato all'asta! Ora inserisci la tua offerta.");
        System.out.println("Inserisci l'importo dell'offerta:");

        avvioThread(idProdotto); // Avvia il thread multicast per ricevere offerte

        double importo = scanner.nextDouble();
        scanner.nextLine(); // Consuma newline

        String offerta = creaOfferta(username, importo, idProdotto);
        output.println(offerta);

        System.out.println("Offerta inviata correttamente!");
    }

    // Crea messaggio JSON per la partecipazione all'asta
    public String creaRichiestaPartecipazione(int idProdotto) {
        Gson gson = new Gson();
        JsonObject req = new JsonObject();
        req.addProperty("type", "partecipa_asta");
        req.addProperty("id_prodotto", idProdotto);

        return gson.toJson(req);
    }

    // Crea messaggio JSON per l'invio di un'offerta
    public String creaOfferta(String username, double importo, int idProdotto) {
        Gson gson = new Gson();
        JsonObject offerta = new JsonObject();
        offerta.addProperty("type", "offerta");
        offerta.addProperty("utente", username);
        offerta.addProperty("id_prodotto", idProdotto);
        offerta.addProperty("importo", importo);
        offerta.addProperty("timestamp", "2025-04-24T18:30:00Z"); // Timestamp fittizio
        return gson.toJson(offerta);
    }

    // Avvia il thread multicast per ricevere offerte in tempo reale
    public void avvioThread(int idProdotto) {
        try {
            this.th = new ThreadClientMulticast(idProdotto);
            th.start();
        } catch (Exception e) {
            System.out.println("Errore nell'avvio del thread multicast: " + e.getMessage());
        }
    }

    // Main
    public static void main(String[] args) {
        Client c = new Client();
        c.avvio();
    }
}
