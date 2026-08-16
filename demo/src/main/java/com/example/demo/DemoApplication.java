package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//このクラスをSpring Bootアプリの起点として扱う

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		//SpringBootを起動
		SpringApplication.run(DemoApplication.class, args);
	}

}
