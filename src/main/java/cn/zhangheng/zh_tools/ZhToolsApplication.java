package cn.zhangheng.zh_tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan
@ServletComponentScan("cn.zhangheng.zh_tools.filter")
//@ConfigurationPropertiesScan
@EnableScheduling // 开启定时任务功能
public class ZhToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhToolsApplication.class, args);
    }


}
