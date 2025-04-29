package Comunicazione.messagges;

public class Offerta {
    private String type;

    private String usename;
    private double importo;

    public Offerta(String usename, double importo) {
        this.type = TypeOfMes.offerta.toString();
        this.usename = usename;
        this.importo = importo;
    }

    public String getType() {
        return type;
    }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) {
        this.importo = importo;
    }

    public String getUsename() {
        return usename;
    }

    public void setUsename(String usename) {
        this.usename = usename;
    }

    @Override
    public String toString() {
        return "Offerta{" +
                "type='" + type + '\'' +
                ", usename='" + usename + '\'' +
                ", importo=" + importo +
                '}';
    }
}
