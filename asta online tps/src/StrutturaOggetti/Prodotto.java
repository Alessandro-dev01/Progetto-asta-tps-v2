package StrutturaOggetti;

public class Prodotto {
    private int id;
    private String nome;
    private String descrizione;
    private double prezzoBase;
    private String indirizzoMulticast;
    private String username;
    private int portaMulticast;
    private int id_categoria;
    private String stato;

    public Prodotto(int id, String stato, int id_categoria, int portaMulticast, String username, String indirizzoMulticast, double prezzoBase, String descrizione, String nome) {
        this.id = id;
        this.stato = stato;
        this.id_categoria = id_categoria;
        this.portaMulticast = portaMulticast;
        this.username = username;
        this.indirizzoMulticast = indirizzoMulticast;
        this.prezzoBase = prezzoBase;
        this.descrizione = descrizione;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public int getId_categoria() {
        return id_categoria;
    }

    public String getStato() {
        return stato;
    }

    public int getPortaMulticast() {
        return portaMulticast;
    }

    public String getIndirizzoMulticast() {
        return indirizzoMulticast;
    }

    public double getPrezzoBase() {
        return prezzoBase;
    }

    public String getUsername() {
        return username;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getNome() {
        return nome;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId_categoria(int id_categoria) {
        this.id_categoria = id_categoria;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPortaMulticast(int portaMulticast) {
        this.portaMulticast = portaMulticast;
    }

    public void setIndirizzoMulticast(String indirizzoMulticast) {
        this.indirizzoMulticast = indirizzoMulticast;
    }

    public void setPrezzoBase(double prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setStato(String stato) {
        if (stato.equals("venduto") || stato.equals("non_venduto") || stato.equals("in-asta")) {
            this.stato = stato;
        } else {
            System.out.println("Valore stato non valido.");
        }
    }
}