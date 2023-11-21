package cn.zhangheng.zh_tools.controller;

import cn.hutool.core.comparator.VersionComparator;
import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.PhoneInfo;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.service.entity_service.AndroidLogService;
import cn.zhangheng.zh_tools.service.ConfigureService;
import cn.zhangheng.zh_tools.service.entity_service.PhoneInfoService;
import com.zhangheng.bean.Message;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 客户端配置
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-16 23:47
 */
@RequestMapping("/config")
@Controller()
public class ConfigureController {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigureService configureService;
    @Autowired
    private PhoneInfoService phoneInfoService;
    @Autowired
    private AndroidLogService androidLogService;
    @Autowired
    private AndroidListenerController androidListenerController;
    @Autowired
    private SettingConfig setting;


    /**
     * App根性查询
     * 查询文件根目录中的update文件夹
     *
     * @param type
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody()
    @RequestMapping("/updateApp/{type}")
    private Message updateApp(@Nullable @PathVariable("type") String type, @Nullable @RequestParam("json") String json, HttpServletRequest request) {
        Message msg = new Message();
        msg.setTime(TimeUtil.toTime(new Date()));
        androidListenerController.location_listener(json, request);
        if (!StrUtil.isEmpty(type)) {
            PhoneInfo phoneInfo = (PhoneInfo) request.getSession().getAttribute("phone_info");
            boolean isNew = VersionComparator.INSTANCE.compare(phoneInfo.getApp_version(),"ZH Tools_V23.04.10") > 0;
            String message;
            if (!setting.getIsUseDownLoadUrl()||!isNew){
                try {
                    List<String> list = new ArrayList<>();
                    try {
                        list = configureService.getUpdate(type);
                    } catch (Exception e) {
                        log.error("update查询错误：" + e.getMessage());
                    }
                    if (!list.isEmpty()) {
                        msg.setCode(200);
                        message=list.get(list.size() - 1);
                        if (!isNew) {
                            msg.setTitle(type);
                        }else {
                            msg.setTitle(setting.getApplication_name()+"_"+setting.getVersion());
                            message=setting.getMainUrl()+"fileload/download/"+message;
                        }
                        msg.setMessage(message);
                        msg.setObj(setting.getMainUrl());
                        String content = message + " || " + phoneInfo.getApp_version();
                        androidLogService.saveLog(type + "更新查询", content, request);
                        log.info("\nAndroid更新：" + content + "\n");
                    } else {
                        msg.setCode(404);
                        msg.setTitle("暂无更新");
                        msg.setMessage("暂时没有更新安装包");
                    }
                } catch (Exception e) {
                    msg.setCode(500);
                    msg.setTitle("更新错误");
                    msg.setMessage(e.getMessage());
                    log.error("update查询错误：" + e.getMessage());
                }
            }else {
                msg.setTitle(setting.getApplication_name()+"_"+setting.getVersion());
                message=setting.getAppDownLoadUrl();
                msg.setMessage(message);
                msg.setObj(setting.getMainUrl());
                String content = phoneInfo.getApp_version() + " || " + message;
                androidLogService.saveLog(type + "更新查询", content, request);
                log.info("\nAndroid更新：" + content + "\n");
            }

        } else {
            msg.setCode(500);
            msg.setTitle("参数错误");
            msg.setMessage("参数不能为空");
        }
        return msg;
    }

    @ResponseBody()
    @RequestMapping("/setting")
    private SettingConfig getSetting() {
        return setting;
    }
}
