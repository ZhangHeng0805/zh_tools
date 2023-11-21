package cn.zhangheng.zh_tools.controller.error;

import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.StatusCode;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * 全局异常处理类
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-12-14 10:05
 */
@ControllerAdvice
public class MyExceptionHandler extends DefaultErrorAttributes {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Value("#{'${server.servlet.context-path}'}")
    private String contextPath;

    @ExceptionHandler(value = Throwable.class)
    public Exception e(Exception e,Map<String,Object> map){
        log.error("\n全局异常捕获:{}\n", e.toString());
        return e;
    }
    @ExceptionHandler({Exception.class})
    public String handleException(Exception e, HttpServletRequest request, Map<String,Object> map){
//        request.setAttribute("javax.servlet.error.status_code",code);
//        System.out.println("error："+JSONUtil.parse(map).toStringPretty());
        map.put("message",e.toString());
        return "forward:/error";
    }


    @ExceptionHandler(value = org.springframework.transaction.CannotCreateTransactionException.class)
    @ResponseBody
    public String e2(Exception e){
        String message = e.getMessage();
        log.error("数据库连接异常:{}", message);
        return "<h1>连接认证失败，请访问认证<a href='/666'>点击认证</a>，注意认证成功后，稍等一会即可正常访问</h1>";
    }
    @ExceptionHandler(value = org.apache.catalina.connector.ClientAbortException.class)
    @ResponseBody
    public void e3(Exception e){
        String message = e.getMessage();
        log.error("客户端连接异常:{}", message);
//        return StatusCode.Http403;
    }
//    @ExceptionHandler(value = java.lang.ArithmeticException.class)
//    @ResponseBody
    public String e4(Exception e){
        String message = e.getMessage();
        log.error("测试异常异常:{}", message);
        return message;
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        String message = errorAttributes.get("message").toString();
        errorAttributes.put("timestamp", TimeUtil.toTime(new Date(),"yyyy-MM-dd'T'HH:mm:ssXXX"));
        errorAttributes.put("index",contextPath);
        if (StrUtil.isBlank(message)||message.equals("No message available")) {
            Object status = errorAttributes.get("status");
            String msg="";
            if (status.equals(404)) {
                msg= StatusCode.Http404;
            }else if (status.equals(500)){
                msg=StatusCode.Http500;
            }else if (status.equals(400)){
                msg=StatusCode.Http400;
            }else if (status.equals(403)){
                msg=StatusCode.Http403;
            }else if (status.equals(503)){
                msg=StatusCode.Http503;
            }else if (status.equals(401)){
                msg=StatusCode.Http401;
            }
            errorAttributes.put("message", msg);
        }else {
            errorAttributes.put("message", message);
        }
        log.error("\n错误异常请求:"+errorAttributes.toString());
        return errorAttributes;
    }

    @Override
    public Throwable getError(WebRequest webRequest) {
        return super.getError(webRequest);
    }
}
