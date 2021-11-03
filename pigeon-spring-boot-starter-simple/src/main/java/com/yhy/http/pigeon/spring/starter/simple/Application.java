package com.yhy.http.pigeon.spring.starter.simple;

import com.yhy.http.pigeon.spring.starter.annotation.EnablePigeon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created on 2021-11-03 12:58
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@EnablePigeon
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
