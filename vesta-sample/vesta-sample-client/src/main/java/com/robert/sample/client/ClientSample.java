package com.robert.sample.client;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.intf.IdService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientSample {
    public static void main(String[] args) {    // 该配置文件是先从 dubbo 获取服务，然后进行调用
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring/vesta-client-sample.xml");
        IdService idService = (IdService) ac.getBean("idService");

        long id = idService.genId();
        Id ido = idService.expId(id);

        System.out.println(id + ":" + ido);
    }
}
