import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class LogWriter {
        private String fileName;

        public LogWriter()
        {
            this.fileName = generateFileName();
        }

        public void writeLog(String msg, String logType) throws IOException
        {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, fileName);

            try(FileWriter writer = new FileWriter(logFile, true)) {
                writer.write("[" + logType + "] " + msg + "\n");
            }
        }

        private String generateFileName() {
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            fileName = "" + currentDate + "-" + currentTime;

            // Shorten length
            int lastColon = fileName.lastIndexOf(':');
            fileName = fileName.substring(0, lastColon);
            fileName = fileName.replace(':', '-');

            fileName = fileName + ".log";

            return fileName;
        }
    }