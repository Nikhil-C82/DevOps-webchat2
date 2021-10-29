package bsu.fpmi.chat.storage;

import bsu.fpmi.chat.exception.ModifyException;
import bsu.fpmi.chat.model.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static bsu.fpmi.chat.util.ServletUtil.UTF_8;

/**
 * Created by Gennady Trubach on 23.04.2015.
 * FAMCS 2d course 5th group
 */
public final class MessageXMLParser {
    private static final String XML_LOCATION = System.getProperty("user.home") + File.separator + "history.xml";
    private static final String MESSAGES = "messages";
    private static final String MESSAGE = "message";
    private static final String ID = "id";
    private static final String SENDER_NAME = "senderName";
    private static final String MESSAGE_TEXT = "messageText";
    private static final String SEND_DATE = "sendDate";
    private static final String MODIFY_DATE = "modifyDate";
    private static final String DELETED = "isDeleted";

    private MessageXMLParser() {
    }

    public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element rootElement = document.createElement(MESSAGES);
        document.appendChild(rootElement);

        Transformer transformer = getTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(XML_LOCATION));
        transformer.transform(source, result);
    }

    public static synchronized void addMessage(Message message) throws ParserConfigurationException, IOException,
            SAXException, TransformerException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(XML_LOCATION);

        Element rootElement = document.getDocumentElement();
        Element messageElement = document.createElement(MESSAGE);
        rootElement.appendChild(messageElement);
        messageElement.setAttribute(ID, message.getID());

        Element senderNameElement = document.createElement(SENDER_NAME);
        senderNameElement.appendChild(document.createTextNode(message.getSenderName()));
        messageElement.appendChild(senderNameElement);

        Element messageTextElement = document.createElement(MESSAGE_TEXT);
        messageTextElement.appendChild(document.createTextNode(message.getMessageText()));
        messageElement.appendChild(messageTextElement);

        Element sendDateElement = document.createElement(SEND_DATE);
        sendDateElement.appendChild(document.createTextNode(message.getSendDate()));
        messageElement.appendChild(sendDateElement);

        Element modifyDateElement = document.createElement(MODIFY_DATE);
        modifyDateElement.appendChild(document.createTextNode(message.getModifyDate()));
        messageElement.appendChild(modifyDateElement);

        Element deletedElement = document.createElement(DELETED);
        deletedElement.appendChild(document.createTextNode(Boolean.toString(message.isDeleted())));
        messageElement.appendChild(deletedElement);

        Transformer transformer = getTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(XML_LOCATION);
        transformer.transform(source, result);
    }

    public static synchronized List<Message> getMessages() throws XMLStreamException, FileNotFoundException {
        List<Message> messages = new ArrayList<>();
        String id = null;
        String senderName = null;
        String messageText = null;
        String sendDate = null;
        String modifyDate = null;
        boolean isDeleted = false;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new InputStreamReader(
                new FileInputStream(XML_LOCATION), Charset.forName(UTF_8)));
        while (xmlStreamReader.hasNext()) {
            switch (xmlStreamReader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    String eventName = xmlStreamReader.getLocalName();
                    if (MESSAGE.equals(eventName)) {
                        id = xmlStreamReader.getAttributeValue(0);
                    }
                    if (SENDER_NAME.equals(eventName)) {
                        senderName = xmlStreamReader.getElementText().trim();
                    }
                    if (MESSAGE_TEXT.equals(eventName)) {
                        messageText = xmlStreamReader.getElementText().trim();
                    }
                    if (SEND_DATE.equals(eventName)) {
                        sendDate = xmlStreamReader.getElementText().trim();
                    }
                    if (MODIFY_DATE.equals(eventName)) {
                        modifyDate = xmlStreamReader.getElementText().trim();
                    }
                    if (DELETED.equals(eventName)) {
                        isDeleted = Boolean.valueOf(xmlStreamReader.getElementText().trim());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (MESSAGE.equals(xmlStreamReader.getLocalName())) {
                        messages.add(new Message(id, senderName, messageText, sendDate, modifyDate, isDeleted));
                    }
                    break;
            }
        }
        return messages;
    }

    public static synchronized Message updateMessage(Message message) throws ParserConfigurationException, IOException,
            SAXException, XPathExpressionException, TransformerException, NullPointerException, ModifyException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(XML_LOCATION);
        Node messageToUpdate = getNodeById(document, message.getID());
        String senderName = null;
        String sendDate = null;
        if (messageToUpdate != null) {
            NodeList nodeList = messageToUpdate.getChildNodes();
            for (int i = nodeList.getLength() - 1; i >= 0; i--) {
                Node node = nodeList.item(i);

                if (SENDER_NAME.equals(node.getNodeName())) {
                    senderName = node.getTextContent();
                }
                if (MESSAGE_TEXT.equals(node.getNodeName())) {
                    node.setTextContent(message.getMessageText());
                }
                if (SEND_DATE.equals(node.getNodeName())) {
                    sendDate = node.getTextContent();
                }
                if (MODIFY_DATE.equals(node.getNodeName())) {
                    node.setTextContent(message.getModifyDate());
                }
                if (DELETED.equals(node.getNodeName())) {
                    if (Boolean.valueOf(node.getTextContent())) {
                        throw new ModifyException();
                    }
                    node.setTextContent(Boolean.toString(message.isDeleted()));
                }
            }

            Transformer transformer = getTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(XML_LOCATION);
            transformer.transform(source, result);
        } else {
            throw new NullPointerException();
        }
        return new Message(message.getID(), senderName, message.getMessageText(), sendDate, message.getModifyDate(), message.isDeleted());
    }

    private static Node getNodeById(Document document, String id) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpression = xPath.compile("//" + MESSAGE + "[@id='" + id + "']");
        return (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        return transformer;
    }

    public static synchronized boolean isStorageExist() {
        File file = new File(XML_LOCATION);
        return file.exists();
    }
}
