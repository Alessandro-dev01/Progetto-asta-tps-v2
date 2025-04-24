package Comunicazione.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ThreadClientMulticast extends Thread{

    private MulticastSocket m;
    private DatagramPacket reciver;

    public ThreadClientMulticast(int port){
        try {
            this.m=new MulticastSocket(port);

            this.m.joinGroup(InetAddress.getByName("230.0.0.1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void run(){

        byte[] pk=new byte[512];
        String msg="";
        this.reciver=new DatagramPacket(pk,pk.length);

        while (!msg.equals("fine")){


            try {
                this.m.receive(reciver);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            msg=new String(reciver.getData(),0,reciver.getLength());


            System.out.println(msg);



            this.reciver=new DatagramPacket(pk,pk.length);


        }


    }




}
