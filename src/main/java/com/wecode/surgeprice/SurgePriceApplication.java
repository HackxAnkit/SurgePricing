package com.wecode.surgeprice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class SurgePriceApplication {

	public static void main(String[] args) {
		System.out.println("Starting SurgepriceApplication");
		SpringApplication.run(SurgePriceApplication.class, args);

	}

}
