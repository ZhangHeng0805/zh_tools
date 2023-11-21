package cn.zhangheng.zh_tools.config.listener;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.bean.WebLog;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import cn.zhangheng.zh_tools.service.entity_service.WebLogService;
import cn.zhangheng.zh_tools.util.MySession;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-11-05 11:34
 */
@WebListener
@Configuration
public class MySessionListener implements HttpSessionListener, HttpSessionAttributeListener {
    @Autowired
    private VisitorService visitorService;
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private WebLogService webLogService;
    private static HashSet sessions;
    @Autowired
    private MySession mySession;


    @Override
    public synchronized void sessionCreated(HttpSessionEvent se) {
//        HttpSession session = se.getSession();
//        String ip = Convert.toStr(session.getAttribute("ip"),"");
//        String user_agent = Convert.toStr(session.getAttribute("user_agent"));
//        MySession.Visit visit = mySession.find(ip, user_agent);
//        if (session.getAttribute("phone_info") == null) {
//            ServletContext application = session.getServletContext();
//            // 在application范围由一个HashSet集保存所有的session
//            sessions = (HashSet) application.getAttribute("sessions");
//            if (sessions == null) {
//                sessions = new HashSet();
//            }
//            // 新创建的session均添加到HashSet集中
//            boolean add = sessions.add(session);
//            application.setAttribute("sessions", sessions);
//            // 可以在别处从application范围中取出sessions集合
//            // 然后使用sessions.size()获取当前活动的session数，即为“在线人数”
//            if (add)
//                log.info("\nsession监听：用户[{}]上线，当前在线人数：{}人", session.getId(), sessions.size()+"\n");
//        }

    }

    @Override
    public synchronized void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        String _ip = Convert.toStr(session.getAttribute("ip"), "");
        String user_agent = Convert.toStr(session.getAttribute("user_agent"), "");
        if (StrUtil.isBlank(_ip) || StrUtil.isBlank(user_agent))
            return;
        MySession.Visit visit = mySession.find(_ip, user_agent);
        if (visit == null)
            return;
//        ServletContext application = session.getServletContext();
//        sessions = (HashSet) application.getAttribute("sessions");
        try {
            Map<String, Object> attr = visit.getAttr();
            Visitor visitor = (Visitor) attr.get("visitor");
            if (visitor != null) {
                boolean b = visitorService.saveAndflush(visitor);
            } else {
                return;
            }
            String creationTime = TimeUtil.toTime(visit.getDate());
//            String lastAccessedTime = TimeUtil.toTime(new Date(session.getLastAccessedTime()));
            WebLog webLog = new WebLog();
//            String ip = visitor.getIp();
            String ip = _ip;
            Optional<Visitor> byId = visitorService.findById(ip);
            if (byId.isPresent()) {
                webLog.setLocation(byId.get().getLocation());
            } else {
                webLog.setLocation("暂无信息");
            }
            webLog.setIp(ip);
            String sessionId = visit.getId();
            webLog.setSession_id(sessionId);
            Integer count = Convert.toInt(attr.get("_c"));
            webLog.setCount(count);
            webLog.setTime(creationTime);
            webLog.setUser_agent(user_agent);

            webLogService.saveAndFlush(webLog);
            log.info("\nsession监听：用户[{}]下线，访问次数[{}]，session创建时间[{}]", ip, count, creationTime);
        } catch (Exception e) {
//            e.printStackTrace();
            log.error(e.toString());
        } finally {
            // 销毁的session均从HashSet集中移除
            try {
                mySession.remove(visit);
            } catch (Exception e) {
                mySession.clear();
                e.printStackTrace();
            }
            log.info("\nsession监听：当前在线人数：{}人", mySession.getVisitors().size());
        }
    }
}
