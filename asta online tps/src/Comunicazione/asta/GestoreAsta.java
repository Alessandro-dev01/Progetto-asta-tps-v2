package Comunicazione.asta;

import Comunicazione.messagges.DatiAsta;
import Comunicazione.messagges.Response;
import Comunicazione.messagges.Result;
import Comunicazione.messagges.TypeOfMes;
import StrutturaOggetti.Prodotto;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;

public class GestoreAsta implements Runnable {

    private Connection con;
    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private Gson converter;
    private String datiProdotto;
    private MonitorVincitore mv;
    private ThreadLetturaAsta letturaAsta;

    public GestoreAsta(Socket richiedenteAsta, String DB_URL, String password, String user) {
        this.client = new Socket();
        try {
            this.input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.output = new PrintWriter(this.client.getOutputStream(), true); // Crea gli stream di input/output
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.con = DriverManager.getConnection(DB_URL, user, password);

        } catch (SQLException e) {
            System.err.println("Errore connessione al database: " + e.getMessage());

            this.converter = new Gson();

            try {
                this.datiProdotto=this.input.readLine();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    @Override
    public void run() {

        Prodotto prod=this.converter.fromJson(this.datiProdotto,Prodotto.class);

        this.mv=new MonitorVincitore(prod.getUsername(),prod.getPrezzoBase());

        this.letturaAsta=new ThreadLetturaAsta(prod.getIndirizzoMulticast(),prod.getPortaMulticast(),this.mv);

        String update= "UPDATE prodotto set stato='in_asta' where prodotto.id=?";

        try {
            PreparedStatement stm=this.con.prepareStatement(update);
            stm.setInt(1, prod.getId());
           int res=stm.executeUpdate(update);

            if (res==0){
                Response resp=new Response();

                resp.setType(TypeOfMes.creazione_asta);
                resp.setEsito(Result.erroreCreazioneAsta);

                String jsonResp=this.converter.toJson(resp);

                this.output.println(jsonResp);

                this.client.close();
            }
            else {
                byte[] pk=new byte[512];


                MulticastSocket m=new MulticastSocket();

                //mi connetto all'asta
                m.joinGroup(InetAddress.getByName(prod.getIndirizzoMulticast()));

                DatagramPacket output=new DatagramPacket(pk,pk.length,
                        InetAddress.getByName(prod.getIndirizzoMulticast()),prod.getPortaMulticast());

            }






        }catch (IOException | SQLException e) {
            System.out.println("connessione interotta");
        }


    }

}




