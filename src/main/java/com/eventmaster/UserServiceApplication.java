package com.eventmaster;

import com.eventmaster.controller.UserController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserServiceApplication {
    public static void main(String[] args){
        ApplicationContext context = new ClassPathXmlApplicationContext("C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/eventmaster/WEB-INF/applicationContext.xml");

        UserController userController = context.getBean(UserController.class);
    }
}
