package cn.zhangheng.zh_tools.service;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.zhangheng.test.app.bean.AppLife;
import com.zhangheng.util.EncryptUtil;
import com.zhangheng.util.TimeUtil;

import java.util.Date;

/**
 * APP口令秘钥的生成
 */
public class APPCommandService {

    public static void main(String[] args) throws Exception {
        //续签口令1
//        cmd1();
        //更新服务地址
//        cmd2();
        //续签口令2
//        System.out.println(cmd3("e3be0ed16136e326",1000));

    }

    /**
     * APP续签口令秘钥生成
     * m
     * V23.03.08
     * @throws Exception
     */
    public static String cmd3(String appId,Integer maxDay) throws Exception {
        AppLife appLife = new AppLife();
        appLife.setCreateTime(new Date().getTime());
        appLife.setMaxDay(maxDay);
        appLife.setAppId(appId);
        appLife.setSign(appLife.createSign());
        JSON json = JSONUtil.parse(appLife);
        System.out.println(json.toStringPretty());
        String cmd="m:"+ EncryptUtil.enBase64Str(json.toString());
//        System.out.println(cmd);
        return cmd;
    }

    /**
     * 更新服务地址口令生成
     */
    private static void cmd2() throws Exception {
        String s = TimeUtil.toTime(new Date(), "yyyyMMdd");
        //更新的服务地址
//        String url="http://zhangheng.free.idcfengye.com/";
        String url="http://zh-tools.zhangheng0805.asia/";
        String myMd5 = EncryptUtil.getMyMd5(url + s);
        String c="u:"+ EncryptUtil.enBase64Str(url+"||"+myMd5);
        System.out.println(c);
    }

    /**
     * APP续签口令秘钥生成
     * m
     * V23.02.25
     * @throws Exception
     */
    private static void cmd1() throws Exception {
        String s = TimeUtil.toTime(new Date(), "yyyyMMddHH");
        String a = "3,5,6,7";//限制能使用的功能序号
        String v = "V23.02.25";//APP版本号
        boolean is = false;//是否限制功能
        String myMd51 = EncryptUtil.getMyMd5(s + v + is);
        if (is) {
            String myMd52 = EncryptUtil.getMyMd5(a + s);
            String b = "m:" + myMd51 + ":" + EncryptUtil.enBase64Str(a + "||" + myMd52);
            System.out.println(b);
        } else {
            System.out.println("m:" + myMd51);
        }
    }
}
