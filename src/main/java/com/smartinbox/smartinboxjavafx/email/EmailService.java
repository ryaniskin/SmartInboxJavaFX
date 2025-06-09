package com.smartinbox.smartinboxjavafx.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.smartinbox.smartinboxjavafx.SmartInboxController;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;



public class EmailService {

    private static final String APPLICATION_NAME = "SmartInboxAutomator";
    private static final String USER = "me";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);

    private final String rawKeywordInput;

    public EmailService(String rawKeywordInput) {
        this.rawKeywordInput = rawKeywordInput;
    }

    private static String getCredentialsFilePath() {
        String baseDir;
        try {
            baseDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get current directory", e);
        }
        return baseDir + File.separator + "credentials.json";
    }

    public static boolean credentialsFileExists() {
        File credentialsFile = new File(getCredentialsFilePath());
        return credentialsFile.exists();
    }

    private static final String CREDENTIALS_FILE_PATH = getCredentialsFilePath();

    public static Gmail getGmailService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(HTTP_TRANSPORT);
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(CREDENTIALS_FILE_PATH));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void readUnreadEmails() throws Exception {
        String downloadDir = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "SmartInbox";
        Files.createDirectories(Paths.get(downloadDir));


        List<String> keywords = Arrays.stream(rawKeywordInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Gmail service = getGmailService();
        ListMessagesResponse response = service.users().messages().list(USER).setQ("is:unread has:attachment").execute();
        List<Message> messages = response.getMessages();
        if (messages == null || messages.isEmpty()) {
            System.out.println("No unread emails with attachments found.");
        } else {
            for (Message message : messages) {
                Message fullMessage = service.users().messages().get(USER, message.getId()).execute();
                com.smartinbox.smartinboxjavafx.processor.PDFProcessor.processEmail(service, fullMessage, keywords, downloadDir);
            }

        }
    }
}
