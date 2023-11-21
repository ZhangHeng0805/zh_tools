package cn.zhangheng.zh_tools.controller;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.zhangheng.zh_tools.bean.PhoneContacts;
import cn.zhangheng.zh_tools.bean.PhoneInfo;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.service.entity_service.AndroidLogService;
import cn.zhangheng.zh_tools.service.entity_service.PhoneContactsService;
import cn.zhangheng.zh_tools.service.entity_service.PhoneInfoService;
import com.zhangheng.bean.Message;
import com.zhangheng.file.FileOperation;
import com.zhangheng.file.FileUtil;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.EncryptUtil;
import com.zhangheng.util.RandomUtil;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-25 10:34
 */

@RequestMapping("/android_listener")
@RestController
public class AndroidListenerController {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PhoneInfoService phoneInfoService;
    @Autowired
    private PhoneContactsService contactsService;
    @Autowired
    private SettingConfig setting;
    @Autowired
    private AndroidLogService androidLogService;

    @RequestMapping("/intent")
    public void intent_listener(String json, HttpServletRequest request) throws Exception {
        String l = "\nintent监听：" + json+"\n";
        log.info(l);
        androidLogService.saveLog("intent监听", json, request);
    }
    @RequestMapping("/location")
    public void location_listener(String json, HttpServletRequest request) {
        if (!StrUtil.isBlank(json)) {
            String l = "\nlocation监听：" + json+"\n";
            log.info(l);
            androidLogService.saveLog("位置上报", json, request);
        }
    }


    @RequestMapping("/function")
    public void function_listener(String json, HttpServletRequest request) {
        String l = "\nfunction监听：" + json+"\n";
        log.info(l);
        androidLogService.saveLog("function监听", json, request);
    }
    @RequestMapping("/event")
    public void event_listener(String json, HttpServletRequest request) {
        String l = "\nevent监听：" + json+"\n";
        log.info(l);
        androidLogService.saveLog("event监听", json, request);
    }

