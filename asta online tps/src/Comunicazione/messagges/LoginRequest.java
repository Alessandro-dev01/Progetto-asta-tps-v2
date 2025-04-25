package Comunicazione.messagges;

public class LoginRequest {
    private String type;

    public LoginRequest() {
        this.type = TypeOfMes.loginRequest.toString();
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "type=" + type +
                '}';
    }
}
