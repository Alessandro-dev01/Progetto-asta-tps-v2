package Comunicazione.messagges;

public class RispostaErrore extends Response {
    private String messaggio;

    public RispostaErrore(Result result, TypeOfMes type, String messaggio) {
        super(result, type);
        this.messaggio = messaggio;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    @Override
    public String toString() {
        return "RispostaErrore{" +
                "messaggio='" + messaggio + '\'' +
                '}';
    }
}
