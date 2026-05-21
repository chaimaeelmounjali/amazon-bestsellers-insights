package com.example.amazonbestseller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import java.util.Arrays;

@SpringBootTest
public class BeanDiagnosticTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void checkBeans() {
        System.out.println("--- DIAGNOSTIC START ---");

        boolean serviceFound = context.containsBean("analysePredictiveService");
        System.out.println("Service Bean Found: " + serviceFound);

        boolean controllerFound = context.containsBean("analysePredictiveController");
        System.out.println("Controller Bean Found: " + controllerFound);

        System.out.println("--- CONTROLLERS ---");
        Arrays.stream(context.getBeanDefinitionNames())
                .filter(name -> name.toLowerCase().endsWith("controller"))
                .forEach(System.out::println);

        System.out.println("--- DIAGNOSTIC END ---");

        if (!controllerFound) {
            throw new RuntimeException("Controller not found in context!");
        }
    }
}
