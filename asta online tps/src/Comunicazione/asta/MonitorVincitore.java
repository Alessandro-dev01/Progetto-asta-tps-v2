package Comunicazione.asta;

// classe monitor utilizzata per tracciare l'attuale vincitore dell'asta
// tiene aggiornato il nome dell'utente che ha fatto l'offerta piu alta e l'importo corrispondente

public class MonitorVincitore {
    private String vincitore;
    private double offertaMassima;
    private boolean InizioAsta = false;
    private boolean FineAsta = false;
    private long ultimaOff; // tempo ultima offerta impostato nell'istante corrente
    private final long timeout = 15000;


    public MonitorVincitore(String vincitore, double prezzoBase) {
        this.vincitore = vincitore;
        this.offertaMassima = prezzoBase;
        this.ultimaOff = System.currentTimeMillis();
    }

    public synchronized String getUsername() {
        return vincitore;
    }

    public synchronized void setUsername(String vincitore) {
        this.vincitore = vincitore;
    }

    public synchronized double getOffertaMassima() {
        return offertaMassima;
    }

    public synchronized void setOffertaMassima(double offertaMassima) {
        this.offertaMassima = offertaMassima;
    }

    public synchronized void terminaAsta() {
        InizioAsta = true;
    }

    public synchronized   boolean isAstaTerminata() {
        return FineAsta;
    }
    public synchronized void setFineAsta(boolean fineAsta) {
        FineAsta = fineAsta;
    }

    public synchronized void aggiornaOfferta(String username, double offerta) {
        if (offerta > this.offertaMassima) {
            this.offertaMassima = offerta;
            this.vincitore = username;
            this.ultimaOff = System.currentTimeMillis(); // aggiorno il tempo dell'ultima offerta
            System.out.println("Nuova offerta! Utente: " + username + " con importo: " + offerta);
        } else {
            System.out.println("Offerta rifiutata: troppo bassa");
        }

    }

    // Restituisce true se l'asta è terminata per timeout o se è stata esplicitamente chiusa.
//    public boolean isAstaTerminata() {
//        if (asta) {
//            return true;
//        }
//        if ((System.currentTimeMillis() - ultimaOff) > timeout) {
//            return true;
//        }
//        return false;
//    }

    public synchronized boolean isAstaIniziata() {
        return InizioAsta;
    }

    public synchronized void setInizioAsta(boolean inizioAsta) {
        this.InizioAsta = inizioAsta;
    }

    @Override
    public synchronized String toString() {
        return "dati offerta {" +
                "fatta da='" + vincitore + '\'' +
                ", con prezzo base=" + offertaMassima +
                '}';
    }
}
