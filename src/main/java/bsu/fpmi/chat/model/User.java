package bsu.fpmi.chat.model;

/**
 * Created by Gennady Trubach on 24.05.2015.
 */
public class User {
    private final String ID;
    private String name;

    public User(String ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(ID)
          .append("\", \"name\":\"").append(name).append("\"}");
        return sb.toString();
    }
}
