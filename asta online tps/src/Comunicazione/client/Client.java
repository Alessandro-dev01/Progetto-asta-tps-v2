package Comunicazione.client;

import Comunicazione.messagges.*;
import Comunicazione.messagges.login.Login;
import Comunicazione.messagges.login.LoginResponse;
import Comunicazione.messagges.login.LoginResult;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
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
        }

        public void avvio(){

            //this.th.start();
            Scanner scanner=new Scanner(System.in);
            String username =" " , password=" ";
            Gson data=new Gson();
            String mes="";
            int operation=-1;
          

            String reply = "";

            do{
                try {

                        reply=this.input.readLine();

                        System.out.println(reply);

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
                        username=scanner.nextLine();

                        System.out.println("si richiede di inserire la password");
                        password=scanner.nextLine();
                        mes=data.toJson(new Login(username,password));

                        output.println(mes);



                        break;
                    }
                    case 2:{

                        LoginResponse l=data.fromJson(mes,LoginResponse.class);


                        if (l.getEsito().contains("erroreLogin")){
                            System.out.println("login non effetuato, dati errati");

                            LoginResult err=new LoginResult();

                            String StringErr=data.toJson(err);

                            output.println(StringErr);

                        }
                        else if (l.getEsito().contains("okLogin")){
                            System.out.println("login riuscito");

                            LoginResult err=new LoginResult();

                            err.setResult(Result.autorizzato);

                            String StringErr=data.toJson(err);

                            output.println(StringErr);
                        }

                        break;
                    }
                }
            } while (true);


        }

    public static void main(String[] args) {
        Client c=new Client();
        c.avvio();
    }
    }


