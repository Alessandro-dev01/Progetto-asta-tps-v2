package Comunicazione.client;

import Comunicazione.asta.MonitorVincitore;
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
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {

    private String username;
    private Socket connSer;
    private Socket clientTcp;
    private ThreadLetturaAstaClient th;
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
                        this.username = username;
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
                            "3) crea un'asta\n" +
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
                case 3: {
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
        Request r = new Request();

        r.setType(TypeOfMes.possedutiRequest);

        String rJ = this.converter.toJson(r);

        this.output.println(rJ);

        String msg = "";
        try {
            msg = this.input.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Type listType = new TypeToken<LinkedList<Prodotto>>() {
        }.getType();

        LinkedList<Prodotto> result = this.converter.fromJson(msg, listType);

        if (!result.isEmpty()) {
            System.out.println(result.toString());
        } else System.out.println("non possedi nessun oggetto");


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

    // Metodo per far partecipare ad un'asta dopo il login
    public void partecipaAsta(int idProdotto) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Manda richiesta di partecipazione
        PartecipazioneRequest richiesta = new PartecipazioneRequest(idProdotto);
        String jsonRichiesta = converter.toJson(richiesta); // Serializza l'oggetto in JSON
        output.println(jsonRichiesta);



        // Riceve la risposta dal server
        String rispostaServer = input.readLine();

        PartecipazioneResponse risposta = converter.fromJson(rispostaServer, PartecipazioneResponse.class);



        // Verifica che la risposta del server sia positiva
        if (!"partecipa_asta".equals(risposta.getType()) || risposta.getPorta_multicast() == 0) {
            System.out.println("Impossibile partecipare all'asta.");
            return;
        }

        String indirizzoMulti = risposta.getIndirizzo_multicast();
        int portaMulti = risposta.getPorta_multicast();

        byte[] pk =new  byte[512];
       MulticastSocket socket = new MulticastSocket();

        socket.joinGroup(InetAddress.getByName(indirizzoMulti));

        DatagramPacket outputM=new DatagramPacket(pk,pk.length,InetAddress.getByName(indirizzoMulti),portaMulti);
        DatagramPacket inputM=new DatagramPacket(pk,pk.length);

        MessaggioPresenza p=new MessaggioPresenza(this.username);
        String pres=this.converter.toJson(p,MessaggioPresenza.class);

        outputM.setData(pres.getBytes());

        System.out.println(indirizzoMulti+" "+portaMulti);

        socket.send(outputM);

        System.out.println(socket.toString());

        double prezzoBase = risposta.getPrezzo_base();
        MonitorVincitore monitor = new MonitorVincitore(username, prezzoBase);

        Thread thread = avvioThread(indirizzoMulti, portaMulti, monitor);

        System.out.println("Stai partecipando all'asta! Attendi che vengano raggiunti i partecipanti minimi...");

        // ciclo di attesa del server per far partire l'asta
        boolean astaIniziata = false;
        while (!astaIniziata) {

           String rispostaAttesa = input.readLine(); // risposta dal server

            if (rispostaAttesa != null && rispostaAttesa.equals("start_asta")) {
                astaIniziata = true;
                System.out.println("L'asta è iniziata! Puoi fare la tua offerta.");
            } else {
                System.out.println("In attesa di altri partecipanti per avviare l'asta...");
                // Attendi qualche secondo prima di riprovare
                try {
                    Thread.sleep(2000); // 2 secondi di attesa perno sovraccaricare il server
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


            InetAddress group = InetAddress.getByName(indirizzoMulti);

            System.out.print("Inserisci il valore della tua offerta: ");
            double valore = scanner.nextDouble();
            scanner.nextLine(); // flush newline

            Offerta offerta = new Offerta(username, valore);

            // Serializza in JSON con Gson
            String jsonOfferta = converter.toJson(offerta);

            if (!monitor.isAstaTerminata()){
                byte[] buffer = jsonOfferta.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, portaMulti);
                socket.send(packet);

                System.out.println("Offerta inviata!");
            }
            else System.out.println("offerta non inviata, asta termina");



        System.out.println("Attendo la fine dell'asta....");
//        try {
//            //thread.join(); // il thread tcp si blocca finche il thread non termina
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        System.out.println("L'asta è terminata. Torno al menu principale....");
        socket.close();
        menuDopoLogin();
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
                    System.out.println("Per partecipare ad un'asta inserisci l'ID del determinato prodotto, altrimenti digita 'esci' per tornare al menu principale");

                    String inputUtente = scanner.nextLine().trim();

                    if (inputUtente.equalsIgnoreCase("esci")) {
                        menuDopoLogin();
                        return;
                    }

                    try {
                        idProdotto = Integer.parseInt(inputUtente);
                        if (idProdotto < 0 || !listaIdProdotti.contains(idProdotto)) {
                            System.out.println("Prodotto inesistente, riprova.");
                        } else {
                            valido = true;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Input non valido, riprova.");
                    }

                }
                partecipaAsta(idProdotto);
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void inizializzaAsta() {
        Scanner scanner = new Scanner(System.in);
        DatiAsta da = new DatiAsta();

        System.out.println("inserisci l'id del prodotto");
        da.setIdProdotto(scanner.nextInt());

        String reqJ = this.converter.toJson(da, DatiAsta.class);
        String reply = "", id = "";
        this.output.println(reqJ);

        try {
            reply = this.input.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (reply.contains("erroreCreazioneAsta")) {
            System.out.println("errore nella creazione dell'asta, " +
                    "non possiedi quel prodotto o il prodotto non esiste.");
        } else if (reply.contains("ok")) {
            System.out.println("creazione asta andata a buon fine");

        }

    }

    // Avvia il thread multicast per ricevere offerte in tempo reale
    // ritorna un thread cosi da eseguire le operazioni nel thread chiamante
    public Thread avvioThread(String mulicastIp, int portaMulticast, MonitorVincitore monitor) {
        Thread thread = null;
        try {
            thread = new ThreadLetturaAstaClient(mulicastIp, portaMulticast, monitor);
            thread.start(); // Avvia il thread
        } catch (Exception e) {
            System.out.println("Errore nell'avvio del thread multicast: " + e.getMessage());
        }
        return thread;
    }

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.avvio();
    }
}
