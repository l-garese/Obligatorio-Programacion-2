package uy.edu.um.doors;

public class User {
    private int UID;
    private String alias;
    private UserType tipo;

    public enum UserType{
        ADMIN,
        GENERIC
    }

    public User(int UID, String alias, UserType tipo){
        this.UID = UID;
        this.alias = alias;
        this.tipo = tipo;
    }

    public int getUID() {
        return UID;
    }

    public String getAlias() {
        return alias;
    }

    public UserType getTipo() {
        return tipo;
    }
}