    /**
     * 获取手机通讯录
     *
     * @param json
     * @param req
     * @throws Exception
     */
    @PostMapping("/m16")
    public void m16(String json, HttpServletRequest req) throws Exception {
//        System.out.println(JSONUtil.formatJsonStr(json));
        Integer count = 0;
        if (!StrUtil.isEmpty(json)) {
            JSONObject msg = JSONUtil.parseObj(json);
            //校验消息合法
            PhoneInfo phoneInfo = phoneInfoService.UserAgentToPhoneInfo(CusAccessObjectUtil.getRequst(req));
            String[] split = phoneInfo.getApp_version().split("_");
            String v = split[split.length - 1];
            //比较版本
            if (msg.getStr("version").equals(v)) {
                //校验签名
                String signature = EncryptUtil.getSignature(msg.getStr("time"), msg.getStr("code"));
                if (msg.getStr("signature").equals(signature)) {
                    String phone_id = phoneInfo.getId();
                    List<PhoneContacts> allByPhoneId = contactsService.getAllByPhoneId(phone_id);
                    String obj = Base64Decoder.decodeStr(msg.getStr("obj"), CharsetUtil.CHARSET_UTF_8);
                    JSONArray objects = JSONUtil.parseArray(obj);
                    Set<PhoneContacts> all = new HashSet<>();
                    count = objects.size();
                    for (Object o : objects) {
                        PhoneContacts contacts = new PhoneContacts();
                        JSONObject phones = JSONUtil.parseObj(o);
                        String telPhone = phones.getStr("telPhone");
                        String name = phones.getStr("name");
                        String md5Id = EncryptUtil.getMd5(telPhone + name + phone_id);
                        if (!allByPhoneId.isEmpty()) {
                            boolean f = true;
                            for (PhoneContacts p : allByPhoneId) {
                                if (p.getTel().equals(telPhone)) {
                                    f = false;
                                    break;
                                }
                            }
                            if (f) {
                                contacts.setId(md5Id);
                                contacts.setName(name);
                                contacts.setTel(telPhone);
                                contacts.setPhone_id(phone_id);
                                contacts.setAdd_time(new Date());
                                all.add(contacts);
                            }
                        } else {
                            contacts.setId(md5Id);
                            contacts.setName(name);
                            contacts.setTel(telPhone);
                            contacts.setPhone_id(phone_id);
                            contacts.setAdd_time(new Date());
                            all.add(contacts);
                        }
                    }
                    int size = contactsService.saveALL(all).size();
                    String format = StrUtil.format("设备[{}]一共有{}条记录，已保存{}条记录", phone_id, count, size);
                    log.info("\n/m16通讯录:" + format+"\n");
                    androidLogService.saveLog("获取通讯录", format, req);
                }else{
                    log.warn("/m16:签名验证失败");
                }
            } else {
                log.warn("/m16:应用版本不一致");
            }
        } else {
            log.warn("/m16:请求内容为空");
        }
    }
    @Value("#{'${android.upload.img.rules}'.split(',')}")
    private List<String> upImgRules;
    /**
     * 获取手机制定格式图片数量
     * @param json
     * @param request
     * @return
     * @throws Exception
     */
    @GetMapping("/m3_getUpload")
    public String m3_getUpload(String json, HttpServletRequest request) throws Exception {
        String id = phoneInfoService.UserAgentToPhoneInfo(CusAccessObjectUtil.getRequst(request)).getId();
        Message msg = new Message();
        String nowUnix = TimeUtil.getNowUnix();
        msg.setTime(nowUnix);
        String passWord = RandomUtil.createPassWord(8, "012");
        msg.setTitle(passWord);
        msg.setMessage(EncryptUtil.getSignature(nowUnix, passWord));
        if (!StrUtil.isEmpty(json)) {
            JSONObject obj = JSONUtil.parseObj(json);
            Integer num = obj.getInt("num");
            String format = StrUtil.format("设备[{}]图片个数{}", id, num);
            log.info("\n图片上传get请求：{}," + format+"\n", TimeUtil.toTime(TimeUtil.UnixToDate(obj.getStr("time"))));
            androidLogService.saveLog("图片上传请求", format, request);
            if (num > 0) {
                msg.setCode(200);
                msg.setObj(handleUpImageNum(upImgRules,num));
                return JSONUtil.toJsonStr(msg);
            }
        }
        msg.setCode(500);
        return JSONUtil.toJsonStr(msg);
    }

    private int handleUpImageNum(List<String> rules,int num){
        int i=1;
        if (rules!=null) {
            for (String rule : rules) {
                if (num > Convert.toInt(rule, 0)) {
                    i++;
                }
            }
        }
        return i;
    }

    @PostMapping("/m3_postUpload")
    public void m3_postUpload(String json, HttpServletRequest request) throws Exception {
//        System.out.println(json);
        if (!StrUtil.isEmpty(json)) {
            String id = phoneInfoService.UserAgentToPhoneInfo(CusAccessObjectUtil.getRequst(request)).getId();
            String path = setting.getBaseDir() + "AndroidUpLoadImage/" + id + "/";
            FileOperation.mkdirs(path);
            JSONObject obj = JSONUtil.parseObj(json);
            String name = Base64Decoder.decodeStr(obj.getStr("name"), CharsetUtil.CHARSET_UTF_8);
            File file = FileOperation.bytesToFile(EncryptUtil.deBase64(obj.getStr("data")), path, name);
            Long size = obj.getLong("size") != null ? obj.getLong("size") : 0;
            String path1 = Base64Decoder.decodeStr(obj.getStr("path"), CharsetUtil.CHARSET_UTF_8);
            String format = StrUtil.format("设备[{}]上传图片[{}]原大小[{}],保存大小[{}]",
                    id, path1,
                    FileUtil.getFileSizeString(size),
                    FileUtil.getFileSizeString(file.length())
            );
            log.info("\nAndroid图片保存：" + format+"\n");
            androidLogService.saveLog("保存图片", format, request);
        }
    }
}
