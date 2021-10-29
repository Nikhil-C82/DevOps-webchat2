package bsu.fpmi.chat.dao;

import bsu.fpmi.chat.exception.ModifyException;
import bsu.fpmi.chat.model.User;

import java.text.ParseException;
import java.util.List;

/**
 * Created by Gennady Trubach on 22.05.2015.
 */
public interface ObjectDAO<T> {
    void addObject(T object) throws ParseException;

    void updateObject(T object) throws ParseException, ModifyException;

    List<T> getObject() throws ParseException;

    T getObjectById(String id) throws ParseException;

    List<T> getObjectByUser(User user) throws ParseException;
}
