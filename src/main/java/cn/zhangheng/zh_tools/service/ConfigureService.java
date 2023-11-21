package cn.zhangheng.zh_tools.service;

import cn.zhangheng.zh_tools.bean.SettingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-17 00:00
 */
@Service
public class ConfigureService {

    @Autowired
    private SettingConfig setting;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private FileService fileService;

    public List<String> getUpdate(String updateName) throws Exception {
        List<String> fliesByPath = fileService.getFliesByPath(setting.getBaseDir() + "update/" + updateName);
        for (int i = 0; i < fliesByPath.size(); i++) {
            String s = fliesByPath.get(i);
            if (s.indexOf("update") > 1) {
                s = s.substring(s.indexOf("update"));
            }
            fliesByPath.set(i,s);
        }
        return fliesByPath;
    }

}
