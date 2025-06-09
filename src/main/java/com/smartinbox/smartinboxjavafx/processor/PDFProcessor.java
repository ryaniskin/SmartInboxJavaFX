package com.smartinbox.smartinboxjavafx.processor;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.smartinbox.smartinboxjavafx.SmartInboxController;
import com.smartinbox.smartinboxjavafx.utils.FileUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;


import static com.smartinbox.smartinboxjavafx.utils.FileUtils.extractTextFromPDF;

public class PDFProcessor {



    public static void processEmail(Gmail service, Message message, List<String> keywords, String downloadDir) throws IOException {
        SmartInboxController.getInstance().appendLog("\nProcessing email ID: " + message.getId());

        Message fullMessage = service.users().messages()
                .get("me", message.getId())
                .setFormat("full")
                .execute();

        MessagePart payload = fullMessage.getPayload();

        if (payload.getParts() == null) {
            SmartInboxController.getInstance().appendLog("→ No attachments found.");
            return;
        }

        for (MessagePart part : payload.getParts()) {
            if ("application/pdf".equals(part.getMimeType()) &&
                    part.getFilename() != null &&
                    !part.getFilename().isEmpty()) {

                String filename = part.getFilename();
                SmartInboxController.getInstance().appendLog("Found PDF: " + filename);

                String attachmentId = part.getBody().getAttachmentId();
                if (attachmentId == null) {
                    SmartInboxController.getInstance().appendLog("→ Attachment has no ID.");
                    continue;
                }

                MessagePartBody attachmentBody = service.users().messages().attachments()
                        .get("me", message.getId(), attachmentId)
                        .execute();

                byte[] data = Base64.decodeBase64(attachmentBody.getData());

                try (InputStream pdfStream = new ByteArrayInputStream(data)) {
                    String content = extractTextFromPDF(pdfStream);
                    String lowerContent = content == null ? "" : content.toLowerCase();

                    boolean allMatch = keywords.stream()
                            .allMatch(keyword -> lowerContent.contains(keyword.toLowerCase()));

                    if (allMatch) {
                        Path filePath = FileUtils.getUniqueFilePath(downloadDir, filename);
                        Files.write(filePath, data);
                        SmartInboxController.getInstance().appendLog("✅ Saved matching PDF: " + filePath);
                    } else {
                        SmartInboxController.getInstance().appendLog("→ PDF does not match all keywords.");
                    }

                } catch (IOException e) {
                    SmartInboxController.getInstance().appendLog("Error reading PDF " + filename + ": " + e.getMessage());
                }
            }
        }
    }

}
