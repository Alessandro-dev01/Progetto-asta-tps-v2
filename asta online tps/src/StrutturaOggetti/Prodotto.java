package StrutturaOggetti;

public class Prodotto {
    private int id;
    private String nome;
    private String descrizione;
    private double prezzoBase;
    private String indirizzoMulticast;
    private String username;
    private int portaMulticast;
    private String nome_categoria;
    private String stato;

    public Prodotto(int id, String stato, String nome_categoria, int portaMulticast, String username, String indirizzoMulticast, double prezzoBase, String descrizione, String nome) {
        this.id = id;
        this.stato = stato;
        this.nome_categoria = nome_categoria;
        this.portaMulticast = portaMulticast;
        this.username = username;
        this.indirizzoMulticast = indirizzoMulticast;
        this.prezzoBase = prezzoBase;
        this.descrizione = descrizione;
        this.nome = nome;
    }

    public Prodotto() {

    }

    public int getPortaMulticast() {
        return portaMulticast;
    }

    public void setPortaMulticast(int portaMulticast) {
        this.portaMulticast = portaMulticast;
    }

    public String getNome_categoria() {
        return nome_categoria;
    }

    public void setNome_categoria(String nome_categoria) {
        this.nome_categoria = nome_categoria;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIndirizzoMulticast() {
        return indirizzoMulticast;
    }

    public void setIndirizzoMulticast(String indirizzoMulticast) {
        this.indirizzoMulticast = indirizzoMulticast;
    }

    public double getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(double prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        if (stato.equals("venduto") || stato.equals("non_venduto") || stato.equals("in-creazione_asta")) {
            this.stato = stato;
        } else {
            System.out.println("Valore stato non valido.");
        }
    }

    @Override
    public String toString() {
        return "Prodotto{" +
                '\n' + "id=" + id +'\n' +
                ", nome='" + nome + '\n' +
                ", descrizione='" + descrizione + '\n' +
                ", prezzoBase=" + prezzoBase +
                ", indirizzoMulticast='" + indirizzoMulticast + '\n' +
                ", username='" + username + '\n' +
                ", portaMulticast=" + portaMulticast +
                ", nome_categoria='" + nome_categoria + '\n' +
                ", stato='" + stato + '\n' +
                '}';
    }
}