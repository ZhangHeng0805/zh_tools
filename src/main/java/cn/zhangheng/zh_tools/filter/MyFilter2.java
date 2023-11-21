package cn.zhangheng.zh_tools.filter;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import cn.zhangheng.zh_tools.bean.StatusCode;
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
import java.io.IOException;
import java.util.Map;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-07-10 14:33
 * @version: 1.0
 * @description: 请求次数过滤
 */
@WebFilter
@Order(2)
public class MyFilter2 extends MyFilter {

    private Logger log= LoggerFactory.getLogger(getClass());
    @Value("#{'${server.servlet.context-path}'}")
    private String contextPath;
    @Value("#{'${my-filter2.exclude-path}'.split(',')}")
    private String[] excludePath;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = CusAccessObjectUtil.getUri(req);
        uri=handleUrl(uri,contextPath);
        boolean isFilter=true;
        isFilter = isFilter(excludePath,uri,isFilter);
        if (isFilter){
            Map<String, Object> attr = getVisitor(req).getAttr();
            Integer sessionCount = Convert.toInt(attr.get("_c"), 0);
            sessionCount = sessionCount + 1;
            attr.put("_c", sessionCount);
            if (sessionCount > setting.getMaxRequestCounts()) {
                Message msg = new Message();
                msg.setCode(403);
                msg.setTime(TimeUtil.getNowTime());
                msg.setTitle("请求达上限!");
                msg.setMessage(StatusCode.Http403);
                wirterJson(response, JSONUtil.parse(msg).toString(), msg.getCode());
                log.warn("\n请求次数过多,路径[{}]-次数[{}]",uri,sessionCount);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
