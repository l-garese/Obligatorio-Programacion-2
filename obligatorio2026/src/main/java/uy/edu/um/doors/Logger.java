package uy.edu.um.doors;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Logger {
    private final String fileName;

    public Logger() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = LocalDateTime.now().format(formatter);
        this.fileName = "DOORS_PROCESS_LOG_" + date;
    }

    public void write(String message) {
        try {
            // true = append, no sobreescribe el archivo
            FileWriter fw = new FileWriter(fileName, true);
            fw.write(message);
            fw.close();
        } catch (IOException e) {
            System.out.println("Error writing to log: " + e.getMessage());
        }
    }

    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public String getTimestamp() {
        return getCurrentTimestamp();
    }
}
