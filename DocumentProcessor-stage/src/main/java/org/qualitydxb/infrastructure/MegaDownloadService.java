package org.qualitydxb.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MegaDownloadService {

    public void downloadFile(String megaLink, String destinationDirectory) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("megadl", megaLink, "--path", destinationDirectory);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Error downloading file from MEGA, exit code: " + exitCode);
        }
    }
}
