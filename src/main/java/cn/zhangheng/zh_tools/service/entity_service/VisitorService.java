package cn.zhangheng.zh_tools.service.entity_service;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.dao.VisitorDao;
import cn.zhangheng.zh_tools.reptile.IPAnalysisAPI;
import com.zhangheng.util.CusAccessObjectUtil;
import com.zhangheng.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-19 00:14
 */
@Service
public class VisitorService {
    @Autowired
    private VisitorDao visitorDao;
    @Autowired
    private SettingConfig setting;

    /**
     * 通过request设置
     * Ip,Location,User_agent,Last_time
     * @param req
     * @return
     */
    public Visitor setRequest(HttpServletRequest req){
        Visitor visitor = new Visitor();
        String ip = CusAccessObjectUtil.getClientIp(req,setting.getIpHeaders());
        visitor.setIp(ip);
        visitor.setLocation(getIPLocation(ip));
        visitor.setUser_agent(CusAccessObjectUtil.getUser_Agent(req));
        visitor.setLast_time(TimeUtil.getNowTime());
        return visitor;
    }
    public String getIPLocation(String ip){
        String location = "无";
        if (!StrUtil.isBlank(ip)) {
            if (IPAddressUtil.isIPv6LiteralAddress(ip)) {
                location = "ipv6地址";
            } else {
                if (!NetUtil.isInnerIP(ip)) {
                    String ipInfo1 = IPAnalysisAPI.getIPInfo1_str(IPAnalysisAPI.getIPInfo1(ip));
                    location = ipInfo1.indexOf("]")>0?ipInfo1.substring(ipInfo1.indexOf("]") + 1):"";
                } else {
                    location = "内网地址";
                }
            }
        }
        return location;
    }

    public boolean saveAndflush(Visitor visitor) throws Exception{

        Visitor saveAndFlush = visitorDao.saveAndFlush(visitor);
        if(visitor.equals(saveAndFlush)){
            return true;
        }
        return false;
    }

    public Optional<Visitor> getVisitorByIp(String ip){
        return visitorDao.findById(ip);
    }
    public boolean existsByIp(String ip){
        return visitorDao.existsById(ip);
    }

    public List<Visitor> findAll(){
        return visitorDao.findAll();
    }

    public List<Visitor> findAllByCountsOrState(Integer counts,Integer state){
        return visitorDao.findAllByCountsOrState(counts,state);
    }
    public List<Visitor> findAllByLast_timeLike(String time){
        return visitorDao.findAllByLast_timeLike(time);
    }
    public Optional<Visitor> findById(String id){
        return visitorDao.findById(id);
    }
}
