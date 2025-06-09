package com.smartinbox.smartinboxjavafx.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {


    public static String extractTextFromPDF(InputStream pdfInputStream) throws IOException {
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    public static Path getUniqueFilePath(String dir, String originalFilename) {
        Path path = Paths.get(dir, originalFilename);

        if (!Files.exists(path)) {
            return path;
        }

        String name = originalFilename;
        String baseName = name;
        String extension = "";

        int dotIndex = name.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }

        int counter = 1;
        while (Files.exists(path)) {
            String newName = String.format("%s_(%d)%s", baseName, counter, extension);
            path = Paths.get(dir, newName);
            counter++;
        }

        return path;
    }
}
