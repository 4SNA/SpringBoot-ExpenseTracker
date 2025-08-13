package com.Project.ExpenseTracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.Project.ExpenseTracker")
public class ExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
	}

	@Bean
	public CommandLineRunner checkControllers(ApplicationContext ctx) {
		return args -> {
			System.out.println("---- Registered Controllers ----");
			String[] controllers = ctx.getBeanNamesForAnnotation(RestController.class);
			for (String controller : controllers) {
				System.out.println(controller);
			}
			System.out.println("-------------------------------");
		};
	}
}
