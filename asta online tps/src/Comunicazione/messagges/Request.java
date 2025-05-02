package Comunicazione.messagges;

public class Request {
    private String type;

    public Request() {
    }

    public Request(TypeOfMes type) {
        this.type =type.toString();
    }

    public void setType(TypeOfMes type) {
        this.type = type.toString();
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
