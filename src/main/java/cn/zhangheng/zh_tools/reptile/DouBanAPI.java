package cn.zhangheng.zh_tools.reptile;

import com.zhangheng.log.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 豆瓣API
 * @author 张恒
 * @program: reptile
 * @email zhangheng.0805@qq.com
 * @date 2022-09-30 07:15
 */
public class DouBanAPI {
    /**
     * 豆瓣搜索
     *
     * @param key 搜索内容
     * @return [{url:豆瓣链接,img_url:封面,type:类型,name:名称,rating_nums:评分信息,
     * subject_cast:主题演员,introduce:介绍}]
     */
    public static List<Map<String, String>> getDouBanSearch(String key) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        String Url = "https://www.douban.com/search?q=" + key;
        try {
            Document doc = Jsoup.connect(Url).get();
            Elements div_results = doc.select("div.result");
            String href = null, img_src = null, type = null,
                    name = null, rating_nums = null, subject_cast = null, content = null;
            for (Element div_result : div_results) {
                try {
                    //豆瓣链接
                    href = div_result.selectFirst("a").attr("href");
                    //封面
                    img_src = div_result.selectFirst("img").attr("src");
                    //类型
                    type = div_result.selectFirst("span").text();
                    //名称
                    name = div_result.selectFirst("h3").selectFirst("a").text();
                    //评分信息
                    rating_nums = div_result.selectFirst("span.rating_nums").text() + "/" + div_result.select("div.rating-info").select("span").get(2).text();
                    //主题演员
                    subject_cast = div_result.selectFirst("span.subject-cast").text();
                    //介绍
                    content = div_result.selectFirst("p").text();

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("url", href);
                    map.put("img_url", img_src);
                    map.put("type", type);
                    map.put("name", name);
                    map.put("rating_nums", rating_nums);
                    map.put("subject_cast", subject_cast);
                    map.put("introduce", content);
                    list.add(map);
                } catch (Exception e) {
//                System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            Log.error("第三方豆瓣搜索[https://www.douban.com/search]查询异常:"+e.getMessage());
        }
        return list;
    }

    /**
     * 豆瓣电影top250电影数据API
     *
     * @param page 页码 [1,10]
     * @return [{name:电影名, url:豆瓣链接, img_url:封面地址, num:序号, rating_num:评分}]
     */
    public static List<Map<String, String>> getDouBanTop250(int page) {
        List<Map<String, String>> list = null;
        String Url = "https://movie.douban.com/top250";
        try {
            int i = page - 1;
            if (i > 0 && i < 10) {
                Url += "?start=" + (25 * i);
            }
            Document doc = Jsoup.connect(Url).get();
            String title = doc.title();
//            System.out.println(title);
            Elements result_list = doc.select("ol.grid_view").select("div.item");
            list = new ArrayList<Map<String, String>>();
            for (Element e : result_list) {
                Map<String, String> top250 = new HashMap<String, String>();
                //豆瓣链接
                String url = e.select("div.pic").select("a").attr("href");
                top250.put("url", url);
                //封面地址
                String img_url = e.select("div.pic").select("img").attr("src");
                top250.put("img_url", img_url);
                //序号
                String num = e.select("div.pic").select("em").text();
                top250.put("num", num);
                //评分
                String rating_num = e.select("span.rating_num").text();
                top250.put("rating_num", rating_num);
                //电影名
                String name = e.select("div.pic").select("img").attr("alt");
                top250.put("name", name);
//            System.out.println(num+".《"+name+"》["+rating_num+"] 封面："+img_url+" 地址："+url);

                list.add(top250);
            }
        } catch (Exception e) {
            Log.error("第三方豆瓣top250[" + Url + "]API错误：{}"+e.getMessage());
        }
        return list;
    }

}
