package Comunicazione.messagges.login;

import Comunicazione.messagges.Result;
import Comunicazione.messagges.TypeOfMes;

public class LoginResponse {
    private String type;
    private String esito;

    public LoginResponse() {
        this.type= TypeOfMes.loginResponse.toString();
        this.esito= Result.erroreLogin.toString();
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
