package cn.zhangheng.zh_tools.reptile;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangheng.log.Log;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.RandomUtil;
import com.zhangheng.util.TimeUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.HttpCookie;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ip解析API
 * @author 张恒
 * @program: reptile
 * @email zhangheng.0805@qq.com
 * @date 2022-09-30 08:06
 */
public class IPAnalysisAPI {

    public static void main(String[] args) {
        String ipInfo1_str = getIPInfo1_str(getIPInfo1("121.33.160.73"));
        System.out.println(ipInfo1_str);
    }

    public static String getIPMessage(HttpServletRequest request){
        String ipAddress = CusAccessObjectUtil.getClientIp(request);
        if (NetUtil.isInnerIP(ipAddress)){
            return CusAccessObjectUtil.getRequst(request);
        }
        return getIPInfo1_str(getIPInfo1(ipAddress));
    }

    /**
     * 获取getIPInfo1中的Cookie
     *
     * @return
     */
    private static String getIPInfo1Cookie() {
        HashMap<String, Object> m = new HashMap<>();
        //APP名
        m.put("app", "");
        //设备名
        m.put("br", "Pc");
        //浏览器
        m.put("bs", "Edg");
        //浏览器版本
        m.put("bsv", 103);
        String cid = String.valueOf((int) (Math.round(2147483647 * Math.abs(Math.random() - 1)) * Integer.valueOf(TimeUtil.getTimeInfo(TimeUtil.MilliSecond)) % 1E10));
        //Cookie ID
        m.put("cid", cid);
        m.put("df", "");
        //页面地址
        m.put("dl", "https://www.qqzeng.com/ip/");
        m.put("dr", "");
        //页面title
        m.put("dt", "IP地址查询-【qqzeng-ip】");
        m.put("dw", "");
        //浏览器是否已启用 Java
        m.put("je", 1);
        //操作系统
        m.put("os", "Windows");
        //操作系统版本
        m.put("osv", "10");
        //时间戳
        m.put("r", new Date().getTime());
        //显示图像的调色板的位深度
        m.put("sd", "24-bit");
        m.put("sid", 1);
        //窗口大小
        m.put("sr", "1536x864");
        //浏览器的语言版本
        m.put("ul", "zh-cn");
        m.put("vp", "374x705");
        String json = JSONUtil.toJsonStr(m);
//        System.out.println(json);

        HttpCookie cookie1 = new HttpCookie("qqzeng_cid", cid);
        HttpCookie cookie2 = new HttpCookie("f_ip", "qqzengip");

        String user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 Edg/103.0.1264.77";
        //模拟请求获取Cookie
        String body = HttpRequest.get("https://www.qqzeng-ip.com/api/analytics")
                .form(json)
                .cookie(cookie1, cookie2)
                .header(Header.REFERER, "https://www.qqzeng.com/")
                .header(Header.USER_AGENT,user_agent)
                .execute().body();
        return cid;
    }
    /**
     * 第三方IP地址查询API
     * 爬取：IP地址查询-【qqzeng-ip】
     *
     * @param ip IP地址
     * @return ip:ip, continent:大洲, country:国家.
     * province:省份, city:城市, district:地区,
     * isp:服务商, areacode:地区码
     * lng:lng, lat:纬度
     */
    public static Map<String, Object> getIPInfo1(String ip) {
        HashMap<String, Object> map = new HashMap<>();
        long time = new Date().getTime();
        String jq = "jQuery" + ("2.1.4" + Math.random()).replace(".", "");
        String Url = "https://www.qqzeng-ip.com/api/ip?" +
                "callback=" + jq +
                "_" + (time - RandomUtil.createRandom(1, 50)) + "&" +
                "ip=" + ip + "&_=" + time;
        String cid = getIPInfo1Cookie();
        HttpCookie cookie1 = new HttpCookie("qqzeng_cid", cid);
        HttpCookie cookie2 = new HttpCookie("f_ip", "qqzengip");
        String s = HttpRequest.get(Url)
                .header(Header.REFERER, "https://www.qqzeng.com/")
                .cookie(cookie1, cookie2)
                .execute().body();
        s = StrUtil.sub(s, s.indexOf('(') + 1, -1);
        s = UnicodeUtil.toString(s);
        if (s.indexOf("Error request") < 0) {
            Integer code = JSONUtil.parseObj(s).getInt("code");
            if (code.equals(0)) {
                JSONObject data = JSONUtil.parseObj(s).getJSONObject("data");
                String areacode = data.getStr("areacode");
                if (!areacode.equals("购买")) {
                    //查询ip
                    map.put("ip", data.getStr("ip"));
                    //大洲
                    map.put("continent", data.getStr("continent"));
                    //国家
                    map.put("country", data.getStr("country"));
                    //省份
                    map.put("province", data.getStr("province"));
                    //城市
                    map.put("city", data.getStr("city"));
                    //地区
                    map.put("district", data.getStr("district"));
                    //服务商
                    map.put("isp", data.getStr("isp"));
                    //地区码
                    map.put("areacode", areacode);
                    //经度
                    map.put("lng", data.getStr("lng"));
                    //纬度
                    map.put("lat", data.getStr("lat"));
                } else {
//                    Log.error("第三方[https://www.qqzeng-ip.com/api/ip]IP查询API权限不足");
                }
            } else {
                Log.error("第三方[https://www.qqzeng-ip.com/api/ip]IP查询API请求失败");
            }
        } else {
            Log.error("第三方[https://www.qqzeng-ip.com/api/ip]IP查询API请求错误");
        }
        return map;
    }
    /**
     * 将IP地址查询结果格式化为字符串
     *
     * @param map getIPInfo1()
     * @return
     */
    public static String getIPInfo1_str(Map<String, Object> map) {
        String str = "";
        if (!map.isEmpty()) {
            String ip = !StrUtil.isEmptyIfStr(map.get("ip")) ? map.get("ip").toString() : "无";
            String con = !StrUtil.isEmptyIfStr(map.get("continent")) ? map.get("continent").toString() : "无";
            String cou = !StrUtil.isEmptyIfStr(map.get("country")) ? map.get("country").toString() : "无";
            String pr = !StrUtil.isEmptyIfStr(map.get("province")) ? map.get("province").toString() : "无";
            String ci = !StrUtil.isEmptyIfStr(map.get("city")) ? map.get("city").toString() : "无";
            String di = !StrUtil.isEmptyIfStr(map.get("district")) ? map.get("district").toString() : "无";
            String is = !StrUtil.isEmptyIfStr(map.get("isp")) ? map.get("isp").toString() : "无";
            String adcode = !StrUtil.isEmptyIfStr(map.get("areacode")) ? map.get("areacode").toString() : "无";
            String lng = !StrUtil.isEmptyIfStr(map.get("lng")) ? map.get("lng").toString() : "无";
            String lat = !StrUtil.isEmptyIfStr(map.get("lat")) ? map.get("lat").toString() : "无";
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(ip).append("]")
                    .append(con).append("-")
                    .append(cou).append("-")
                    .append(pr).append("-")
                    .append(ci).append("-")
                    .append(di).append("-")
                    .append(is).append("-")
                    .append("<").append(adcode).append(">")
                    .append("{").append(lng).append(",").append(lat).append("}");
            str = sb.toString();
        }
        return str;
    }
}
