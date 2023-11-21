package cn.zhangheng.zh_tools.dao;


import cn.zhangheng.zh_tools.bean.PhoneInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface PhoneInfoDao extends JpaRepository<PhoneInfo, String> {
}
