package Comunicazione.messagges;

public class PartecipazioneRequest extends Request{
    private int id_prodotto;

    public PartecipazioneRequest(int id_prodotto) {
        super(TypeOfMes.partecipa_asta);
        this.id_prodotto = id_prodotto;
    }

    public int getId_prodotto() {
        return id_prodotto;
    }

    public void setId_prodotto(int id_prodotto) {
        this.id_prodotto = id_prodotto;
    }

    @Override
    public String toString() {
        return "PartecipazioneRequest{" +
                "id_prodotto=" + id_prodotto +
                '}';
    }
}
