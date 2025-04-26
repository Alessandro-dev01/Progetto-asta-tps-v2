package Comunicazione.messagges;

public class dataUser {

    private String type;

    private String username;
    private String password;

    public dataUser(String username, String password) {

        this.type=TypeOfMes.login.toString();

        this.username = username;
        this.password = password;
    }

    public String getType() {
        return type.toString();
    }

    public void setType(TypeOfMes type) {
        this.type = type.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Login{" +

                "type=" + type +
                ", username='" + username + '\'' +

                ", password='" + password + '\'' +
                '}';
    }
}
