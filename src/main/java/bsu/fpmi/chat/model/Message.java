package bsu.fpmi.chat.model;

import bsu.fpmi.chat.util.MessageUtil;

/**
 * Created by Gennady Trubach on 28.03.2015.
 * FAMCS 2d course 5th group
 */
public class Message {
    private final String ID;
    private String senderName;
    private String messageText;
    private String sendDate;
    private String modifyDate;
    private boolean isDeleted;

    public Message(String ID, String senderName, String messageText, String sendDate, String modifyDate, boolean isDeleted) {
        this.ID = ID;
        this.senderName = senderName;
        this.messageText = messageText;
        this.sendDate = sendDate;
        this.modifyDate = modifyDate;
        this.isDeleted = isDeleted;
    }

    public void setModified() {
        modifyDate = MessageUtil.generateCurrentDate();
    }

    public void delete() {
        isDeleted = true;
        messageText = "deleted";
    }

    public String getID() {
        return ID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSendDate() {
        return sendDate;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(ID)
          .append("\", \"senderName\":\"").append(senderName)
          .append("\", \"messageText\":\"").append(messageText)
          .append("\", \"sendDate\":\"").append(sendDate)
          .append("\", \"modifyDate\":\"").append(modifyDate)
          .append("\", \"isDeleted\":\"").append(isDeleted).append("\"}");
        return sb.toString();
    }

    public String getReadableView() {
        StringBuilder sb = new StringBuilder(getSendDate());
        sb.append(' ')
          .append(senderName)
          .append(" : ")
          .append(getMessageText());
        return sb.toString();
    }
}