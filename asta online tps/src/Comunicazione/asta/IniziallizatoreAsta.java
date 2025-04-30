package Comunicazione.asta;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IniziallizatoreAsta  {


    // Dati per la connessione al database
    private String DB_URL = "jdbc:mysql://127.0.0.1:3306/asteonline";
    private String password = "";
    private String user = "root";



    private ExecutorService executorService;
    private ServerSocket serverAsta;


    public IniziallizatoreAsta() {
       this.executorService = Executors.newCachedThreadPool();
       try {
           serverAsta=new ServerSocket(4000);
       } catch (IOException e) {
          throw new RuntimeException(e);
       }
    }

   public void avvioServerAsta(){
        Socket richiedenteAsta;

        while (true){

            try {
                richiedenteAsta=this.serverAsta.accept();
                System.out.println("mi sono connesso");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.executorService.submit(
                    new GestoreAsta(richiedenteAsta,this.DB_URL,this.password,this.user)
            );
            System.out.println("ho creato il task");
        }
    }

    public static void main(String[] args) {
        IniziallizatoreAsta i=new IniziallizatoreAsta();
        i.avvioServerAsta();
    }

}
