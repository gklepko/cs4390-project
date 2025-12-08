// Writes simple log lines to a file inside the logs folder.
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class LogWriter {
        // The name of the log file we will write to (e.g., 2025-12-08-12-30.log)
        private String fileName;

        public LogWriter()
        {
            this.fileName = generateFileName();
        }


        public void writeLog(String msg, String logType) throws IOException
        {
            // Ensure the logs directory exists so file creation won't fail
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // Create a File object pointing to our log file
            File logFile = new File(logDir, fileName);

            // try-with-resources ensures FileWriter is closed automatically
            try(FileWriter writer = new FileWriter(logFile, true)) {
                // Write one line: [TYPE] message\n
                writer.write("[" + logType + "] " + msg + "\n");
            }
        }

        
         //Generate a simple filename using current date and time.
        private String generateFileName() {
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            fileName = "" + currentDate + "-" + currentTime;

            // Shorten length 
            int lastColon = fileName.lastIndexOf(':');
            fileName = fileName.substring(0, lastColon);
            // Replace remaining ':' characters with '-' for a valid filename
            fileName = fileName.replace(':', '-');

            fileName = fileName + ".log";

            return fileName;
        }
    }