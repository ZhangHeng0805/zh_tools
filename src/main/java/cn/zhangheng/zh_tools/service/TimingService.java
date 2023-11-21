package cn.zhangheng.zh_tools.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.AndroidLog;
import cn.zhangheng.zh_tools.bean.IPBlack;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.dao.IPBlackDao;
import cn.zhangheng.zh_tools.service.entity_service.AndroidLogService;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-19 17:12
 */
@Service
public class TimingService {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private SettingConfig setting;
    @Autowired
    private AndroidLogService androidLogService;
    @Autowired
    private IPBlackDao ipBlackDao;
    @Autowired
    private HtmlService htmlService;
    @Autowired
    private EmailService emailService;
    @Value(value = "#{'${timing.email.if-send-img}'}")
    private Boolean ifSendImg;
    @Value(value = "#{'${timing.email.if-send-visitor-form}'}")
    private Boolean ifSendvisitor;
    @Value(value = "#{'${timing.email.if-send-android-log-form}'}")
    private Boolean ifSendAndroidLog;


    /**
     * 定时重置白名单中的每日请求次数
     */
    @Async
    @Scheduled(cron = "0 0 6,18 * * ?")
    public void TimingDelAccessTokenBefore() {
        @Min(1) @NotNull Integer maxRequestCounts = setting.getMaxRequestCounts();
        Integer max = Convert.toInt(maxRequestCounts - (maxRequestCounts * 0.5));
        List<Visitor> all = visitorService.findAllByCountsOrState(max, 1);
//        List<String> list = TxtOperation.readTxtFile("config/ip-black-list.txt");
        List<IPBlack> list = ipBlackDao.findAllByFlag("b");
        Integer b = 0;
        if (!all.isEmpty()) {
            if (!list.isEmpty()) {
                List<Visitor> remove = new ArrayList<>();
                for (int i = 0; i < all.size(); i++) {
                    for (IPBlack black : list) {
                        if (black.getIp().equals(all.get(i).getIp())) {
                            remove.add(all.get(i));
                        }
                    }
                }
                for (Visitor index : remove) {
                    boolean isRemove = all.remove(index);
                    if (isRemove) {
                        b++;
                    }
                }
            }
            Integer n = 0;
            for (Visitor visitor : all) {
                if (visitor.getCount() >= max || visitor.getState().equals(1)) {
                    visitor.setCount(0);
                    visitor.setState(0);
                    try {
                        visitorService.saveAndflush(visitor);
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                    n++;
                }
            }
            log.info("\n定时任务:重置白名单IP个数[{}],未重置黑名单IP个数[{}]", n, b);
        } else {
            log.info("\n定时任务:无需重置IP");
        }
    }

    /**
     * 邮件
     * 定时统计安卓用户上传文件
     */
    @Async
    @Scheduled(cron = "0 30 8 * * ?")
    public void timingSengImg() {
        if (ifSendImg) {
            String yesterday = TimeUtil.toTime(DateUtil.yesterday(), "yyyy-MM-dd");
            List<AndroidLog> all = androidLogService.findAllByTimeLikeAndOperation_name(yesterday, "保存图片");
            if (!all.isEmpty()) {
                Set<String> ids = new HashSet<>();
                for (AndroidLog a : all) {
                    ids.add(a.getPhone_id());
                }
                for (String s : ids) {
                    StringBuilder content = new StringBuilder();
                    List<File> files = new ArrayList<>();
                    for (AndroidLog a : all) {
                        if (s.equals(a.getPhone_id())) {
                            String operation_content = a.getOperation_content();
                            content.append(a.getTime() + " - " + operation_content + "\n\n");
                            try {
                                String s1 = StrUtil.subBetween(operation_content, "]上传图片[", "]原大小[");
                                String[] split = s1.split("/");
                                File file = new File(setting.getBaseDir() + "AndroidUpLoadImage/" + s + "/" + split[split.length - 1]);
                                if (file.exists()) {
                                    files.add(file);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage());
                            }
                        }
                    }
                    File[] fs = new File[files.size()];
                    files.toArray(fs);
                    emailService.send("ZH Tools客户端[" + s + "]昨日上传图片", content.toString(), false, fs);
                }
            }
        }
    }

    /**
     * 邮件
     * 定时发送昨日普通访问报表
     */
    @Async
    @Scheduled(cron = "0 0 8 * * ?")
    public void timingSengVisitorForm() {
        if (ifSendvisitor) {
            String yesterday = TimeUtil.toTime(DateUtil.yesterday(), "yyyy-MM-dd");
            StringBuilder html = htmlService.getHtmlString1(yesterday);
            if (html.length() > 0) {
                emailService.send("ZH Tools[" + yesterday + "]普通用户访问报表", html.toString(), true);
            } else {
                log.info("定时任务：暂无普通用户访问数据报表");
            }
        }
    }



    /**
     * 邮件
     * 定时发送昨日安卓访问报表
     */
    @Async
    @Scheduled(cron = "0 10 8 * * ?")
    public void timingSengAndroidLogForm() {
        if (ifSendAndroidLog) {
            String yesterday = TimeUtil.toTime(DateUtil.yesterday(), "yyyy-MM-dd");
            List<AndroidLog> androidLogs = androidLogService.findAllByTimeLike(yesterday);
            StringBuilder sb = new StringBuilder();
            if (androidLogs.size() > 0) {
                sb.append("<h3>" + yesterday + " 安卓用户访问记录</h3>");
                sb.append("<table border=\"1\">");
                sb.append("<tr>");
                sb.append("<th>").append("ID").append("</th>");
                sb.append("<th>").append("时间").append("</th>");
                sb.append("<th>").append("IP").append("</th>");
                sb.append("<th>").append("操作名").append("</th>");
                sb.append("<th>").append("操作内容").append("</th>");
                sb.append("<th>").append("设备ID").append("</th>");
                sb.append("</tr>");

                for (AndroidLog a : androidLogs) {
                    sb.append("<tr>");
                    sb.append("<td>").append(a.getId()).append("</td>");
                    sb.append("<td>").append(a.getTime()).append("</td>");
                    sb.append("<td>").append(a.getIp()).append("</td>");
                    sb.append("<td>").append(a.getOperation_name()).append("</td>");
                    sb.append("<td>").append(a.getOperation_content()).append("</td>");
                    sb.append("<td>").append(a.getPhone_id()).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                sb.append("<p>总计:" + androidLogs.size() + "条数据</p>");
                sb.append("<p><a href='" + setting.getMainUrl() + "'>星曦向荣网</a></p>");
            }
            if (sb.length() > 0) {
                emailService.send("ZH Tools[" + yesterday + "]安卓用户访问报表", sb.toString(), true);
            } else {
                log.info("定时任务：暂无安卓用户访问数据报表");
            }
        }
    }

}
