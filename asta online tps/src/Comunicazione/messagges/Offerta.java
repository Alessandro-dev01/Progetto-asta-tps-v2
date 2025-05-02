package Comunicazione.messagges;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Offerta {
    private String type;

    private String username;
    private double importo;
    private String data_offerta;

    public Offerta(String username, double importo) {
        this.type = TypeOfMes.offerta.toString();
        this.username = username;
        this.importo = importo;
        this.data_offerta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
        return username;
    }

    public void setUsename(String usename) {
        this.username = usename;
    }

    public String getData_offerta() {
        return data_offerta;
    }

    public void setData_offerta(String data_offerta) {
        this.data_offerta = data_offerta;
    }

    @Override
    public String toString() {
        return "Offerta{" +
                "type='" + type + '\'' +
                ", username='" + username + '\'' +
                ", importo=" + importo +
                ", data_offerta='" + data_offerta + '\'' +
                '}';
    }
}
