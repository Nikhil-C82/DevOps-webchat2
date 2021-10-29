package bsu.fpmi.chat.util;


import bsu.fpmi.chat.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by Gennady Trubach on 21.04.2015.
 * FAMCS 2d course 5th group
 */
public final class MessageUtil {
    public static final String MESSAGES = "messages";
    private static final String ID = "id";
    private static final String SENDER_NAME = "senderName";
    private static final String MESSAGE_TEXT = "messageText";
    private static final String NOT_MODIFIED = "not modified";
    private static final String DELETED = "isDeleted";
    private static final long LIMIT = 10000000000L;

    private MessageUtil() {
    }

    private static String generateId() {
        Random random = new Random();
        long currentDate = System.currentTimeMillis();
        return String.valueOf(Math.abs(currentDate * random.nextLong() % LIMIT));
    }

    public static String generateCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
        return dateFormat.format(new Date());
    }

    public static JSONObject stringToJson(String data) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(data.trim());
    }

    public static Message jsonToNewMessage(JSONObject jsonObject) throws NullPointerException{
        Object senderName = jsonObject.get(SENDER_NAME);
        Object messageText = jsonObject.get(MESSAGE_TEXT);
        if (senderName == null || messageText == null) {
            throw new NullPointerException();
        }
        return new Message(generateId(), (String) senderName, (String) messageText, generateCurrentDate(), NOT_MODIFIED,
                Boolean.FALSE);
    }

    public static Message jsonToCurrentMessage(JSONObject jsonObject) throws NullPointerException{
        Object id = jsonObject.get(ID);
        Object messageText = jsonObject.get(MESSAGE_TEXT);
        Object modifyDate = jsonObject.get(DELETED);
        if (modifyDate == null) {
            modifyDate = NOT_MODIFIED;
        }
        if (id == null) {
            throw new NullPointerException();
        }
        return new Message((String) id, null, (String) messageText, null, (String) modifyDate, Boolean.FALSE);
    }
}
