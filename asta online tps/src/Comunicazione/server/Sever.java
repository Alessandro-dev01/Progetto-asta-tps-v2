package Comunicazione.server;

import Comunicazione.messagges.TypeOfMes;
import Comunicazione.messagges.dataUser;
import Comunicazione.messagges.Request;
import Comunicazione.messagges.Response;
import Comunicazione.messagges.Result;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;


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
        String mes;
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


        do {
            try {
                mes=input.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println(mes);

            if (mes.contains("loginRequest")){
                System.out.println("sono nel login");
                loginRequest();
            }
            else if (mes.contains("autorizzato")){
                System.out.println("ok");
            }
        }while (true);

    }

    public void loginRequest(){
        Request request=new Request();

        request.setType(TypeOfMes.loginRequest);

        String mes=this.converter.toJson(request);

        output.println(mes);

        String reply="";

        try {
            reply=input.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(reply);

        dataUser log=this.converter.fromJson(reply, dataUser.class);

        //query per trovare l'utente
        String queryUtente="SELECT * from utente u where u.username= '"
                +log.getUsername()+"' AND u.password='"+log.getPassword()+"'";

        //prendo tutti i prodotti
        Statement stm;
        try {
           stm=this.con.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ResultSet resQuery;
        try {
            resQuery=stm.executeQuery(queryUtente);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try {
            if(resQuery.next()){
                Response response=new Response();

                response.setType(TypeOfMes.loginResponse);
                response.setEsito(Result.okLogin);

                String logres=this.converter.toJson(response);
                this.output.println(logres);
            }
            else {
                Response response=new Response();

                response.setType(TypeOfMes.loginResponse);
                response.setEsito(Result.erroreLogin);

                String logres=this.converter.toJson(response);
                System.out.println(logres);
                this.output.println(logres);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        Sever server=new Sever();

        server.avvioServer();

    }
}
