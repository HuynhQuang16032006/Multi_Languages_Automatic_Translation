package vn.edu.sgu.audiotour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AudiotourApplication {

	public static void main(String[] args) {
		SpringApplication.run(AudiotourApplication.class, args);
	}

}
