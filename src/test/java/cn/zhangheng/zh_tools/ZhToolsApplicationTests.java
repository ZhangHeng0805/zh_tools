package cn.zhangheng.zh_tools;

import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.dao.AndroidLogDao;
import cn.zhangheng.zh_tools.dao.IPBlackDao;
import cn.zhangheng.zh_tools.dao.VisitorDao;
import cn.zhangheng.zh_tools.dao.WebLogDao;
import cn.zhangheng.zh_tools.service.ConfigureService;
import cn.zhangheng.zh_tools.service.EmailService;
import cn.zhangheng.zh_tools.service.TimingService;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;

@SpringBootTest
@AutoConfigureMockMvc
class ZhToolsApplicationTests {

    @Autowired
    private SettingConfig setting;
    @Autowired
    private TimingService timingService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private VisitorDao visitorDao;
    @Autowired
    private ConfigureService configureService;
    @Autowired
    private IPBlackDao ipBlackDao;
    @Autowired
    private WebLogDao webLogDao;
    @Autowired
    private AndroidLogDao androidLogDao;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void contextLoads() throws Exception {
//        List<String> zh_tools = configureService.getUpdate("ZH Tools");
//        System.out.println(zh_tools);
//
//        files/AndroidUpLoadImage/4ecd9b374593a5b1/Screenshot_20220711_083627_com.huawei.android.launcher.jpg

//        List<Map<String, Object>> allBytime = webLogDao.findBeforeByDay(30);
//        System.out.println(JSONUtil.parse(allBytime).toStringPretty());

//        List<Map<String, Object>> beforeBytime = androidLogDao.findBeforeByDay(30);

//        timingService.timingSengVisitorForm();


//        System.out.println(JSONUtil.parse(visitorService.findAllByLast_timeLike("2023-03-13")).toStringPretty());
//        List<IPBlack> all = ipBlackDao.findAll();
//        for (IPBlack black : all) {
//            System.out.println(black.getIp()+" - "+ TimeUtil.toTime(black.getAdd_time(),TimeUtil.CnDateFormat,TimeUtil.TimeZoneID));
//        }
//        System.out.println(all);


//        Context context = new Context();
//        context.setVariable("main_url",setting.getMainUrl());
//        String form1 = templateEngine.process("form1", context);
//        System.out.println(form1);
//        emailService.send("报表测试", form1, true)
//        System.out.println(setting);

        System.out.println(ipBlackDao.findAllByFlag("b"));
        System.out.println(ipBlackDao.existsByIpAndFlag("127.0.0.1","w"));
    }

    public static void main(String[] args) {
        //jsx-2783320995
        String url1="https://www.cupfox.app/s/%E4%BF%84%E7%BD%97%E6%96%AF%E6%96%B9%E5%9D%97?id=26087471";
        String url2="https://www.cupfox.app/s/%E4%BF%84%E7%BD%97%E6%96%AF%E6%96%B9%E5%9D%97";
        System.out.println();
    }
}
