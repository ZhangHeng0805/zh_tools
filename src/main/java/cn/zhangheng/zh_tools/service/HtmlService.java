package cn.zhangheng.zh_tools.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.zhangheng.zh_tools.bean.AndroidLog;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.bean.WebLog;
import cn.zhangheng.zh_tools.dao.WebLogDao;
import cn.zhangheng.zh_tools.service.entity_service.AndroidLogService;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import com.zhangheng.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-03-14 16:14
 * @version: 1.0
 * @description:
 */
@Service
public class HtmlService {
    @Autowired
    private WebLogDao webLogDao;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private AndroidLogService androidLogService;
    @Autowired
    private SettingConfig setting;


    /**
     * 获取指定时间的访问报表
     *
     * @param time yyyy-MM-dd
     * @return
     */
    public StringBuilder getHtmlString1(String time) {
        List<Visitor> visitors = visitorService.findAllByLast_timeLike(time);
        List<WebLog> webLogs = webLogDao.findAllByTimeLike(time);
        List<AndroidLog> androidLogs = androidLogService.findAllByTimeLike(time);
        int before = 30;
        List<Map<String, Object>> webLogBeforeByDay = webLogDao.findBeforeByDay(before);
        List<Map<String, Object>> androidLogBeforeByDay = androidLogService.findBeforeByDay(before);

        StringBuilder sb = new StringBuilder();
        sb.append("<html lang=\"zh\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width\">");
        sb.append("<title>星曦向荣报表-" + time + "用户的访问记录</title></head>");
        sb.append("<body>");
        //sb.append("<script>" + StrUtil.subAfter(HttpUtil.get("https://cdn.staticfile.org/echarts/4.3.0/echarts.min.js"), "\n\n", true) + "</script>");
        sb.append("<script src='https://cdn.staticfile.org/echarts/4.3.0/echarts.min.js'></script>");

        if (webLogBeforeByDay.size()>0){
            List<String> key = new ArrayList<>();
            List<Integer> value1 = new ArrayList<>();
            List<Integer> value2 = new ArrayList<>();
            Long count1=0L;//webLog次数
            Long count2=0L;//androidLog次数
            for (int i=0;i<webLogBeforeByDay.size();i++) {
                Map<String, Object> webLog = webLogBeforeByDay.get(i);
                Map<String, Object> android = androidLogBeforeByDay.get(i);
                key.add((String) webLog.get("date"));
                Integer num1 = Convert.toInt(webLog.get("num"));
                value1.add(num1);
                count1+=num1;
                Integer num2 = Convert.toInt(android.get("num"));
                value2.add(num2);
                count2+=num2;
            }
            long Total1 = webLogDao.sumByCount();
            Long Total2 = androidLogService.count();
            sb.append("<div style='text-align: center'>");
            sb.append("<h3><a href='"+setting.getMainUrl()+"2'>"  + "近"+before+"天用户总访问-数据统计图</a></h3>");
            sb.append("<div id=\"line-chart1\" style=\"width: 100%;height: 400px\"></div>");
            sb.append("<p>近"+before+"天普通访问量总计：<mark>" + count1 + "</mark>, 总访问量：<mark>" + Total1 + "</mark></p>");
            sb.append("<p>近"+before+"天安卓访问量总计：<mark>" + count2 + "</mark>, 总访问量：<mark>" + Total2 + "</mark></p>");
            sb.append("</div>");
            sb.append("<script type=\"text/javascript\">");
            sb.append("var myChart1 = echarts.init(document.getElementById('line-chart1'));");
            sb.append("var option1 = {");
            sb.append("title: {text:''},");
            sb.append("legend: {data: ['普通访问量','安卓访问量']},");
            sb.append("xAxis: {data: " + JSONUtil.parse(key).toString() + "},");
            sb.append("tooltip: {},");
            sb.append("yAxis: {},");
            sb.append("series: [" +
                    "{name:'普通访问量',type:'line',data: " + JSONUtil.parse(value1).toString() + "}," +
                    "{name:'安卓访问量',type:'bar',data: " + JSONUtil.parse(value2).toString() + "}," +
                    "],");
            sb.append("};");
            sb.append("myChart1.setOption(option1);");
            sb.append("</script>");
            sb.append("<hr>");
        }
        if (webLogs.size() > 0||androidLogs.size()>0) {
            List<Integer> value1 = new ArrayList<>();
            List<Integer> value2 = new ArrayList<>();
            Integer count1,count2;
            count1=count2=0;
            List<String> X=new ArrayList<>();
            String now;
            if (TimeUtil.getNowTime().indexOf(time)<0)
                now="23";
            else
                now= TimeUtil.getTimeInfo(TimeUtil.Hour);
            for (int n=0;n<= Convert.toInt(now);n++){
                Integer num1=0;
                Integer num2=0;
                X.add(n+"时");
                for (WebLog webLog : webLogs) {
                    String h = webLog.getTime().substring(10, 13);
                    if (Convert.toInt(h).equals(n)){
                        Integer c = webLog.getCount();
                        count1+=c;
                        num1+=c;
                    }
                }
                value1.add(n,num1);
                for (AndroidLog androidLog : androidLogs) {
                    String h = androidLog.getTime().substring(10, 13);
                    if (Convert.toInt(h).equals(n)){
                        count2++;
                        num2++;
                    }
                }
                value2.add(n,num2);
            }

                sb.append("<div style='text-align: center'>");
                sb.append("<h3><a href='"+setting.getMainUrl()+"2'>" + time + " 用户总访问-各时段分布图</a></h3>");
                sb.append("<div id=\"line-chart2\" style=\"width: 100%;height: 400px\"></div>");
                sb.append("<p>普通访问量总计：<mark>" + count1 + "</mark>, 安卓访问量总计：<mark>" + count2 + "</mark></p>");
                sb.append("<p>统计时间段：[" + X.get(0) + "] —— [" + X.get(X.size() - 1) + "]</p>");
                sb.append("</div>");
                sb.append("<script type=\"text/javascript\">");
                sb.append("var myChart1 = echarts.init(document.getElementById('line-chart2'));");
                sb.append("var option1 = {");
                sb.append("title: {text:''},");
                sb.append("legend: {data: ['普通访问量','安卓访问量']},");
                sb.append("xAxis: {data: " + JSONUtil.parse(X).toString() + "},");
                sb.append("tooltip: {},");
                sb.append("yAxis: {},");
                sb.append("series: [" +
                        "{name:'普通访问量',type:'line',data: " + JSONUtil.parse(value1).toString() + "}," +
                        "{name:'安卓访问量',type:'bar',data: " + JSONUtil.parse(value2).toString() + "}," +
                        "],");
                sb.append("};");
                sb.append("myChart1.setOption(option1);");
                sb.append("</script>");
                sb.append("<hr>");

                List<Data> list = handleLocation(webLogs);
                if (list.size()>0) {
                    String json = JSONUtil.parse(list).toString();
                    sb.append("<div style='text-align: center'>");
                    sb.append("<h3><a href='"+setting.getMainUrl()+"2'>" + time + " 普通用户访问-地区分布</a></h3>");
                    sb.append("<div id=\"sunburst-chart1\" style=\"width: 100%;height:400px;\"></div>");
                    sb.append("<script type=\"text/javascript\">");
                    sb.append("var myChart2 = echarts.init(document.getElementById('sunburst-chart1'));");
                    sb.append("option2 = {");
                    sb.append("tooltip: {},");
                    sb.append("series: {");
                    sb.append("name:'全球访问',");
                    sb.append("type: 'sunburst',");
                    sb.append("data: " + json + ",");
                    sb.append("radius: [0, '100%'],");
                    sb.append("label: {rotate: 'radial'}");
                    sb.append(" }");
                    sb.append("};");
                    sb.append("myChart2.setOption(option2);");
                    sb.append("</script>");
                    sb.append("</div><hr>");
                }
        }

        if (visitors.size() > 0) {
            sb.append("<div style='text-align: center'>");
            sb.append("<h3>" + time + " 普通用户成功访问-记录</h3>");
            sb.append("<table border=\"1\" style='width: 100%'>");
            sb.append("<tr>");
            sb.append("<th>").append("时间").append("</th>");
            sb.append("<th>").append("IP").append("</th>");
            sb.append("<th>").append("位置").append("</th>");
            sb.append("<th>").append("次数").append("</th>");
            sb.append("<th>").append("总数").append("</th>");
            sb.append("<th>").append("状态").append("</th>");
            sb.append("</tr>");
            String color = "green";
            String state = "正常";
            for (Visitor v : visitors) {
                sb.append("<tr>");
                sb.append("<td>").append(v.getLast_time()).append("</td>");
                sb.append("<td>").append(v.getIp()).append("</td>");
                sb.append("<td>").append(v.getLocation()).append("</td>");
                sb.append("<td>").append(v.getCount()).append("</td>");
                sb.append("<td>").append(v.getTotal()).append("</td>");
                if (v.getState().equals(0)) {
                    color = "green";
                    state = "正常";
                } else {
                    color = "red";
                    state = "封禁";
                }
                sb.append("<td style='color:" + color + "'>").append(state).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("<p>总计:" + visitors.size() + "条数据</p>");
            sb.append("<p><a href='" + setting.getMainUrl() + "'>星曦向荣网</a></p>");
            sb.append("</div>");
        }

        sb.append("</body>");
        sb.append("</html>");
        return sb;
    }

    /**
     * 处理位置分布信息
     * @param webLogs
     * @return
     */
    public List<Data> handleLocation(List<WebLog> webLogs) {
//        List<Visitor> visitors = visitorService.findAllByLast_timeLike(time);
        List<Data> list = new ArrayList<>();
        for (int i = 0; i < webLogs.size(); i++) {
            WebLog v = webLogs.get(i);
            String location = v.getLocation();
            if (!StrUtil.isBlank(location)) {
                String[] split = location.split("-");
                if (split.length > 4) {
                    List<Data> temp_L = list;
                    Data temp_D = null;
                    for (int n = 0; n < 4; n++) {
                        String name = split[n];
                        if (!name.equals("无")) {
                            Map<String, Object> map = excu1(temp_L, name);
                            if (temp_D != null)
                                temp_D.children = (List<Data>) map.get("list");
                            Data data = (Data) map.get("data");
                            temp_D = data;
                            temp_L = data.children;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }

    private static Map<String, Object> excu1(List<Data> data, String name) {
        Map<String, Object> map = new HashMap<>();
        if (data == null)
            data = new ArrayList<>();
        Data d;
        for (int i = 0; i < data.size(); i++) {
            d = data.get(i);
            if (d.name.equals(name)) {
                map.put("index", i);
                d.value += 1;
                map.put("data", d);
                data.set(i, d);
                map.put("list", data);
                return map;
            }
        }
        d = new Data();
        d.name = name;
        d.value = 1;
        data.add(d);
        map.put("index", data.indexOf(d));
        map.put("list", data);
        map.put("data", d);
        return map;
    }

    private static class Data {
        String name;
        Integer value = 0;
        List<Data> children;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public List<Data> getChildren() {
            return children;
        }

        public void setChildren(List<Data> children) {
            this.children = children;
        }
    }

}
