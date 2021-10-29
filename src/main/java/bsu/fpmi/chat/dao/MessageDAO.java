package bsu.fpmi.chat.dao;

import bsu.fpmi.chat.db.ConnectionManager;
import bsu.fpmi.chat.exception.ModifyException;
import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.User;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Gennady Trubach on 22.05.2015.
 */
public class MessageDAO implements ObjectDAO<Message> {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String SEND_DATE = "send_date";
    private static final String MODIFY_DATE = "modify_date";
    private static final String NOT_MODIFIED = "not modified";
    private static final String DELETED = "deleted";
    private static Logger logger = Logger.getLogger(MessageDAO.class.getName());

    @Override
    public void addObject(Message message) throws ParseException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("SELECT id from users WHERE name = ?");
            preparedStatement.setString(1, message.getSenderName());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                preparedStatement = connection.prepareStatement("INSERT INTO messages (id, user_id, text, send_date, modify_date, deleted) VALUES (?, ?, ?, ?, ?, ?)");
                preparedStatement.setString(1, message.getID());
                preparedStatement.setString(2, resultSet.getString(ID));
                preparedStatement.setString(3, message.getMessageText());
                preparedStatement.setTimestamp(4, formatDbDate(message.getSendDate()));
                preparedStatement.setTimestamp(5, formatDbDate(message.getModifyDate()));
                preparedStatement.setBoolean(6, message.isDeleted());
                preparedStatement.executeUpdate();
                logger.info("Added message " + message.getReadableView());
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void updateObject(Message message) throws ParseException, ModifyException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("SELECT deleted FROM messages WHERE id = ?");
            preparedStatement.setString(1, message.getID());
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next() || resultSet.getBoolean(DELETED)) {
                throw new ModifyException();
            }
            preparedStatement = connection.prepareStatement("UPDATE messages SET text = ?, modify_date = ?, deleted = ? WHERE id = ?");
            preparedStatement.setString(1, message.getMessageText());
            preparedStatement.setTimestamp(2, formatDbDate(message.getModifyDate()));
            preparedStatement.setBoolean(3, message.isDeleted());
            preparedStatement.setString(4, message.getID());
            preparedStatement.executeUpdate();
            logger.info("Updated message with id" + message.getID());
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public List<Message> getObject() throws ParseException {
        List<Message> messages = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM messages JOIN users ON users.id = messages.user_id ORDER BY messages.send_date");
            while (resultSet.next()) {
                String id = resultSet.getString(ID);
                String name = resultSet.getString(NAME);
                String text = resultSet.getString(TEXT);
                String sendDate = formatStringDate(resultSet.getTimestamp(SEND_DATE));
                String modifyDate = formatStringDate(resultSet.getTimestamp(MODIFY_DATE));
                boolean isDeleted = resultSet.getBoolean(DELETED);
                messages.add(new Message(id, name, text, sendDate, modifyDate, isDeleted));
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return messages;
    }

    @Override
    public Message getObjectById(String id) throws ParseException {
        Message message = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM messages JOIN users ON users.id = messages.user_id WHERE messages.id = ?");
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString(NAME);
                String text = resultSet.getString(TEXT);
                String sendDate = formatStringDate(resultSet.getTimestamp(SEND_DATE));
                String modifyDate = formatStringDate(resultSet.getTimestamp(MODIFY_DATE));
                boolean isDeleted = resultSet.getBoolean(DELETED);
                message = new Message(id, name, text, sendDate, modifyDate, isDeleted);
                logger.info("Get message" + message.getReadableView());
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return message;
    }

    @Override
    public List<Message> getObjectByUser(User user) throws ParseException {
        List<Message> messages = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM messages JOIN users ON users.id = messages.user_id WHERE messages.user_id = ? ORDER BY messages.send_date");
            preparedStatement.setString(1, user.getID());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString(ID);
                String name = resultSet.getString(NAME);
                String text = resultSet.getString(TEXT);
                String sendDate = formatStringDate(resultSet.getTimestamp(SEND_DATE));
                String modifyDate = formatStringDate(resultSet.getTimestamp(MODIFY_DATE));
                boolean isDeleted = resultSet.getBoolean(DELETED);
                messages.add(new Message(id, name, text, sendDate, modifyDate, isDeleted));
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return messages;
    }

    private Timestamp formatDbDate(String date) throws ParseException {
        if (!NOT_MODIFIED.equals(date)) {
            SimpleDateFormat oldSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss");
            SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            newSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return Timestamp.valueOf(newSimpleDateFormat.format(oldSimpleDateFormat.parse(date)));
        }
        return null;
    }

    private String formatStringDate(Timestamp timestamp) throws ParseException {
        if (timestamp == null) {
            return NOT_MODIFIED;
        }
        SimpleDateFormat newSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss");
        SimpleDateFormat oldSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        oldSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return newSimpleDateFormat.format(oldSimpleDateFormat.parse(timestamp.toString()));
    }
}
