package Comunicazione.asta;

import Comunicazione.messagges.Offerta;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class ThreadLetturaAsta extends Thread{

    private MulticastSocket m;
    private String multicastAddress;
    private int porta;
    private MonitorVincitore monitor;
    private Gson converter;

    public ThreadLetturaAsta(String address, int port, MonitorVincitore mon){
        this.monitor=mon;
        this.multicastAddress=address;
        this.porta=port;
        this.converter=new Gson();
        try {
            m=new MulticastSocket();
            m.joinGroup(InetAddress.getByName(this.multicastAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void run(){

        byte[] pk=new byte[512];

        DatagramPacket input=new DatagramPacket(pk,pk.length);

        while (true){

            try {
                this.m.setSoTimeout(3000);
                this.m.receive(input);

               String dati=new String(input.getData(),0,input.getLength());

               if (dati.contains("offerta")){

                   Offerta o=this.converter.fromJson(dati,Offerta.class);

                   //gestione classe monitor per il vincitore
                   if (this.monitor.getImporto()<o.getImporto()){
                       this.monitor.setUsername(o.getUsename());
                       this.monitor.setImporto(o.getImporto());
                   }

                   input=new DatagramPacket(pk,pk.length);
               }



            } catch (SocketException e) {
                System.out.println("asta finita");
            }catch (IOException e) {
                throw new RuntimeException(e);
            }



        }


    }
}
