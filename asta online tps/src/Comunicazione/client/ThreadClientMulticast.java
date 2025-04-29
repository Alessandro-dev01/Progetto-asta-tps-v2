package Comunicazione.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ThreadClientMulticast extends Thread {
    private int idProdotto;
    private MulticastSocket socket;
    private InetAddress group;
    private static final int portaMulticast = 6000;
    private static final String multicastIp = "230.0.0.1";
    private double offertaCorr = 0.0;


    public ThreadClientMulticast(int idProdotto, String multicastIp, int portaMulticast) {
        this.idProdotto = idProdotto;
        try {
            this.group = InetAddress.getByName(multicastIp);
            this.socket = new MulticastSocket(portaMulticast);
            this.socket.joinGroup(group);
        } catch (IOException e) {
            throw new RuntimeException("Errore nella creazione del socket multicast: " + e.getMessage());
        }

    }

    public void run() {

        byte[] buffer = new byte[1024];

        while (true) {
            try {

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String mess = new String(packet.getData(), 0, packet.getLength());

                JsonObject jsonOfferta = JsonParser.parseString(mess).getAsJsonObject();

                // visualizzazione dell'offerta ricevuta
                if (jsonOfferta.has("type") && jsonOfferta.get("type").getAsString().equals("offerta")) {
                    String utente = jsonOfferta.get("utente").getAsString();
                    int idProdotto = jsonOfferta.get("id_prodotto").getAsInt();
                    double importo = jsonOfferta.get("importo").getAsDouble();
                    String timestamp = jsonOfferta.get("data_offerta").getAsString();

                    if (importo > offertaCorr) {
                        offertaCorr = importo;
                        System.out.println("Nuova offerta ricevuta:");
                        System.out.println("Utente: " + utente);
                        System.out.println("ID Prodotto: " + idProdotto);
                        System.out.println("Importo: " + importo);
                        System.out.println("data_offerta: " + timestamp);
                    }

                }

                if (jsonOfferta.has("type") && jsonOfferta.get("type").getAsString().equals("fine_asta")) {
                    int id = jsonOfferta.get("id_prodotto").getAsInt();

                    if (id == this.idProdotto) {
                        String vincitore = jsonOfferta.get("vincitore").getAsString();
                        double importoFinale = jsonOfferta.get("importo_finale").getAsDouble();

                        System.out.println("Asta terminata per il prodotto ID " + idProdotto);
                        System.out.println("Vincitore: " + vincitore);
                        System.out.println("Importo Finale: " + importoFinale + " €");

                        break;
                    }
                }

            } catch (IOException e) {
                System.out.println("Errore nella ricezione multicast: " + e.getMessage());
                break;
            }
        }

        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            System.out.println("Errore nella chiusura del socket multicast: " + e.getMessage());
        }
    }


}
