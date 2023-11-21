package cn.zhangheng.zh_tools.dao;


import cn.zhangheng.zh_tools.bean.PhoneContacts;
import cn.zhangheng.zh_tools.bean.PhoneInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface PhoneContactsDao extends JpaRepository<PhoneContacts, String> {
    @Query("select p from PhoneContacts p where p.phone_id=?1")
    List<PhoneContacts> getAllByPhoneId(String id);
}
