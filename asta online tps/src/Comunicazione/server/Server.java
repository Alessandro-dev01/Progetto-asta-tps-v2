package Comunicazione.server;

import Comunicazione.client.ThreadClientMulticast;
import Comunicazione.messagges.*;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {


    // Dati per la connessione al database
    private String DB_URL = "jdbc:mysql://127.0.0.1:3306/asteonline";
    private String password = "";
    private String user = "root";

    // Dati per la connessione con il client
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public Server() throws IOException {
        this.executorService = Executors.newCachedThreadPool();

        try {
            // Inizializza il ServerSocket per comunicare con i client
            this.serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo principale per l'avvio del server
    public void avvioServer() throws IOException {
        Socket client;

        do {
            try {
                client = this.serverSocket.accept(); // Aspetta che un client si connetta
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.executorService.submit(new RunnableAsta(client, this.DB_URL, this.password, this.user));


        } while (true); // Ciclo continuo per gestire le comunicazioni
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.avvioServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
