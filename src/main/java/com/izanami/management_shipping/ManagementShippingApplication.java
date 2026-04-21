package com.izanami.management_shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class ManagementShippingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagementShippingApplication.class, args);
	}

}
