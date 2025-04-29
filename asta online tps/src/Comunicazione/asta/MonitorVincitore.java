package Comunicazione.asta;

public class MonitorVincitore {
    private String username;
    private double importo;


    public MonitorVincitore(String username, double importo) {
        this.username = username;
        this.importo = importo;
    }

    public synchronized String getUsername() {
        return username;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized double getImporto() {
        return importo;
    }

    public synchronized void setImporto(double importo) {
        this.importo = importo;
    }

    @Override
    public synchronized String toString() {
        return "dati offerta {" +
                "fatta da='" + username + '\'' +
                ", con prezzo base=" + importo +
                '}';
    }
}
