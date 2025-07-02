package com.github.Atgsasakazh5.my_ec_site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MyEcSiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyEcSiteApplication.class, args);
	}

}
