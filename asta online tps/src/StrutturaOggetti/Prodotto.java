package StrutturaOggetti;

public class Prodotto {
    private int id;
    private String nome;
    private String categoria;
    private double prezzoBase;
    private String indirizzoMulticast;
    private int portaMulticast;

    public Prodotto(int id, String nome, String categoria, double prezzoBase, String indirizzoMulticast, int portaMulticast) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.prezzoBase = prezzoBase;
        this.indirizzoMulticast = indirizzoMulticast;
        this.portaMulticast = portaMulticast;
    }

    // Getter e Setter
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrezzoBase() {
        return prezzoBase;
    }

    public String getIndirizzoMulticast() {
        return indirizzoMulticast;
    }

    public int getPortaMulticast() {
        return portaMulticast;
    }
}