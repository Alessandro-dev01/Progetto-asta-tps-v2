package Comunicazione.client;

import Comunicazione.asta.MonitorVincitore;
import Comunicazione.messagges.Offerta;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;

// Thread dedicato ad ascoltare le offerte inviate dai client tramite multicast durante l'asta
// aggiorna il vincitore attuale usando la classe MonitorVincitore

public class ThreadLetturaAstaClient extends Thread {

    private MulticastSocket socketMulti;
    private String multicastAddress;
    private int porta;
    private MonitorVincitore monitor;
    private Gson converter;

    public ThreadLetturaAstaClient(String address, int port, MonitorVincitore mon) {
        this.monitor = mon;
        this.multicastAddress = address;
        this.porta = port;
        this.converter = new Gson();
        try {
            socketMulti = new MulticastSocket();
            socketMulti.joinGroup(InetAddress.getByName(this.multicastAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {

        byte[] pk = new byte[512];
        DatagramPacket input = new DatagramPacket(pk, pk.length);

        // Il ciclo si interrompe se il monitor segnala la fine dell'asta


        while (!monitor.isAstaIniziata()){

            while (!monitor.isAstaTerminata()) {
                try {
                    this.socketMulti.receive(input);
                    String dati = new String(input.getData(), 0, input.getLength());

                    if (dati.contains("offerta")) {
                        Offerta offerta = this.converter.fromJson(dati, Offerta.class);
                        monitor.aggiornaOfferta(offerta.getUsename(), offerta.getImporto());
                    }
                    else if (dati.contains("fine_asta")){
                        System.out.println("asta termina, il prodotto è stato aggidicato da: "
                        + monitor.getUsername()+ "al prezzo di: "+monitor.getOffertaMassima() );
                    }

                } catch(SocketException e){
                    System.out.println("connessione chiusa ");
                    this.monitor.setInizioAsta(true);
                } catch (IOException e) {
                    System.out.println("errore nell IO");
                    break;
                }

                try {
                    socketMulti.leaveGroup(InetAddress.getByName(multicastAddress));
                    socketMulti.close();
                } catch (IOException e) {
                    System.err.println("Errore durante la chiusura del socket multicast: " + e.getMessage());
                }

                System.out.println("Thread multicast terminato: l'asta è finita.");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }



    }


}

