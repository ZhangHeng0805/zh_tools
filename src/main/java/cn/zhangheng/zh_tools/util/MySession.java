package cn.zhangheng.zh_tools.util;

import cn.hutool.core.lang.UUID;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import com.zhangheng.util.CusAccessObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-07-10 11:56
 * @version: 1.0
 * @description:
 */
@Component
@Data
public class MySession {
    private List<Visit> visitors = new ArrayList<>();

   @Autowired
   private SettingConfig setting;
    @Data
    public class Visit {
        private String id;
        private String ip;
        private String ug;
        private Date date;
        private Map<String, Object> attr;
    }

    public Visit put(HttpServletRequest request) {
        Visit visit = generateVisit(CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders()), CusAccessObjectUtil.getUser_Agent(request));
        return this.put(visit);
    }
    public Visit put(Visit visit) {
        if (exist(visit)) {
        } else {
            visitors.add(visit);
        }
        return visit;
    }

    public Visit generateVisit(String ip, String ug) {
        Visit visitor = new Visit();
        visitor.id= UUID.randomUUID().toString();
        visitor.ip = ip;
        visitor.ug = ug;
        visitor.date=new Date();
        visitor.attr=new HashMap<>();
        return visitor;
    }

    public boolean remove(Visit visitor) {
        visitor = find(visitor);
        if (visitor != null)
            return visitors.remove(visitor);
        else
            return false;
    }

    public void clear() {
        visitors.clear();
    }

    public boolean exist(Visit visitor) {
        return find(visitor) != null;
    }

    public Visit find(String ip,String ug) {
        for (Visit v : visitors) {
            if (v.ip.equals(ip)
                    && v.ug.equalsIgnoreCase(ug)) {
                return v;
            }
        }
        return null;
    }

    public Visit find(Visit visitor) {
        return find(visitor.ip,visitor.ug);
    }

    private void setVisitors(List<Visit> visitors) {
        this.visitors = visitors;
    }
}
