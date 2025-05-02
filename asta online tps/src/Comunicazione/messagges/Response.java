package Comunicazione.messagges;


public class Response {
    private String type;
    private String esito;

    public Response() {
    }

    public Response(Result result, TypeOfMes type) {
        this.type = type.toString();
        this.esito = result.toString();
    }

    public String getType() {
        return type;
    }

    public void setType(TypeOfMes type) {
        this.type = type.toString();
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(Result esito) {
        this.esito = esito.toString();
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "type=" + type +
                ", esito='" + esito + '\'' +
                '}';
    }
}
