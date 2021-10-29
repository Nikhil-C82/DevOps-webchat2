package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.dao.MessageDAO;
import bsu.fpmi.chat.dao.ObjectDAO;
import bsu.fpmi.chat.dao.UserDAO;
import bsu.fpmi.chat.exception.ModifyException;
import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.User;
import bsu.fpmi.chat.proccesor.AsyncProcessor;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static bsu.fpmi.chat.util.MessageUtil.MESSAGES;
import static bsu.fpmi.chat.util.MessageUtil.stringToJson;
import static bsu.fpmi.chat.util.ServletUtil.*;
import static bsu.fpmi.chat.util.UserUtil.*;

/**
 * Created by Gennady Trubach on 23.05.2015.
 */
@WebServlet(urlPatterns = "/user")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(UserServlet.class.getName());
    private ObjectDAO<User> userDAO;
    private ObjectDAO<Message> messageDAO;

    @Override
    public void init() throws ServletException {
        logger.info("Initialization");
        userDAO = new UserDAO();
        messageDAO = new MessageDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Get request");
        String name = request.getParameter(NAME);
        logger.info(name);
        try {
            String messages = serverUserResponse(new User("", name));
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            PrintWriter pw = response.getWriter();
            pw.print(messages);
            pw.flush();
        } catch (java.text.ParseException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Post request");
        String data = getMessageBody(request);
        logger.info("Request data : " + data);
        try {
            JSONObject jsonObject = stringToJson(data);
            User user = jsonToNewUser(jsonObject);
            if (userDAO.getObjectByUser(user).get(0) == null) {
                userDAO.addObject(user);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | java.text.ParseException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid message");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Put request");
        String data = getMessageBody(request);
        logger.info("Request data : " + data);
        try {
            JSONObject jsonObject = stringToJson(data);
            User user = jsonToCurrentUser(jsonObject);
            if (userDAO.getObjectByUser(user).get(0) == null) {
                userDAO.updateObject(user);
                AsyncProcessor.notifyAllClients(serverMessageResponse(userDAO.getObjectById(user.getID())));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                logger.error("User with name " + user.getName() + " have already exist!");
            }
        } catch (ParseException | java.text.ParseException | NullPointerException | ModifyException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid user");
        }
    }

    private String serverUserResponse(User user) throws java.text.ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(USER, userDAO.getObjectByUser(user).get(0));
        return jsonObject.toJSONString();
    }

    private String serverMessageResponse(User user) throws java.text.ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, messageDAO.getObjectByUser(user));
        return jsonObject.toJSONString();
    }
}
