package cn.zhangheng.zh_tools.reptile;

import cn.hutool.core.net.NetUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.zhangheng.util.EncryptUtil;
import com.zhangheng.util.RandomUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 有道API
 * @author 张恒
 * @program: weixin1
 * @email zhangheng.0805@qq.com
 * @date 2022-09-28 12:43
 */
public class YouDaoAPI {

    public static Map<String,String> Lag_Type=new HashMap<String, String>();
    static {
        Lag_Type.put("zh-CHS","中文");
        Lag_Type.put("en","英语");
        Lag_Type.put("ja","日语");
        Lag_Type.put("ko","韩语");
        Lag_Type.put("fr","法语");
        Lag_Type.put("de","德语");
        Lag_Type.put("ru","俄语");
        Lag_Type.put("es","西班牙语");
        Lag_Type.put("pt","葡萄牙语");
        Lag_Type.put("it","意大利语");
        Lag_Type.put("vi","越南语");
        Lag_Type.put("id","印尼语");
        Lag_Type.put("ar","阿拉伯语");
        Lag_Type.put("nl","荷兰语");
        Lag_Type.put("th","泰语");
    }

    /**
     * 有道翻译
     *
     * 爬虫API
     * @param e 需要翻译的内容
     * @return JSON结果
     */
    public static String getTranslation(String e) throws Exception {
        String url="https://fanyi.youdao.com/translate_o?smartresult=dict&smartresult=rule";
        String ncoo=""+2147483647 * Math.random();
        String ipByHost = NetUtil.getIpByHost("www.baidu.com");
        String app_v="5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 Edg/103.0.1264.77";
        String cookie="OUTFOX_SEARCH_USER_ID=-257520112@"+ipByHost+"; OUTFOX_SEARCH_USER_ID_NCOO="+ncoo+"; ___rl__test__cookies="+new Date().getTime();

        Map<String,Object> map=new HashMap<String, Object>();
        map.put("from","auto");
        map.put("to","auto");
        map.put("i",e);
        map.put("smartresult","dict");
        map.put("client","fanyideskweb");
        long time = new Date().getTime();
        map.put("lts", time);
        String salt = time + String.valueOf(RandomUtil.createRandom(0, 9));
        map.put("salt", salt);
        String sign = EncryptUtil.getMd5("fanyideskweb" + e + salt + "Ygy_4c=r#e#4EX^NUGUc5");
        map.put("sign",sign);
        map.put("bv", EncryptUtil.getMd5(app_v));
        map.put("doctype","json");
        map.put("version","2.1");
        map.put("keyfrom","fanyi.web");
        map.put("action","FY_BY_CLICKBUTTION");

        String body = HttpRequest.post(url).form(map)
                .header(Header.REFERER, "https://fanyi.youdao.com/")
                .header(Header.USER_AGENT,"Mozilla/"+app_v)
                .cookie(cookie)
                .execute().body();

        return JSONUtil.formatJsonStr(body)
                .replace("\\r","")
                .replace("\\n","");
    }
}
