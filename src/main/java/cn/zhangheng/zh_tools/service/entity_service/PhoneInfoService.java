package cn.zhangheng.zh_tools.service.entity_service;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.zhangheng.zh_tools.bean.PhoneInfo;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.dao.PhoneInfoDao;
import cn.zhangheng.zh_tools.reptile.IPAnalysisAPI;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.EncryptUtil;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-17 10:50
 */
@Service
public class PhoneInfoService {
    @Autowired
    private PhoneInfoDao phoneInfoDao;
    @Autowired
    private VisitorService visitorService;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public PhoneInfo saveAndflush(HttpServletRequest request) {
        String req = CusAccessObjectUtil.getRequst(request);
        PhoneInfo phone_info = null;
        try {
            PhoneInfo phoneInfo = UserAgentToPhoneInfo(req);
            phone_info = phoneInfoDao.saveAndFlush(phoneInfo);
        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.getMessage());
        }
        return phone_info;
    }

    /**
     * 解析requst为手机信息对象
     *
     * @param request
     * @return
     */
    public PhoneInfo UserAgentToPhoneInfo(String request) throws Exception {
        String[] info = {"model:", "sdk:", "release:", "Appversion:", "tel:", "ID:"};
        PhoneInfo pi = new PhoneInfo();
//        System.out.println(user_agent);
        try {
//应用更新查询:127.0.0.1 / (model:M2104K10AC) (sdk:30) (release:Android11) (Appversion:HappyShopping V2-2.23) (tel:[China Mobile]null) (ID:18fdc81fffa6884e)
            String[] split2 = request.split(" / ");
            String Phone = split2[1];

            String ip = split2[0].replace("[", "").replace("]", "");
            Optional<Visitor> visitorByIp = visitorService.getVisitorByIp(ip);
            if (visitorByIp.isPresent()) {
                Visitor visitor = visitorByIp.get();
                ip = "[" + visitor.getIp() + "]" + visitor.getLocation();
            } else {
                if (IPAddressUtil.isIPv6LiteralAddress(ip)) {
                } else {
                    if (!NetUtil.isInnerIP(ip)) {
                        String ipInfo1_str = IPAnalysisAPI.getIPInfo1_str(IPAnalysisAPI.getIPInfo1(ip));
                        if (!StrUtil.isEmpty(ipInfo1_str)) {
                            ip = ipInfo1_str;
                        }
                    }
                }
            }
            pi.setIp(ip);
            if (JSONUtil.isTypeJSON(Phone)) {
                JSONObject obj = JSONUtil.parseObj(Phone);
//                System.out.println(obj.toStringPretty());
                String charset = obj.getStr("charset", null);
                charset = charset != null ? charset : "UTF-8";
                String id = obj.getStr("ID");
                String model = obj.getStr("model");
                String sdk = obj.getStr("sdk");
                String release = obj.getStr("release");
                String appversion = obj.getStr("Appversion");
                String tel = obj.getStr("tel");
                String notice = obj.getStr("notice");
                Long time = obj.getLong("time");
                String token = obj.getStr("token", "");
                String sign =
                        time +
                        id +
                        model +
                        sdk +
                        release +
                        appversion +
                        tel +
                        notice +
                        TimeUtil.toTime(new Date(time), TimeUtil.EnDateFormat_Detailed);
//                System.out.println(sign);
                String signature = EncryptUtil.getSignature(sign, id, charset);
                if (StrUtil.equals(token, signature)) {
                    pi.setId(id);
                    pi.setModel(model);
                    pi.setNet_address(sdk);
                    pi.setRelease(release);
                    pi.setApp_version(appversion);
                    tel = EncryptUtil.deBase64Str(tel, charset);
                    String tel_type = StrUtil.subBetween(tel, "[", "]");
                    pi.setTel_type(tel_type);
                    pi.setTel(StrUtil.subAfter(tel, "]", true));
                    pi.setNotice(EncryptUtil.deBase64Str(notice, charset));
                } else {
                    System.out.println("token:" + token);
                    System.out.println("signature:" + signature);
                    throw new RuntimeException("客户端token验证失败！");
                }
            } else {
                String model = Phone.substring(Phone.indexOf(info[0]) + info[0].length(), Phone.indexOf(") (" + info[1]));
                pi.setModel(model);

                String sdk = Phone.substring(Phone.indexOf(info[1]) + info[1].length(), Phone.indexOf(") (" + info[2]));
                pi.setNet_address(sdk);

                String release = Phone.substring(Phone.indexOf(info[2]) + info[2].length(), Phone.indexOf(") (" + info[3]));
                pi.setRelease(release);

                String Appversion = Phone.substring(Phone.indexOf(info[3]) + info[3].length(), Phone.indexOf(") (" + info[4]));
                pi.setApp_version(Appversion);

                String Tel = Phone.substring(Phone.indexOf(info[4]) + info[4].length(), Phone.indexOf(") (" + info[5]));
                String[] Telsplit = Tel.split("]");
                if (Telsplit.length > 1) {
                    if (!Telsplit[1].equals("null")) {
                        pi.setTel(Telsplit[1]);
                    }
                }
                pi.setTel_type(Telsplit[0].substring(1));

                String id = Phone.substring(Phone.indexOf(info[5]) + info[5].length(), Phone.lastIndexOf(")"));
                pi.setId(id);
            }
            pi.setLast_time(TimeUtil.getNowTime());
        } catch (Exception e) {
//            e.printStackTrace();
            log.error("手机信息解析错误：{}", e.toString());
            throw new RuntimeException("非法客户端：" + request);
        }
        return pi;
    }


}
