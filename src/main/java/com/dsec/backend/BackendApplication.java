package com.dsec.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}


	public static String getHALJSON()
	{
		return new String("  \"_links\": {\n" +
				"    \"self\": {\n" +
				"      \"href\": \""+ ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUriString() +"\"\n" +
				"    }},");
	}
}
