package cn.zhangheng.zh_tools.filter;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import cn.zhangheng.zh_tools.bean.StatusCode;
import cn.zhangheng.zh_tools.util.MySession;
import com.zhangheng.bean.Message;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-04-20 10:32
 * @version: 1.0
 * @description: 请求频率过滤
 */
@WebFilter
@Order(1)
public class MyFilter1 extends MyFilter {
    private Logger log= LoggerFactory.getLogger(getClass());
//    @Autowired
//    private SettingConfig setting;
    @Value("#{'${server.servlet.context-path}'}")
    private String contextPath;
    @Value("#{'${my-filter1.exclude-path}'.split(',')}")
    private String[] excludePath;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = CusAccessObjectUtil.getUri(req);
        uri=handleUrl(uri,contextPath);
        boolean exclusive = true;
        exclusive=isFilter(excludePath,uri,exclusive);
        if (exclusive) {
            long nowTime = new Date().getTime();
            MySession.Visit visit = getVisitor(req);
            Map<String, Object> attr = visit.getAttr();
            Long sessionTime = Convert.toLong(attr.get(uri+"_t"), null);
            attr.put(uri+"_t", nowTime);
            if (sessionTime != null) {
                //判断请求间隔时间
                long abs = Math.abs(nowTime - sessionTime);
                if (abs < setting.getRequestInterval()) {
                    Message msg = new Message();
                    msg.setCode(503);
                    msg.setTime(TimeUtil.getNowTime());
                    msg.setTitle("请求频繁!");
                    msg.setMessage(StatusCode.Http503);
                    wirterJson(response, JSONUtil.parse(msg).toString(),msg.getCode());
                    log.warn("\n请求频率过快,路径[{}]-间隔[{}ms]",uri,abs);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
