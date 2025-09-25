package org.qualitydxb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocumentProcessorApplication {
	public static void main(String[] args) {
		SpringApplication.run(DocumentProcessorApplication.class, args);
	}
}
