package cn.zhangheng.zh_tools.reptile;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhangheng.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 音乐API
 * @author 张恒
 * @program: reptile
 * @email zhangheng.0805@qq.com
 * @date 2022-09-30 08:26
 */
public class MusicAPI {
    /**
     * 音乐API
     * 爬取：音乐直链搜索
     *
     * @param name 音乐名
     * @param type 类型[qq：QQ音乐；netease：网易云]
     * @param page 页码
     * @return [{ title:歌名, author:歌手, type:平台 ， pic:封面 ， url:文件地址 ， link:来源地址, lrc:歌词 }]
     */
    public static List<Map<String, Object>> getMusicByLiuzhijin(String name, String type, Integer page) {
        List<Map<String, Object>> list = null;
        type = type.replace("网易云", "netease");
        String url = "https://music.liuzhijin.cn/";
        Map<String, Object> body = new HashMap<>();
        body.put("input", name);
        if (page==null||page<1){
            page=1;
        }
        body.put("page", page);
        body.put("filter", "name");
        //qq：QQ音乐；netease：网易云
        body.put("type", type);
        String json = HttpRequest.post(url)
                .form(body)
                .header("X-Requested-With", "XMLHttpRequest")
                .contentType("application/x-www-form-urlencoded;charset=UTF-8")
                .execute().body();
        //Unicode转字符串
        json = UnicodeUtil.toString(json);
//        System.out.println(JSONUtil.formatJsonStr(json));
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Integer code = jsonObject.getInt("code");
        if (code.equals(200)) {
            list = new ArrayList<>();
            JSONArray data = jsonObject.getJSONArray("data");
            for (Object d : data) {
                String s = JSONUtil.toJsonStr(d);
                JSONObject obj = JSONUtil.parseObj(s);
                Map<String, Object> music = new HashMap<>();
                //歌名
                music.put("title", obj.getStr("title"));
                //歌手
                music.put("author", obj.getStr("author"));
                //平台
                music.put("type", obj.getStr("type"));
                //封面
                music.put("pic", obj.getStr("pic").replace("\\", ""));
                //文件地址
                music.put("url", obj.getStr("url").replace("\\", ""));
                //来源地址
                music.put("link", obj.getStr("link").replace("\\", ""));
                //歌词
                String lrc = obj.getStr("lrc");
                music.put("lrc", StrUtil.isEmptyIfStr(lrc) ?"":lrc.replace("\\", ""));
                list.add(music);
            }

        } else {
            Log.error("第三方音乐[" + url + "]API错误："+ jsonObject.getStr("error"));
        }
        return list;
    }

    /**
     * 音乐API
     * 爬取：音乐直链搜索
     *
     * @param name 音乐名
     * @param type 类型[qq：QQ音乐；netease：网易云]
     *
     * @return [{ title:歌名, author:歌手, type:平台 ， pic:封面 ， url:文件地址 ， link:来源地址, lrc:歌词 }]
     */
    public static List<Map<String, Object>> getMusicByLiuzhijin(String name, String type) {
        return getMusicByLiuzhijin(name,type,null);
    }
}
