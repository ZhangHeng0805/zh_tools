package cn.zhangheng.zh_tools.service;

import cn.hutool.core.collection.ListUtil;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import com.zhangheng.bean.Const;
import com.zhangheng.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * 功能描述
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-12-26 09:48
 */

@Service
public class EmailService {

    private static EmailUtil emailUtil;
    @Autowired
    private SettingConfig setting;

    static {
        if (emailUtil == null)
            emailUtil = new EmailUtil(Const.getMailAccount_163());
    }

    public EmailService() {
        if (emailUtil == null)
            emailUtil = new EmailUtil(Const.getMailAccount_163());

    }

    public void send(List<String> recipient, String title, String content, boolean isHtml) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                emailUtil.send(recipient, title, content, isHtml);
            }
        }).start();

    }


    public void send(String title, String content, boolean isHtml) {
        send(ListUtil.of(setting.getAdminEmail()), title, content, isHtml);
    }


    public void send(String title, String content) {
        send(ListUtil.of(setting.getAdminEmail()), title, content, false);
    }

    public void send(String title, String content, boolean isHtml, File[] files) {
        send(ListUtil.of(setting.getAdminEmail()), title, content, false, files);
    }


    public void send(List<String> recipient, String title, String content, boolean isHtml, File[] files) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                emailUtil.send(recipient, title, content, isHtml, files);
            }
        }).start();

    }
}
