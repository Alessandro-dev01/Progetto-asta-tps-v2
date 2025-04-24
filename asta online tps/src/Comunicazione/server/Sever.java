package Comunicazione.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sever {

    private ServerSocket server;
    private Connection con;
    private String DB_URL = "jdbc:mysql://127.0.0.1/asteOnline";
    private String password = "";
    private String user = "root";

    public Sever(Connection con) {
        //connessione per scambio dati prodotti con il client
        try {
            this.server = new ServerSocket(5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //dati connessioe database
        try {
            this.con = DriverManager.getConnection(this.DB_URL,this.user,this.password);
        } catch (SQLException e) {
            //fare inoltro messaggio d'errore tramite xml al client
            System.out.println("errore con la connessione al server");
        }

    }
}
