package Comunicazione.client;

import Comunicazione.messagges.*;
import Comunicazione.messagges.dataUser;
import Comunicazione.messagges.Response;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.DoubleSummaryStatistics;
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
        public Client(){


           try {
                this.clientTcp=new Socket(InetAddress.getByName("127.0.0.1"),5000);
           } catch (IOException e) {
                throw new RuntimeException(e);
          }


            try {
                this.input=new BufferedReader(new InputStreamReader(this.clientTcp.getInputStream()));

               this.output=new PrintWriter(this.clientTcp.getOutputStream(),true);
         } catch (IOException e) {
                throw new RuntimeException(e);
            }

           this.th=new ThreadClientMulticast(25001);

            this.converter=new Gson();
        }

        public void avvio(){

            //this.th.start();
            Scanner scanner=new Scanner(System.in);


            String mes="";
            int  menu=-1;

            do {
                System.out.println(
                        "inserisci un delle seguenti opzione: " +"\n"+
                                "1)login, " +"\n"+
                                "2)registrazione" +"\n"+
                                "3)carica oggetti posseduti  "+"\n" +
                                "4)partecipa pasta"
                );
                menu=scanner.nextInt();


                switch (menu){
                    case 1:{
                        login();
                        break;
                    }
                    case 2:{

                        registrazione();
                        break;
                    }

                }
            }while (true);
        }


    public void login(){
            Gson data=new Gson();
            Request re=new Request();
            int operation=-1;
            Scanner scanL=new Scanner(System.in);

            String username =" " , password=" ",mes="";

            re.setType(TypeOfMes.loginRequest);

            String req=this.converter.toJson(re);

            this.output.println(re);


            String reply = "";

            do{
                try {

                    reply=this.input.readLine();

                    if (reply.contains("loginRequest")){
                        operation=1;
                    } else if (reply.contains("loginResponse")) {
                        operation=2;
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                switch (operation){

                    case 1:{

                        System.out.println("si richiede di inserire il nome utente");
                        username=scanL.nextLine();

                        System.out.println("si richiede di inserire la password");
                        password=scanL.nextLine();
                        mes=data.toJson(new dataUser(username,password));

                        output.println(mes);

                        break;
                    }
                    case 2:{
                        if (mes.contains("erroreLogin")){
                            System.out.println("login non effetuato, dati errati");

                            Request err=new Request();

                            err.setType(TypeOfMes.loginRequest);

                            String StringErr=data.toJson(err);

                            output.println(StringErr);

                        }
                        else {
                            System.out.println("login riuscito");
                        }

                        break;
                    }
                }
            } while (true);


        }

    private void registrazione(){

        String password="", mes="",reply="";
        Gson data=new Gson();
        Scanner scanR=new Scanner(System.in);

            do {

                System.out.println("si richiede di inserire il nome utente");
                username=scanR.nextLine();


                System.out.println("si richiede di inserire la password");
                password=scanR.nextLine();

                dataUser d=new dataUser(username,password);

                d.setType(TypeOfMes.registrazione);

                mes=data.toJson(d);

                output.println(mes);

                try {
                    reply=input.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (reply.contains("okRegistrazione")){
                    System.out.println("registrazione avvenuta con successo");
                    break;
                }
                else if (reply.contains("erroreRegistrazione")){
                    System.out.println("errore nella registrazione");
                }

            }while (true);




    }


    public static void main(String[] args) {
        Client c=new Client();
        c.avvio();
    }

    }


