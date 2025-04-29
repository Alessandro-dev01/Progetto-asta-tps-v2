package Comunicazione.messagges;

public class DatiAsta {
    private String type;
    private int idProdotto;

    public DatiAsta() {
        this.type = TypeOfMes.creazione_asta.toString();
        this.idProdotto =0;
    }

    public String getType() {
        return type;
    }

    public void setIdProdotto(int idProdotto) {
        this.idProdotto = idProdotto;
    }

    public int getIdProdotto() {
        return idProdotto;
    }

    @Override
    public String toString() {
        return "DatiAsta{" +
                "type='" + type + '\'' +
                ", idProdotto=" + idProdotto +
                '}';
    }
}
