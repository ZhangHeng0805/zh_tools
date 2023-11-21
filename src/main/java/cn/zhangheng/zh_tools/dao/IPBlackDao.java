package cn.zhangheng.zh_tools.dao;


import cn.zhangheng.zh_tools.bean.IPBlack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface IPBlackDao extends JpaRepository<IPBlack, String> {
    List<IPBlack> findAllByFlag(String flag);

    boolean existsByIpAndFlag(String ip,String flag);
}
