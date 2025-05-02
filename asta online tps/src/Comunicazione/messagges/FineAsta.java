package Comunicazione.messagges;

public class FineAsta {
    private String type;
    private int id_prodotto;
    private String vincitore;
    private double importo_finale;

    public FineAsta(int id_prodotto, String vincitore, double importo_finale) {
        this.type = TypeOfMes.fine_asta.toString();
        this.id_prodotto = id_prodotto;
        this.vincitore = vincitore;
        this.importo_finale = importo_finale;
    }

    public String getType() {
        return type;
    }

    public double getImporto_finale() {
        return importo_finale;
    }

    public void setImporto_finale(double importo_finale) {
        this.importo_finale = importo_finale;
    }

    public String getVincitore() {
        return vincitore;
    }

    public void setVincitore(String vincitore) {
        this.vincitore = vincitore;
    }

    public int getId_prodotto() {
        return id_prodotto;
    }

    public void setId_prodotto(int id_prodotto) {
        this.id_prodotto = id_prodotto;
    }

    @Override
    public String toString() {
        return "FineAsta{" +
                "type='" + type + '\'' +
                ", id_prodotto=" + id_prodotto +
                ", vincitore='" + vincitore + '\'' +
                ", importo_finale=" + importo_finale +
                '}';
    }
}
