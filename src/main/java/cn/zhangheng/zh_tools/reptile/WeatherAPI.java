package cn.zhangheng.zh_tools.reptile;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangheng.log.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 天气API
 * @author 张恒
 * @program: reptile
 * @email zhangheng.0805@qq.com
 * @date 2022-09-30 07:53
 */
public class WeatherAPI {

    /**
     * 天气查询API
     * 爬取：中国气象局
     * 位置路径 降雨量 温度 气压 湿度 风向 风标 风速 更新时间 预警信息[标题,内容,发布时间]
     *
     * @param city 城市
     * @return path precipitation temperature pressure humidity windDirection
     * windScale windSpeed lastUpdate alarm[{title,content,time]}
     */
    public static Map<String, Object> getWeatherByQiXiangJu(String city) {
        Map<String, Object> map = null;
        try {
            map = new HashMap<>();
            //自动填充接口，查询城市编码
            String Url = "https://weather.cma.cn/api/autocomplete?q=" + URLEncoder.encode(city,"UTF-8") + "&limit=5&timestamp=" + new Date().getTime();
//        System.out.println(Url);
            String s = HttpUtil.get(Url);
//        System.out.println(JSONUtil.formatJsonStr(s));

            JSONArray data = JSONUtil.parseObj(s).getJSONArray("data");
            String o = data.get(0).toString();
            String[] split = o.split("\\|");
//        System.out.println(split[0]+"-"+split[1]);
            //查询当前天气
            String adcode = split[0];
            String Url_now = "https://weather.cma.cn/api/now/" + adcode;
            String s1 = HttpUtil.get(Url_now);
//        System.out.println(JSONUtil.formatJsonStr(s1));

            String msg = JSONUtil.parseObj(s1).getStr("msg");
            if (msg.equals("success")) {
                JSONObject data1 = JSONUtil.parseObj(s1).getJSONObject("data");
                //位置路径
                String path = data1.getJSONObject("location").getStr("path").replace(", ", "-");
                map.put("path", path);
                //当前天气
                JSONObject now = data1.getJSONObject("now");
                //降雨量
                String precipitation = now.getFloat("precipitation") > 0 ? now.getFloat("precipitation") + "mm" : "无降水";
                precipitation = precipitation.equals("9999.0mm") ? "" : precipitation;
                map.put("precipitation", precipitation);
                //温度
                String temperature = now.getFloat("temperature") + "℃";
                temperature = temperature.equals("9999.0℃") ? "" : temperature;
                map.put("temperature", temperature);
                //气压
                String pressure = now.getFloat("pressure") + "hPa";
                pressure = pressure.equals("9999.0hPa") ? "" : pressure;
                map.put("pressure", pressure);
                //湿度
                String humidity = now.getFloat("humidity") + "%";
                humidity = humidity.equals("9999.0%") ? "" : humidity;
                map.put("humidity", humidity);
                //风向
                String windDirection = now.getStr("windDirection");
                windDirection = windDirection.equals("9999") ? "" : windDirection;
                map.put("windDirection", windDirection);
                //风标
                String windScale = now.getStr("windScale");
                windScale = windScale.equals("9999") ? "" : windScale;
                map.put("windScale", windScale);
                //风速
                String windSpeed = now.getFloat("windSpeed") + "m/s";
                windSpeed = windSpeed.equals("9999.0m/s") ? "" : windSpeed;
                map.put("windSpeed", windSpeed);
                //更新时间
                String lastUpdate = data1.getStr("lastUpdate");
                map.put("lastUpdate", lastUpdate);

                //预警
                JSONArray alarm = data1.getJSONArray("alarm");
                List<Map<String, String>> list = new ArrayList<>();
                for (Object a : alarm) {
                    Map<String, String> a_map = new HashMap<>();
                    JSON parse = JSONUtil.parse(a);
                    //预警标题
                    String title = parse.getByPath("title").toString();
                    a_map.put("title", title);
                    //内容
                    String content = parse.getByPath("signaltype").toString() + "-" + parse.getByPath("signallevel").toString();
                    a_map.put("content", content);
                    //发布时间
                    String time = parse.getByPath("effective").toString();
                    a_map.put("time", time);
                    list.add(a_map);
                }
                map.put("alarm", list);
            } else {
                Log.error("第三方气象局[https://weather.cma.cn/api/now/]天气查询错误："+ msg);
            }
        } catch (Exception e) {
            if (e.getMessage().indexOf("JSONObject") > 0) {
                Log.error("第三方气象局[https://weather.cma.cn/api/now/]天气查询异常：返回数据解析错误[JSONObject]");
            } else {
                Log.error("第三方气象局[https://weather.cma.cn/api/now/]天气查询异常："+e.getMessage());
            }
        }
        return map;
    }

    /**
     * 天气查询API
     * 爬取：天气网
     * @param city 城市名 例：武汉市-武汉
     * @return {city:城市名,date:日期,now:当前气温,weather:当日天气,shidu:湿度,feng:风向,
     * ziwaixian:紫外线强度,kongqi:空气质量,pm:PM2.5,sun:日出日落}
     */
    public static Map<String, String> getWeatherByTianQi(String city) {
        Map<String, String> map = new HashMap<>();
        String url1 = "https://www.tianqi.com/tianqi/ctiy?keyword=" + city;
        String s = HttpUtil.get(url1);
        try {
            JSONArray objects = JSONUtil.parseArray(s);
            if (!objects.isEmpty()) {
                Object o = objects.get(0);
                String url = JSONUtil.parseObj(o).getStr("url");
                Document doc = Jsoup.connect(url).get();
                Element weather_info = doc.selectFirst("dl.weather_info");
                //城市
                String h1 = weather_info.selectFirst("dd.name").selectFirst("h1").text();
                map.put("city", h1.replace("天气", ""));
                //时间
                String text = weather_info.selectFirst("dd.week").text();
                map.put("date", text);
                //当前气温
                String now = weather_info.selectFirst("p.now").text();
                map.put("now", now);
                //当日天气
                String span1 = weather_info.selectFirst("dd.weather").selectFirst("span").text();
                String span2 = weather_info.selectFirst("dd.weather").selectFirst("span").selectFirst("b").text();
                map.put("weather", span2 + " " + span1.replace(span2, ""));
                //湿度，风力，紫外线
                Elements b = weather_info.selectFirst("dd.shidu").select("b");
                String shidu = b.get(0).text();
                map.put("shidu",shidu);
                String feng = b.get(1).text();
                map.put("feng",feng);
                String ziwaixian = b.get(2).text();
                map.put("ziwaixian",ziwaixian);
                //空气质量，pm，日出日落
                Element element = weather_info.selectFirst("dd.kongqi");
                String kongqi = element.selectFirst("h5").text();
                map.put("kongqi",kongqi);
                String pm = element.selectFirst("h6").text();
                map.put("pm",pm);
                String sun = element.selectFirst("span").text();
                map.put("sun",sun);

            } else {
                Log.error("第三方天气[https://www.tianqi.com/]查询失败：没有找到该城市信息");
            }
        } catch (IOException e) {
            Log.error("第三方天气[https://www.tianqi.com/]查询错误：{}"+ e.getMessage());
        }
        return map;
    }
}
