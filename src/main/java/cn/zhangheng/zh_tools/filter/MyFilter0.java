package cn.zhangheng.zh_tools.filter;

import cn.zhangheng.zh_tools.bean.SettingConfig;
import com.zhangheng.util.CusAccessObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-07-10 14:56
 * @version: 1.0
 * @description: 初始化请求消息
 */
@WebFilter
@Order(0)
public class MyFilter0 extends MyFilter {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Value("#{'${server.servlet.context-path}'}")
    private String contextPath;
    @Value("#{'${my-filter0.exclude-path}'.split(',')}")
    private String[] excludePath;
    @Autowired
    private SettingConfig setting;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = CusAccessObjectUtil.getUri(req);
        uri=handleUrl(uri,contextPath);
        boolean isFilter=true;
        isFilter = isFilter(excludePath,uri,isFilter);
        if (isFilter) {
            String ip = CusAccessObjectUtil.getClientIp(req,setting.getIpHeaders());
            mySession.put(req);
            String user_agent = CusAccessObjectUtil.getUser_Agent(req);
            HttpSession session = req.getSession();
            session.setAttribute("ip", ip);
            session.setAttribute("user_agent", user_agent);
        }
        chain.doFilter(request, response);
    }
}
