package cn.zhangheng.zh_tools.filter;

import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.util.MySession;
import com.zhangheng.util.CusAccessObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.Filter;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-04-26 15:54
 * @version: 1.0
 * @description:
 */
@WebFilter
public abstract class MyFilter implements Filter {
    @Autowired
    protected SettingConfig setting;
    @Autowired
    protected MySession mySession;

    protected MySession.Visit _Visitor;

//    public static List<Map<String,Object>> vivMaps=new ArrayList<>();

    public  MySession.Visit getVisitor(HttpServletRequest req){
        return mySession.find(CusAccessObjectUtil.getClientIp(req,setting.getIpHeaders()), CusAccessObjectUtil.getUser_Agent(req));
    }
    /**
     * 地址过滤判断
     * @param paths url地址数组
     * @param url 判断的url
     * @param defualt 默认值
     * @return 若存在，返回非默认值；不存在，则返回默认值
     */
    protected boolean isFilter(String[] paths,String url, boolean defualt) {
        for (String s : paths) {
            if (url.startsWith(s.replace("*", ""))) {
                defualt=!defualt;
                break;
            }
        }
        return defualt;
    }
    protected String handleUrl(String url,String contextPath){
        url=url.startsWith("//")?url.replace("//","/"):url;
        if (!contextPath.equals("/")){
            url=url.replace(contextPath,"");
        }
        return url;
    }
    protected ServletResponse wirterJson(ServletResponse response, String json) throws IOException {
        return wirterJson(response,json,200);
    }
    protected ServletResponse wirterJson(ServletResponse response, String json,Integer state) throws IOException {
        HttpServletResponse resp= (HttpServletResponse) response;
        resp.setStatus(state);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(json);
        writer.close();
        return response;
    }

}
