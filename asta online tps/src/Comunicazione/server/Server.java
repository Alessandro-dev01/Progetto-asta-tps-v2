package Comunicazione.server;

import java.io.IOException;
import java.net.*;
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

            this.executorService.submit(new AstaClientHandler(client, this.DB_URL, this.password, this.user));


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
