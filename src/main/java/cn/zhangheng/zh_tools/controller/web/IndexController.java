package cn.zhangheng.zh_tools.controller.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.zhangheng.zh_tools.bean.*;
import cn.zhangheng.zh_tools.dao.AndroidLogDao;
import cn.zhangheng.zh_tools.dao.IPBlackDao;
import cn.zhangheng.zh_tools.dao.PhoneInfoDao;
import cn.zhangheng.zh_tools.service.APPCommandService;
import cn.zhangheng.zh_tools.service.ConfigureService;
import cn.zhangheng.zh_tools.service.HtmlService;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import cn.zhangheng.zh_tools.util.MySession;
import com.zhangheng.bean.Message;
import com.zhangheng.file.FileUtil;
import com.zhangheng.file.TxtOperation;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.TimeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-11-23 14:01
 */
@Controller
public class IndexController {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private ConfigureService configureService;
    @Autowired
    private SettingConfig setting;
    @Autowired
    private StaticController staticController;
    @Autowired
    private PhoneInfoDao phoneInfoDao;
    @Autowired
    private AndroidLogDao androidLogDao;
    @Autowired
    private MySession mySession;
    @Autowired
    private HtmlService htmlService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private IPBlackDao ipBlackDao;


    @GetMapping("/")
    private String getIndex(Model model, HttpServletRequest request, HttpSession session) {
        String ip = CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders());
        String ug = CusAccessObjectUtil.getUser_Agent(request);
        Visitor visitor = null;
        MySession.Visit visit = mySession.find(ip, ug);
        if (visit != null) {
            visitor = (Visitor) visit.getAttr().get("visitor");
        }
        String address = ip;
        if (visitor != null) {
            String[] s = visitor.getLocation().split("-");
            if (s.length >= 7) {
                address = s[0] + "-" + s[1] + "-" + s[2] + "-" + s[3] + "-" + s[4];
//                model.addAttribute("address", address);
                log.info("\n首页访问：来自[" + ip + "]" + address + "-" + s[5] + "的访问者");
            }
        }
        model.addAttribute("address", address);
        model.addAttribute("weixin_url", setting.getWeixin_url());
        String appName = setting.getApplication_name();
        if (!setting.getIsUseDownLoadUrl()) {
            List<String> zh_toos = new ArrayList<>();
            try {
                zh_toos = configureService.getUpdate(appName);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            if (!zh_toos.isEmpty()) {
                String s = zh_toos.get(zh_toos.size() - 1);
                appName = FileUtil.getMainName(s);
                model.addAttribute("app_url", "/download/split/" + s);
            } else {
                model.addAttribute("app_url", setting.getAppDownLoadUrl());
            }
        } else {
            model.addAttribute("app_url", setting.getAppDownLoadUrl());
        }
        model.addAttribute("app_title", setting.getApp_introduce());
        model.addAttribute("app_name", appName + "下载");
        List<String> img_url = TxtOperation.readTxtFile("config/" + setting.getIndex_img_config_file());
        model.addAttribute("img_url", img_url);
        model.addAttribute("js_version", setting.getVersion());

        return setting.getIndex_html();
    }

