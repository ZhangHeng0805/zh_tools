package cn.zhangheng.zh_tools.service.entity_service;

import cn.zhangheng.zh_tools.bean.PhoneContacts;
import cn.zhangheng.zh_tools.dao.PhoneContactsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-11-20 13:59
 */
@Service
public class PhoneContactsService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PhoneContactsDao phoneContactsDao;

    /**
     * 根据设备id查询
     * @param id
     * @return
     */
    public List<PhoneContacts> getAllByPhoneId(String id){
        return phoneContactsDao.getAllByPhoneId(id);
    }
    public List<PhoneContacts> saveALL(Iterable<PhoneContacts> list){
        return phoneContactsDao.saveAll(list);
    }

}
