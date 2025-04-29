package Comunicazione.asta;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IniziallizatoreAsta extends Thread {


    // Dati per la connessione al database
    private String DB_URL = "jdbc:mysql://127.0.0.1:3306/asteonline";
    private String password = "";
    private String user = "root";



    private ExecutorService executorService;
    private ServerSocket serverAsta;


    public IniziallizatoreAsta() {
        this.executorService = Executors.newCachedThreadPool();
        try {
            serverAsta=new ServerSocket(5001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Socket richiedenteAsta;

        while (true){

            try {
                richiedenteAsta=this.serverAsta.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.executorService.submit(
                    new GestoreAsta(richiedenteAsta,this.DB_URL,this.password,this.user)
            );
        }
    }
}
