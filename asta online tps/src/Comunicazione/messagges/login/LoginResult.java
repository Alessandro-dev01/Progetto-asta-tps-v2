package Comunicazione.messagges.login;

import Comunicazione.messagges.Result;
import Comunicazione.messagges.TypeOfMes;

public class LoginResult {
    private String type;
    private String result;

    public LoginResult() {
        this.type= TypeOfMes.loginResponse.toString();
        this.result= Result.nonAutorizzato.toString();
    }

    public String getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result.toString();
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "type='" + type + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
