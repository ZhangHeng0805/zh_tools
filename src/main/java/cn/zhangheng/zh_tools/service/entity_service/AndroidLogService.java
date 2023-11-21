package cn.zhangheng.zh_tools.service.entity_service;

import cn.zhangheng.zh_tools.bean.AndroidLog;
import cn.zhangheng.zh_tools.bean.PhoneInfo;
import cn.zhangheng.zh_tools.dao.AndroidLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-12-08 17:43
 */
@Service
public class AndroidLogService {

    @Autowired
    private AndroidLogDao androidLogDao;


    public AndroidLog saveAndFlush(AndroidLog androidLog){
        return androidLogDao.saveAndFlush(androidLog);
    }

    public List<AndroidLog> findAllByTimeLike(String time){
        return androidLogDao.findAllByTimeLike(time);
    }
    public List<AndroidLog> findAllByTimeLikeAndOperation_name(String time,String operation_name){
        return androidLogDao.findAllByTimeLikeAndOperation_name(time,operation_name);
    }

    public void saveLog(String title, String content, HttpServletRequest request){
        AndroidLog androidLog = new AndroidLog();
        androidLog.setOperation_name(title);
        androidLog.setOperation_content(content);
        PhoneInfo phoneInfo = (PhoneInfo) request.getSession().getAttribute("phone_info");
        String ip = phoneInfo.getIp();
        androidLog.setIp(ip);
        androidLog.setPhone_id(phoneInfo.getId());
        androidLog.setTime(phoneInfo.getLast_time());
        saveAndFlush(androidLog);
    }
    public List<Map<String,Object>> findBeforeByDay(Integer before){
        return androidLogDao.findBeforeByDay(before);
    };
    public Long count(){
        return androidLogDao.count();
    }
}
