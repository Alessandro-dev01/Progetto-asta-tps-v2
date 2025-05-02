package Comunicazione.messagges;

public class PartecipazioneResponse extends Response {
    private String indirizzo_multicast;
    private int porta_multicast;
    private double prezzo_base;

    public PartecipazioneResponse(String indirizzo_multicast, int porta_multicast, double prezzo_base) {
        super.setType(TypeOfMes.partecipa_asta);
        this.indirizzo_multicast = indirizzo_multicast;
        this.porta_multicast = porta_multicast;
        this.prezzo_base = prezzo_base;
    }

    public String getIndirizzo_multicast() {
        return indirizzo_multicast;
    }

    public void setIndirizzo_multicast(String indirizzo_multicast) {
        this.indirizzo_multicast = indirizzo_multicast;
    }

    public int getPorta_multicast() {
        return porta_multicast;
    }

    public void setPorta_multicast(int porta_multicast) {
        this.porta_multicast = porta_multicast;
    }

    public double getPrezzo_base() {
        return prezzo_base;
    }

    public void setPrezzo_base(double prezzo_base) {
        this.prezzo_base = prezzo_base;
    }

    @Override
    public String toString() {
        return "PartecipazioneResponse{" +
                "indirizzo_multicast='" + indirizzo_multicast + '\'' +
                ", porta_multicast=" + porta_multicast +
                ", prezzo_base=" + prezzo_base +
                '}';
    }
}
