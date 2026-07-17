package com.dpms;
import com.dpms.entity.Role;
import com.dpms.entity.User;
import com.dpms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Optional;
@SpringBootApplication
public class DpmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DpmsApplication.class, args);
    }
}