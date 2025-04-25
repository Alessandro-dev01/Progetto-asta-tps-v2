package Comunicazione.server;

import Comunicazione.messagges.LoginRequest;
import Comunicazione.messagges.LoginResponse;
import Comunicazione.messagges.Result;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sever {


    //dati connessione db

    private String DB_URL = "jdbc:mysql://127.0.0.1/asteOnline";
    private String password = "";
    private String user = "root";


    // dati connessione client
    private ServerSocket serverSocket;
    private Connection con;
    private Gson converter;
    private BufferedReader input;
    private  PrintWriter output;

    public Sever() {
        //connessione per scambio dati prodotti con il client
        try {
            this.serverSocket = new ServerSocket(5000);

   
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


        this.converter=new Gson();

    }

    public void avvioServer(){

        Socket c;
        try{
           c=this.serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
              this.input=new BufferedReader(new InputStreamReader(c.getInputStream()));

                this.output=new PrintWriter(c.getOutputStream(),true);
            } catch (IOException e) {
                throw new RuntimeException(e);
           }

        loginRequest();

    }

    public void loginRequest(){
        LoginRequest request=new LoginRequest();

        String mes=this.converter.toJson(request);

        output.println(mes);

        String reply="";

        try {
            reply=input.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(reply);


        LoginResponse response=new LoginResponse();

        response.setEsito(Result.erroreLogin);

        String logres=this.converter.toJson(response);
        System.out.println(logres);
        this.output.println(logres);

        System.out.println("ok");

        try {
            String res=this.input.readLine();

            if (res.contains("nonAutorizzato")){
                output.println(this.converter.toJson(request));
            }
            else if(res.contains("autorizzato")) {
                System.out.println("utente loggato");
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        Sever server=new Sever();

        server.avvioServer();

    }
}
