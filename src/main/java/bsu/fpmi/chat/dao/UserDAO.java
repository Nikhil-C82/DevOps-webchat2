package bsu.fpmi.chat.dao;

import bsu.fpmi.chat.db.ConnectionManager;
import bsu.fpmi.chat.exception.ModifyException;
import bsu.fpmi.chat.model.User;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Gennady Trubach on 24.05.2015.
 */
public class UserDAO implements ObjectDAO<User> {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static Logger logger = Logger.getLogger(UserDAO.class.getName());

    @Override
    public void addObject(User user) throws ParseException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("INSERT INTO users (id, name) VALUES (?, ?)");
            preparedStatement.setString(1, user.getID());
            preparedStatement.setString(2, user.getName());
            preparedStatement.executeUpdate();
            logger.info("Added user " + user.getName());
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
        }
    }

    @Override
    public void updateObject(User user) throws ParseException, ModifyException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?");
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getID());
            preparedStatement.executeUpdate();
            logger.info("Updated user with id" + user.getID());
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
        }
    }

    @Override
    public List<User> getObject() throws ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getObjectById(String id) throws ParseException {
        User user = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE users.id = ?");
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String userId = resultSet.getString(ID);
                String name = resultSet.getString(NAME);
                user = new User(userId, name);
                logger.info("Get user" + user.getName());
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
        return user;
    }

    @Override
    public List<User> getObjectByUser(User user) throws ParseException {
        User fullUser = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE name = ?");
            preparedStatement.setString(1, user.getName());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String id = resultSet.getString(ID);
                String name = resultSet.getString(NAME);
                fullUser = new User(id, name);
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
        return Collections.singletonList(fullUser);
    }
}
