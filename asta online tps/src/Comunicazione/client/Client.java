package Comunicazione.client;
import Comunicazione.messagges.Login;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.PrintWriter;
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

//            try {
//                this.clientTcp=new Socket(InetAddress.getByName("127.0.0.1"),12121);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//
//            try {
//                this.input=new BufferedReader(new InputStreamReader(this.clientTcp.getInputStream()));
//
//                this.output=new PrintWriter(this.clientTcp.getOutputStream(),true);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            this.th=new ThreadClientMulticast(25001);

        }

        public void avvio(){

            //this.th.start();
            Scanner scanner=new Scanner(System.in);
            String username =" " , password=" ";
            Gson data=new Gson();
            String mes="";
            int operation=-1;

            System.out.println("si richiede di inserire il nome utente");
            username=scanner.nextLine();

            System.out.println("si richiede di inserire la password");
            password=scanner.nextLine();
            mes=data.toJson(new Login(username,password));



            do{
                System.out.println("inserisci 1 per stampare la stringa json");
                operation=scanner.nextInt();
                switch (operation){
                    case 1:{
                        System.out.println(mes);

                        Login p=data.fromJson(mes,Login.class);

                        System.out.println(p.toString());
                        break;
                    }
                    case 2:
                }
            } while (true);





        }

    public static void main(String[] args) {
        Client c=new Client();
        c.avvio();
    }
    }


