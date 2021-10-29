package bsu.fpmi.chat.util;

import bsu.fpmi.chat.model.User;
import org.json.simple.JSONObject;

import java.util.Random;

/**
 * Created by Gennady Trubach on 24.05.2015.
 */
public final class UserUtil {
    public static final String USER = "user";
    private static final String ID = "id";
    public static final String NAME = "name";
    private static final long LIMIT = 10000000000L;

    private UserUtil() {
    }

    private static String generateId() {
        Random random = new Random();
        long currentDate = System.currentTimeMillis();
        return String.valueOf(Math.abs(currentDate * random.nextLong() % LIMIT));
    }

    public static User jsonToNewUser(JSONObject jsonObject) throws NullPointerException {
        Object name = jsonObject.get(NAME);
        if (name == null) {
            throw new NullPointerException();
        }
        return new User(generateId(), (String) name);
    }

    public static User jsonToCurrentUser(JSONObject jsonObject) throws NullPointerException {
        Object id = jsonObject.get(ID);
        Object name = jsonObject.get(NAME);
        if (id == null || name == null) {
            throw new NullPointerException();
        }
        return new User((String) id, (String) name);
    }
}
