package cn.zhangheng.zh_tools.config.interceptor;

import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.bean.StatusCode;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.dao.IPBlackDao;
import cn.zhangheng.zh_tools.service.EmailService;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import cn.zhangheng.zh_tools.util.MySession;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Optional;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-18 23:39
 */

/**
 * 请求记录，拦截
 */
@Configuration
public class BlackListInterceptor implements HandlerInterceptor {
    @Autowired
    private VisitorService visitorService;
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private SettingConfig setting;
    @Autowired
    private IPBlackDao ipBlackDao;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MySession mySession;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        HttpSession session = request.getSession();
        Map<String, Object> attr = mySession.find(CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders()), CusAccessObjectUtil.getUser_Agent(request)).getAttr();
        /**
         * 请求角色校验
         */
        //获取访问ip
        String ip = CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders());
        /*获取访问者信息*/
        //session获取访问者信息
        Visitor visit = (Visitor) attr.get("visitor");
        //session没有访问者信息
        if (visit == null) {
            //数据库中获取访问者信息
            Optional<Visitor> byIp = visitorService.getVisitorByIp(ip);
            //数据库中存在
            if (byIp.isPresent()) {
                visit = byIp.get();
            } else {//数据库中不存在
                //创建新对象,赋值，存数据库
                visit = visitorService.setRequest(request);
                visit.setCount(1);
                visit.setTotal(1);
                visit.setState(0);
                visit.setFirst_time(TimeUtil.getNowTime());
                try {
                    boolean b = visitorService.saveAndflush(visit);
                    if (b) {//保存信息，放行
                        attr.put("visitor", visit);
                        return true;
                    }
                } catch (Exception e) {//异常，拦截
                    log.error(e.toString());
                    response.sendError(500, StatusCode.Http500);
                    return false;
                }
            }
        }
        attr.put("visitor", visit);
        /*验证访问者信息是否合法*/
        //验证ip是否变更
        if (setting.getIP_change_verify()&&!visit.getIp().equals(ip)) {
            //session.invalidate();
            response.sendError(401,StatusCode.Http401);
            return false;
        }
//        String code403 = "您请求已达到限制【(￣^￣゜)尴尬】，下次再来玩啊【(҂‾ ▵‾)σ(˚▽˚’!)/随叫随到】，【ヾ(￣▽￣)Bye~Bye~】";
        //验证状态
        if (visit.getState().equals(1)) {
            response.sendError(403, StatusCode.Http403);
            return false;
        }
        //验证请求次数
        if (visit.getCount() >= setting.getMaxRequestCounts()) {
            response.sendError(403,StatusCode.Http403);
            return false;
        }
        /*更新访问者信息*/
        visit.setUser_agent(CusAccessObjectUtil.getUser_Agent(request));
        visit.setLast_time(TimeUtil.getNowTime());
        visit.setCount(visit.getCount() + 1);
        visit.setTotal(visit.getTotal() + 1);
        //请求到达限制邮件预警
        if (visit.getCount().equals(setting.getMaxRequestCounts())) {
            if (!ipBlackDao.existsByIpAndFlag(ip,"w")) {
                visit.setState(1);
                sendEmail(request, visit);
            }else {
                visit.setCount(1);
            }
        }
        attr.put("visitor", visit);

        return true;
    }

    private void sendEmail(HttpServletRequest request, Visitor visit) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        String requestURl = request.getRequestURL().toString();
        sb.append("<p>请求路径：[" + request.getMethod() + "]" + URLDecoder.decode(requestURl, "UTF-8") + "</p>");
        sb.append("<p>请求者ip：" + visit.getIp() + "</p>");
        sb.append("<p>请求者位置：" + visit.getLocation() + "</p>");
        sb.append("<p>请求者User_Agent：" + visit.getUser_agent() + "</p>");
        sb.append("<p>请求者最近访问时间：" + visit.getLast_time() + "</p>");
        sb.append("<p>请求者最近访问次数：" + visit.getCount() + "</p>");
        sb.append("<p>请求者访问总数：" + visit.getTotal() + "</p>");
        String state = visit.getState().equals(0) ? "正常" : "封禁";
        sb.append("<p>请求者当前状态：" + state + "</p>");
        emailService.send("ZH Tools请求次数请求次数达到限制", sb.toString(), true);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        /*打印指定状态码的响应日志*/
//        Integer status = response.getStatus();
//        Boolean isPrintAllRequestInfo = setting.getIsPrintAllRequestInfo();
//        if (!isPrintAllRequestInfo) {
//            @NotNull List<Integer> acc = setting.getAccessPrintCode();
//            if (acc.indexOf(status) > -1) {
//                Visitor visitor = (Visitor) request.getSession().getAttribute("visitor");
//                String msg;
//                String requestURI = request.getRequestURI();
//                requestURI = URLDecoder.decode(requestURI, "UTF-8");
//                if (visitor == null) {
//                    String req = CusAccessObjectUtil.getRequst(request);
//                    msg = "(" + status + ")-[" + request.getMethod() + "]" + requestURI + ":" + req;
//                } else {
//                    String location = "[" + visitor.getIp() + "] " + visitor.getLocation() + "（" + visitor.getCount() + "）\n" + visitor.getUser_agent();
//                    msg = "(" + status + ")-[" + request.getMethod() + "]" + requestURI + ":" + location;
//                }
//                log.info("\n"+msg+"\n");
//            }
//        }
    }
}
