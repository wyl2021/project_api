package com.example.project.init;

import com.example.project.util.TestDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 测试数据初始化器，在Spring Boot应用启动后自动生成测试数据
 */
@Component
public class TestDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private TestDataGenerator testDataGenerator;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("========== 开始生成测试数据 ==========");
        try {
            // 调用测试数据生成器生成所有测试数据
            testDataGenerator.generateAllTestData();
            System.out.println("========== 测试数据生成完成 ==========");
        } catch (Exception e) {
            System.err.println("生成测试数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}