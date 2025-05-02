package Comunicazione.asta;

import Comunicazione.messagges.Offerta;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

// Thread dedicato ad ascoltare le offerte inviate dai client tramite multicast durante l'asta
// aggiorna il vincitore attuale usando la classe MonitorVincitore

public class ThreadLetturaAsta extends Thread {

    private MulticastSocket socketMulti;
    private String multicastAddress;
    private int porta;
    private MonitorVincitore monitor;
    private Gson converter;

    public ThreadLetturaAsta(String address, int port, MonitorVincitore mon) {
        this.monitor = mon;
        this.multicastAddress = address;
        this.porta = port;
        this.converter = new Gson();
        try {
            socketMulti = new MulticastSocket();
            socketMulti.joinGroup(InetAddress.getByName(this.multicastAddress));
            socketMulti.setSoTimeout(6000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {

        byte[] pk = new byte[512];
        DatagramPacket input = new DatagramPacket(pk, pk.length);

        // Il ciclo si interrompe se il monitor segnala la fine dell'asta
        while (!monitor.isAstaTerminata()) {
            try {
                this.socketMulti.receive(input);
                String dati = new String(input.getData(), 0, input.getLength());

                if (dati.contains("offerta")) {
                    Offerta offerta = this.converter.fromJson(dati, Offerta.class);
                    monitor.aggiornaOfferta(offerta.getUsename(), offerta.getImporto());
                }

            } catch (SocketException e) {
                System.out.println("asta finita");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                socketMulti.leaveGroup(InetAddress.getByName(multicastAddress));
                socketMulti.close();
            } catch (IOException e) {
                System.err.println("Errore durante la chiusura del socket multicast: " + e.getMessage());
            }

            System.out.println("Thread multicast terminato: l'asta è finita.");
        }


    }


}

