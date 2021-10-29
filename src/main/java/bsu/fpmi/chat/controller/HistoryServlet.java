package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.dao.MessageDAO;
import bsu.fpmi.chat.dao.ObjectDAO;
import bsu.fpmi.chat.model.Message;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import static bsu.fpmi.chat.util.MessageUtil.MESSAGES;
import static bsu.fpmi.chat.util.ServletUtil.APPLICATION_JSON;
import static bsu.fpmi.chat.util.ServletUtil.UTF_8;

/**
 * Created by gtrubach on 15.05.2015.
 */
@WebServlet(urlPatterns = "/restore")
public class HistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(HistoryServlet.class.getName());
    private ObjectDAO<Message> messageDAO;

    @Override
    public void init() throws ServletException {
        try {
            logger.info("Initialization");
            this.messageDAO = new MessageDAO();
            for (Message message : messageDAO.getObject()) {
                logger.info(message.getReadableView());
            }
        } catch (ParseException e) {
            logger.error(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Get request");
        try {
            String messages = serverResponse();
            response.setContentType(APPLICATION_JSON);
            response.setCharacterEncoding(UTF_8);
            PrintWriter pw = response.getWriter();
            pw.print(messages);
            pw.flush();
        } catch (ParseException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            logger.error(e);
        }
    }

    private String serverResponse() throws ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, messageDAO.getObject());
        return jsonObject.toJSONString();
    }
}
