package Comunicazione.messagges;

public class Response {
    private String type;
    private String esito;

    public Response() {
        this.type= TypeOfMes.loginResponse.toString();
        this.esito= Result.erroreLogin.toString();
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
