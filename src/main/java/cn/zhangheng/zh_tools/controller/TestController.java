package cn.zhangheng.zh_tools.controller;

import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.service.ConfigureService;
import cn.zhangheng.zh_tools.util.MySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-17 00:42
 */
@Controller
@RequestMapping("/test")
public class TestController {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private ConfigureService configureService;

    @Autowired
    private SettingConfig setting;

    @Autowired
    private MySession mySession;


    @RequestMapping("/test_err")
    private void test3(){
        int i=1/0;
    }
    @ResponseBody
    @RequestMapping("/wait")
    private String wait(@RequestParam(name = "sec",defaultValue = "5")Integer wait,
                        @RequestBody(required = false) String body
    ) throws InterruptedException {
        Thread.sleep(wait*1000);
        String reponse="wait："+wait+"s";
        if (StrUtil.isNotBlank(body))
            reponse+="\n"+body;
        return reponse;
    }

}
