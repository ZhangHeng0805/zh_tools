package cn.zhangheng.zh_tools.controller.web;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import com.zhangheng.bean.Message;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.FormatUtil;
import com.zhangheng.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-11-26 11:22
 */
@Controller
public class StaticController {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private SettingConfig setting;

    @RequestMapping("/favicon.ico")
    public String favicon(){
        return "forward:/static/favicon.ico";
    }
    @ResponseBody
    @RequestMapping("/static/client")
    public void client(@RequestBody String map, HttpServletRequest request){
        String ip = CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders());
        JSONObject jb = JSONUtil.parseObj(map);
        StringBuilder sb = new StringBuilder()
                .append("时间:"+ TimeUtil.toTime(new Date(jb.getLong("r"))))
                .append("\tip:"+ip)
                .append("\t系统:"+jb.getStr("os")+jb.getStr("osv"))
                .append("\t浏览器:"+jb.getStr("bs")+"-V"+jb.getStr("bsv")+"("+jb.getStr("ul")+")["+jb.getStr("br")+"]")
                ;
        String app = jb.getStr("app");
        if (!StrUtil.isEmptyIfStr(app)){
            sb.append("\t应用:"+app);
        }
        log.info("\nWeb端信息:{{}}",sb.toString());
    }
    /**
     * 获取数学验证码
     */
    @RequestMapping("/getVerify/math")
    public void getMathVerify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = null;
        response.setCharacterEncoding("UTF-8");
        try {
            CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 100, 6, 100);
            captcha.setGenerator(new MathGenerator());
            captcha.createCode();
            String code = captcha.getCode();
            HttpSession session = request.getSession();
            session.setAttribute("verify-code", ScriptUtil.eval(code.replace("=", "")));
            outputStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "filename=[星曦向荣]验证码" + new Date().getTime() + ".png");
            captcha.write(outputStream);
        } catch (Exception e) {
            response.sendError(500, "验证码生成错误:" + e.getMessage());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    @RequestMapping("/getVerify/math/checking")
    @ResponseBody
    public Message verifyMathCheck(@RequestParam("code") String code, @RequestParam("isClear") Boolean isClear, HttpServletRequest request) {
        Message msg = new Message();
        msg.setTime(TimeUtil.getNowTime());
        HttpSession session = request.getSession();
        Integer vCode = Convert.toInt(session.getAttribute("verify-code"));
//        System.out.println(vCode);
//        System.out.println(code);
        if (vCode != null && !StrUtil.isBlank(code)) {
            if (FormatUtil.isNumber(code)) {
                if (vCode.equals(Convert.toInt(code))) {
                    msg.setCode(200);
                    msg.setTitle("验证码正确");
                    msg.setMessage("恭喜，验证成功！");
                    if (isClear)
                        session.setAttribute("verify-code", null);
                } else {
                    msg.setCode(500);
                    msg.setTitle("验证码错误");
                    msg.setMessage("对不起，验证码输入错误！");
                }
            } else {
                msg.setCode(500);
                msg.setTitle("验证码格式错误");
                msg.setMessage("请输入验证码中的计算结果！");
            }
        } else {
            msg.setCode(404);
            msg.setTitle("验证码为空");
            msg.setMessage("请重新获取验证码，然后再来验证！");
        }
        return msg;
    }
}
