package com.example.demo;

import com.example.demo.dao.UserDOMapper;
import com.example.demo.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//maven install之后如果再次install 会无法找到启动类（如当前项目的DempApplication）的bean
@SpringBootApplication(scanBasePackages = {"com.example.demo"})
@RestController
@MapperScan("com.example.demo.dao")
@RequestMapping("/")
public class DemoApplication {
    @Autowired
    private UserDOMapper userDOMapper;
    @RequestMapping("/home")
    public String home(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if(userDO==null){
            return "user does't exist.";
        }
        return userDO.getName();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
