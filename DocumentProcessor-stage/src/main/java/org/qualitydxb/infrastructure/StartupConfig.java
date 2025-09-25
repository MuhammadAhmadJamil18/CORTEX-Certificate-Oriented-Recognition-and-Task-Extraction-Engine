package org.qualitydxb.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Configuration
public class StartupConfig {
    private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);

    @Bean
    public ApplicationRunner folderInitializer() {
        return args -> {
            String baseFolderPath = "src/main/resources";
            String[] foldersToCreate = {"uploads", "logs"};

            for (String folder : foldersToCreate) {
                String folderPath = baseFolderPath + "/" + folder;
                File directory = new File(folderPath);

                if (!directory.exists()) {
                    if (directory.mkdirs()) {
                        logger.info("Directory created successfully: {}", folderPath);
                    } else {
                        logger.error("Failed to create directory: {}", folderPath);
                    }
                } else {
                    logger.warn("Directory already exists: {}", folderPath);
                }
            }
        };
    }
}
