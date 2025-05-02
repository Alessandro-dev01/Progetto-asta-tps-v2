package Comunicazione.messagges;

public class MessaggioPresenza {
    private String type;
    private String username;

    public MessaggioPresenza(String username) {
        this.type = TypeOfMes.presenza.toString();
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "MessaggioPresenza{" +
                "type='" + type + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

