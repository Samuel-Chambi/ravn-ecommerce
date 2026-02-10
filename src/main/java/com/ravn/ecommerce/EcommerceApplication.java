package com.ravn.ecommerce;

import com.ravn.ecommerce.application.config.AppConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

	@Bean
	public CommandLineRunner probConnexion(DataSource dataSource) {
		return args -> {
			try (Connection conn = dataSource.getConnection()) {
				System.out.println("✅ ==========================================");
				System.out.println("✅ Successful connection: " + conn.getCatalog());
				System.out.println("✅ ==========================================");
			} catch (Exception e) {
				System.err.println("❌ Fatal Error: " + e.getMessage());
			}
		};
	}
}