    /**
     * 验证身份
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/666")
    public String Authentication() {
        return getAuthentication();
    }

    private String getAuthentication() {
        String body = null;
        try {
            String url = "https://" + setting.getDataHost() + "/";
            String html = HttpUtil.get(url);
            Document document = Jsoup.parse(html);
            Elements select = document.select("form").select("input").select("[type=hidden]");
            Map<String, String> map = new HashMap<>();
            for (Element e : select) {
                map.put(e.attr("name"), e.attr("value"));
            }
            body = HttpRequest.post(url)
                    .form("pw", setting.getDataPwd())
                    .form("csrf", map.get("csrf"))
                    .form("ip", map.get("ip"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36")
                    .execute().body();
        } catch (Exception e) {
            //SSLException: Unrecognized SSL message, plaintext connection?
            //ConnectException: Connection refused: connect
            String msg = e.getMessage();
            if (msg.startsWith("SSLException:")) {
                body = "你已成功认证，请勿重复认证！";
            } else if (msg.startsWith("ConnectException")) {
                body = "认证失败，可能认证服务未开启，服务器暂时不可用！";
            } else {
                body = msg;
            }
        }
        return body;
    }

    @GetMapping("/3")
    public String htm3(Model model) {
        model.addAttribute("main_url", setting.getMainUrl());
        return "renewal";
    }

    private String operation_name1 = "Web续期APP";

    @PostMapping("/3_form")
    @ResponseBody
    public Message html3(String phoneId, String version, String code, HttpServletRequest request) {
        Message msg;
        msg = staticController.verifyMathCheck(code, true, request);
        if (msg.getCode() != 200)
            return msg;
        if (!StrUtil.isBlank(phoneId) && !StrUtil.isBlank(version)) {
            Optional<PhoneInfo> byId = phoneInfoDao.findById(phoneId);
            if (byId.isPresent()) {
                if (byId.get().getApp_version().endsWith(version)) {
                    List<AndroidLog> all = androidLogDao.findAllByTimeLikeAndOperation_nameAnAndPhone_id(TimeUtil.getNowTime().substring(0, 10), operation_name1, phoneId);
                    if (all.size() <= 0) {
                        try {
                            int maxDay = 7;
                            String cmd = APPCommandService.cmd3(phoneId, maxDay);
                            AndroidLog alog = new AndroidLog();
                            String ip = CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders());
                            String ug = CusAccessObjectUtil.getUser_Agent(request);
//                            Visitor visitor = (Visitor) request.getSession().getAttribute("visitor");
                            Visitor visitor = null;
                            MySession.Visit visit = mySession.find(ip, ug);
                            if (visit != null) {
                                visitor = (Visitor) visit.getAttr().get("visitor");
                            }else {
                                msg.setCode(500);
                                msg.setTitle("续期失败");
                                msg.setMessage("您的访问信息未获取到！");
                                return msg;
                            }
                            alog.setIp(StrUtil.format("[{}]{}", visitor.getIp(), visitor.getLocation()));
                            alog.setOperation_name(operation_name1);
                            alog.setTime(msg.getTime());
                            alog.setPhone_id(phoneId);
                            alog.setOperation_content(JSONUtil.createObj()
                                    .set("maxDay", maxDay)
                                    .set("appId", phoneId)
                                    .set("version", version)
                                    .toString());
                            androidLogDao.saveAndFlush(alog);
                            msg.setCode(200);
                            msg.setTitle("续期成功");
                            msg.setMessage(cmd);
                            log.info("\nAPP续期：{}\n", alog.toString());
                        } catch (Exception e) {
                            log.error("APP续期错误：" + e.toString());
                            msg.setCode(500);
                            msg.setTitle("续期错误");
                            msg.setMessage(e.toString());
                            e.printStackTrace();
                        }
                    } else {
                        msg.setCode(500);
                        msg.setTitle("续期失败");
                        msg.setMessage("您今日续期次数达到上限！");
                    }
                } else {
                    msg.setCode(500);
                    msg.setTitle("应用版本错误");
                    msg.setMessage("应用ID对应的应用版本不一致！");
                }
            } else {
                msg.setCode(500);
                msg.setTitle("应用ID错误");
                msg.setMessage("应用ID不存在！");
            }
        } else {
            msg.setCode(500);
            msg.setTitle("输入内容错误");
            msg.setMessage("应用ID和应用版本不能为空！");
        }

        return msg;
    }

    @ResponseBody
    @RequestMapping("/1")
    private String main(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder("<center><h1>欢迎来自:");
        String ip = CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders());
        MySession.Visit visit = mySession.find(ip, CusAccessObjectUtil.getUser_Agent(request));
        //int i=1/0;
        if (!NetUtil.isInnerIP(ip)) {
            Optional<Visitor> visitors = visitorService.getVisitorByIp(ip);
            if (visitors.isPresent()) {
                Visitor visitor = visitors.get();
                msg.append(visitor.getLocation().substring(0, visitor.getLocation().indexOf("<") - 1));
                if (visit!=null){
                    msg.append(",您已访问" + Convert.toInt(visit.getAttr().get("_c")) + "次了");
                }
            } else {
                msg.append(CusAccessObjectUtil.getRequst(request));
            }
        } else {
            msg.append(CusAccessObjectUtil.getRequst(request));
        }
        msg.append("</h1>");
        //个人主页
        msg.append("<p><a href='https://github.com/ZhangHeng0805'>个人GitHub主页</a></p>");
        //最新APP下载
        try {
            List<String> zh_toos = new ArrayList<>();
            zh_toos = configureService.getUpdate("ZH Tools");
            String download_url = "";
            if (!zh_toos.isEmpty()) {
                download_url = "/download/split/" + zh_toos.get(zh_toos.size() - 1);
            } else {
                download_url = setting.getAppDownLoadUrl();
            }
            msg.append("<p><a href='" + download_url + "'>点我下载最新安卓工具APP</a></p>");
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        msg.append("</center>");

        log.info("\n[" + CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders()) + "]" + msg.substring(msg.indexOf("<h1>") + 4, msg.indexOf("</h1>")));
        return msg.toString();
    }

    @ResponseBody
    @RequestMapping("/2")
    private String test2(String time){
        if (StrUtil.isBlank(time))
            time= TimeUtil.toTime(new Date(),"yyyy-MM-dd");
        return htmlService.getHtmlString1(time).toString();
    }


    @ResponseBody
    @RequestMapping("/ip")
    private Message ip(String ip,String flag){
        Message msg = new Message();
        if (!StrUtil.isBlank(ip)&&!StrUtil.isBlank(flag)){
            if(flag.equals("w")||flag.equals("b")) {
                Optional<Visitor> byId = visitorService.findById(ip);
                if (byId.isPresent()) {
                    IPBlack black = new IPBlack();
                    black.setIp(ip);
                    black.setAdd_time(new Date());
                    black.setExplain(byId.get().getLocation());
                    black.setFlag(flag);
                    IPBlack black1 = ipBlackDao.saveAndFlush(black);
                    msg.setCode(200);
                    String s=flag.equals("w")?"白":"黑";
                    msg.setTitle("IP"+s+"名单编辑成功");
                    msg.setObj(black1);
                }else {
                    msg.setCode(500);
                    msg.setTitle("ip错误");
                    msg.setMessage("ip未知，不存在");
                }
            }else {
                msg.setCode(500);
                msg.setTitle("参数错误");
                msg.setMessage("参数flag错误");
            }
        }else {
            msg.setCode(500);
            msg.setTitle("参数为空");
            msg.setMessage("参数ip和flag不能为空");
        }
        return msg;
    }
}
