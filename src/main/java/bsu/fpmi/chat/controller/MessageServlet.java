package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.dao.MessageDAO;
import bsu.fpmi.chat.dao.ObjectDAO;
import bsu.fpmi.chat.exception.ModifyException;
import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.proccesor.AsyncProcessor;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static bsu.fpmi.chat.util.MessageUtil.*;
import static bsu.fpmi.chat.util.ServletUtil.getMessageBody;

/**
 * Created by Gennady Trubach on 21.04.2015.
 * FAMCS 2d course 5th group
 */
@WebServlet(urlPatterns = "/chat", asyncSupported = true)
public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MessageServlet.class.getName());
    private ObjectDAO<Message> messageDAO;

    @Override
    public void init() throws ServletException {
        logger.info("Initialization");
        this.messageDAO = new MessageDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Get request");
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(300000);
        AsyncProcessor.addAsyncContext(asyncContext);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Post request");
        String data = getMessageBody(request);
        logger.info("Request data : " + data);
        try {
            JSONObject jsonObject = stringToJson(data);
            Message message = jsonToNewMessage(jsonObject);
            logger.info(message.getReadableView());
            messageDAO.addObject(message);
            AsyncProcessor.notifyAllClients(serverResponse(message));
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
        Message message = null;
        try {
            JSONObject jsonObject = stringToJson(data);
            message = jsonToCurrentMessage(jsonObject);
            message.setModified();
            messageDAO.updateObject(message);
            AsyncProcessor.notifyAllClients(serverResponse(messageDAO.getObjectById(message.getID())));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | java.text.ParseException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid message");
        } catch (ModifyException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Message with id : " + message.getID() + " doesn't exist, was deleted or not modified");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Delete request");
        String data = getMessageBody(request);
        logger.info("Request data : " + data);
        Message message = null;
        try {
            JSONObject jsonObject = stringToJson(data);
            message = jsonToCurrentMessage(jsonObject);
            message.delete();
            messageDAO.updateObject(message);
            AsyncProcessor.notifyAllClients(serverResponse(messageDAO.getObjectById(message.getID())));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | java.text.ParseException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid message");
        } catch (ModifyException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Message with id : " + message.getID() + " doesn't exist, was deleted or not modified");
        }
    }

    private String serverResponse(Message message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, Collections.singletonList(message));
        return jsonObject.toJSONString();
    }
}
