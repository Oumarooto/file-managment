package com.jokers.file_managment;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Arrays;

@SpringBootApplication
@ConfigurationPropertiesScan        // Indispensable pour détecter le record ci-dessus
public class FileManagmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileManagmentApplication.class, args);

        System.out.println("System OK");
	}

}
