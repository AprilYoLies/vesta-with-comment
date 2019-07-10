package com.robert.vesta.sample.embed;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.intf.IdService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EmbedSample {
    public static void main(String[] args) {    // 该配置文件是直接调用 id 生成服务
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring/vesta-service-sample.xml");
        IdService idService = (IdService) ac.getBean("idService");

        long id = idService.genId();
        Id ido = idService.expId(id);

        System.out.println(id + ":" + ido);
    }
}
