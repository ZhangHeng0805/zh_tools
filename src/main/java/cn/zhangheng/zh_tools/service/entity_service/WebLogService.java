package cn.zhangheng.zh_tools.service.entity_service;

import cn.zhangheng.zh_tools.bean.WebLog;
import cn.zhangheng.zh_tools.dao.WebLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-12-08 17:43
 */
@Service
public class WebLogService {

    @Autowired
    private WebLogDao webLogDao;


    public WebLog saveAndFlush(WebLog webLog){
        return webLogDao.saveAndFlush(webLog);
    }
}
