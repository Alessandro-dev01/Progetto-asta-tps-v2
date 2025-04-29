package Comunicazione.client;

import Comunicazione.messagges.*;
import Comunicazione.messagges.dataUser;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {

    private String username;
    private Socket connSer;
    private HashMap<Integer,Prodotto> prodotti;
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

    public void avvio() throws IOException {
        Scanner scanner = new Scanner(System.in);
        int menu = -1;

        do {
            // Stampa il menu delle opzioni per l'utente
            System.out.println(
                    "Inserisci una delle seguenti opzioni: \n" +
                            "1) Effettua il login\n" +
                            "2) Effettua la registrazione\n" +
                            "3) Esci dal programma."
            );
            menu = scanner.nextInt();
            scanner.nextLine(); // Consuma il newline rimasto

            // Gestisce la selezione del menu
            switch (menu) {
                case 1:
                    login(); // Effettua login
                    break;
                case 2:
                    registrazione(); // Effettua registrazione
                    break;
                case 3:
                    System.out.println("Uscita dal programma....");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opzione non valida, riprova.");
            }
        } while (true);
    }

    public void login() throws IOException {
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
                        this.username=username;
                        menuDopoLogin();
                    }
                    break;
            }
        } while (true);
    }

    public void menuDopoLogin() throws IOException {
        Scanner scanner = new Scanner(System.in);
        int menu = -1;

        do {
            // Stampa il menu delle opzioni per l'utente
            System.out.println(
                    "Inserisci una delle seguenti opzioni:\n" +
                            "1) Visualizza oggetti in tuo possesso \n" +
                            "2) Visualizza oggetti in asta\n" +
                            "3) crea un'asta\n"+
                            "4) Esci"
            );
            menu = scanner.nextInt();
            scanner.nextLine(); // Consuma il newline rimasto

            // Gestisce la selezione del menu
            switch (menu) {
                case 1:
                    visualizzaOggettiInPossesso(this.username);
                    break;
                case 2:
                    visualizzaOggettiInAsta();
                    break;
                case 3:{
                    inizializzaAsta();
                    break;
                }
                case 4:
                    System.out.println("Uscita dal programma....");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opzione non valida, riprova.");
            }
        } while (true);
    }

    private void visualizzaOggettiInPossesso(String username) {
       Request r=new Request();

       r.setType(TypeOfMes.possedutiRequest);

       String rJ=this.converter.toJson(r);

       this.output.println(rJ);

        String msg="";
        try {
           msg=this.input.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Type listType=new TypeToken<LinkedList<Prodotto>>(){}.getType();

        LinkedList<Prodotto> result=this.converter.fromJson(msg,listType);

        if (!result.isEmpty()){
            System.out.println(result.toString());
        }
        else System.out.println("non possedi nessun oggetto");


    }


    private void registrazione() {
        String password = "", mes = "", reply = "", username;
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
                reply = input.readLine(); // Legge la risposta dal server
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

    // Metodo per far partecipare ad un'creazione_asta dopo il login
    public void partecipaAsta(int idProdotto) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Manda richiesta di partecipazione
        String richiesta = creaRichiestaPartecipazione(idProdotto);
        output.println(richiesta);

        String req = input.readLine();

        JsonObject jsonReq = JsonParser.parseString(req).getAsJsonObject();

        if (jsonReq.get("type").getAsString().equals("ok")) {
            String indirizzoMulti = jsonReq.get("indirizzo_multicast").getAsString();
            int portaMulti = jsonReq.get("porta_multicast").getAsInt();

            avvioThread(idProdotto, indirizzoMulti, portaMulti); // Avvia il thread multicast per ricevere offerte

            System.out.println("stai partecipando all'creazione_asta!");









//            while (true) {
//                System.out.println("Inserisci l'importo della tua offerta. Oppure digita 'esci' per tornare al menu");
//
//                String input = scanner.nextLine();
//
//                if (input.equals("esci")) {
//                    break;
//                }
//
//                try {
//                    double importo = Double.parseDouble(input);
//
//                    String offerta = creaOfferta(username, importo, idProdotto);
//                    output.println(offerta);
//
//                    System.out.println("Offerta inviata correttamente!");
//                } catch (NumberFormatException e) {
//                    System.out.println("Importo non valido. Riprova.");
//                }
//            }
       } else {
          System.out.println("Errore nella partecipazione all'creazione_asta: " + jsonReq.get("messaggio").getAsString());
      }

    }

    public void visualizzaOggettiInAsta() throws IOException {

        Scanner scanner = new Scanner(System.in);

        JsonObject richiesta = new JsonObject();
        richiesta.addProperty("type", "visualizza_prodotti"); // Tipo di richiesta
        output.println(richiesta);

        try {
            String ris = input.readLine(); // Riceve la risposta dal server
            JsonObject jsonRisposta = JsonParser.parseString(ris).getAsJsonObject();

            if (jsonRisposta.get("type").getAsString().equals("lista_prodotti")) {

                // Estrai la stringa JSON che rappresenta l'array di prodotti
                JsonObject prodotto = jsonRisposta.getAsJsonObject("prodotti");

                Gson gson = new Gson();
                System.out.println("Prodotti disponibili all'creazione_asta:");

                LinkedList<Prodotto> listaProdotti = new LinkedList<>();
                LinkedList<Integer> listaIdProdotti = new LinkedList<>();

                for (String categoria : prodotto.keySet()) {

                    System.out.println("---------------------------------------");
                    System.out.println("Categoria: " + categoria);

                    JsonArray jsonArray = prodotto.getAsJsonArray(categoria);
                    Prodotto[] prodotti = gson.fromJson(jsonArray, Prodotto[].class);

                    // Stampa i prodotti disponibili
                    for (Prodotto p : prodotti) {
                        listaProdotti.add(p);
                        listaIdProdotti.add(p.getId());

                        System.out.println("id: " + p.getId() +
                                " Nome: " + p.getNome() +
                                " Descrizione: " + p.getDescrizione() +
                                " Categoria: " + p.getNome_categoria() +
                                " Prezzo Base: €" + p.getPrezzoBase() +
                                " Indirizzo multicast: " + p.getIndirizzoMulticast() +
                                " Porta multicast: " + p.getPortaMulticast() +
                                " Username: " + p.getUsername() +
                                " Stato: " + p.getStato());
                    }
                }

                int idProdotto = -1;
                boolean valido = false;

                while (!valido) {
                    System.out.println("Per partecipare ad un'creazione_asta inserisci l'ID del determinato prodotto: ");
                    if (scanner.hasNextInt()) {
                        idProdotto = scanner.nextInt();

                        if (idProdotto < 0 || !listaIdProdotti.contains(idProdotto)) {
                            System.out.println("Prodotto inesistente, riprova.");
                        } else {
                            valido = true;
                        }
                    }
                }

                partecipaAsta(idProdotto);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void inizializzaAsta() {
        Scanner scanner=new Scanner(System.in);
        DatiAsta da=new DatiAsta();

        System.out.println("inserisci l'id del prodotto");
        da.setIdProdotto(scanner.nextInt());


        String reqJ=this.converter.toJson(da,DatiAsta.class);
        String reply="", id="";
        this.output.println(reqJ);

        try {
            reply=this.input.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (reply.contains("erroreCreazioneAsta")){
            System.out.println("errore nella creazione dell'asta, " +
                    "non possiedi quel prodotto o il proddotto non è presente");
        }

    }

    // Crea messaggio JSON per la partecipazione all'creazione_asta
    public String creaRichiestaPartecipazione(int idProdotto) {
        JsonObject req = new JsonObject();
        req.addProperty("type", "partecipa_asta");
        req.addProperty("id_prodotto", idProdotto); // ID del prodotto

        return this.converter.toJson(req); // Serializza la richiesta in JSON
    }

    // Crea messaggio JSON per l'invio di un'offerta
    public String creaOfferta(String username, double importo, int idProdotto) {
        JsonObject offerta = new JsonObject();
        offerta.addProperty("type", "offerta");
        offerta.addProperty("utente", username); // Nome utente
        offerta.addProperty("id_prodotto", idProdotto); // ID del prodotto
        offerta.addProperty("importo", importo); // Importo offerto

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        offerta.addProperty("timestamp", timestamp);
        return this.converter.toJson(offerta); // Serializza l'offerta in JSON
    }

    // Avvia il thread multicast per ricevere offerte in tempo reale
    public void avvioThread(int idProdotto, String mulicastIp, int portaMulticast) {
        try {
            this.th = new ThreadClientMulticast(idProdotto, mulicastIp, portaMulticast);
            th.start(); // Avvia il thread
        } catch (Exception e) {
            System.out.println("Errore nell'avvio del thread multicast: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.avvio();
    }
}
