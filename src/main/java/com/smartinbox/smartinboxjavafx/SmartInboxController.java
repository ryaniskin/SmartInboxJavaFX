package com.smartinbox.smartinboxjavafx;

import com.smartinbox.smartinboxjavafx.email.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;

public class SmartInboxController {

    private static SmartInboxController instance;

    @FXML
    private TextArea textArea;
    @FXML
    private TextArea logArea;

    public String getRawKeywordInput() {
        return textArea.getText();
    }

    public SmartInboxController() {
        instance = this;
    }

    public static SmartInboxController getInstance() {
        return instance;
    }

    @FXML
    protected void onStartButtonClick() {
        logArea.clear();
        System.out.println("SmartInboxAutomator started...");
        appendLog("SmartInboxAutomator started...");

        String inputText = getRawKeywordInput();

        EmailService service = new EmailService(inputText);
        try {
            service.readUnreadEmails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void appendLog(String message) {
        logArea.appendText(message + "\n");
    }


}