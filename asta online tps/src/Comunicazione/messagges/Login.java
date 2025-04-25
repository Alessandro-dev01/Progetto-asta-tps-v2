package Comunicazione.messagges;

public class Login {
    private TypeOfMes type;
    private String username;
    private String password;

    public Login(String username, String password) {
        this.type=TypeOfMes.login;
        this.username = username;
        this.password = password;
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
