package cn.zhangheng.zh_tools.reptile;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangheng.log.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频API
 *
 * @author 张恒
 * @program: reptile
 * @email zhangheng.0805@qq.com
 * @date 2022-09-30 08:23
 */
public class VideoAPI {

    /**
     * 影视API
     * 爬取：茶杯狐
     *
     * @param name 影视名
     * @return [{ name:影视名, url:播放链接, web_name:播放平台 }]
     */
    public static List<Map<String, String>> getVideoByCupfox1(String name) {
        String url = "https://cupfox.app/search?key=" + name;
        List<Map<String, String>> list = null;
        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println(doc);
            Elements result_list = doc.getElementsByClass("search-result-list").select("div").select("a");
            list = new ArrayList<>();
            for (Element e : result_list) {
                //播放地址
                String href = e.select("a").attr("href");
                //播放站名
                String web_name = e.select("span.website-name").text();
                //影视名
                String em = e.select("em").text();
                Map<String, String> video = new HashMap<>();
                video.put("name", em);
                video.put("url", href);
                video.put("web_name", web_name);
                list.add(video);
            }
        } catch (Exception e) {
            Log.error("第三方影视[https://cupfox.app/search]API错误：" + e.getMessage());
        }
        return list;
    }

    public static List<Map<String, String>> getVideoByCupfox(String name) {
        String url = "https://cupfox.app/search?key=" + name;
        List<Map<String, String>> list = null;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements script = doc.select("script");
            String text = script.get(script.size() - 1).html();
            String jsonStr = UnicodeUtil.toString(text);
            JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONObject("props")
                    .getJSONObject("pageProps")
                    .getJSONObject("resourceSearchResult")
                    .getJSONArray("resources");
            list = new ArrayList<>();
            for (Object o : jsonArray) {
                Map<String, String> map = new HashMap<>();
                JSONObject obj = JSONUtil.parseObj(o);
                String title = obj.getStr("text");
                map.put("name", title);
                String href = obj.getStr("url");
                map.put("url", href);
                String website = obj.getStr("website");
                map.put("web_name", website);
                String icon = obj.getStr("icon");
                map.put("web_icon", icon);
                JSONArray tags = obj.getJSONArray("tags");
                StringBuilder tags_sb = new StringBuilder();
                for (int i = 0; i < tags.size(); i++) {
                    String str = tags.get(i).toString();
                    if (i == tags.size() - 1) {
                        tags_sb.append(str);
                    } else {
                        tags_sb.append(str+",");
                    }
                }
                map.put("tag_name", tags_sb.toString());
                list.add(map);
            }
        } catch (Exception e) {

        }
        return list;
    }
}
