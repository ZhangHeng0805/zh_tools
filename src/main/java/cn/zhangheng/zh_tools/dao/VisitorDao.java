package cn.zhangheng.zh_tools.dao;

import cn.zhangheng.zh_tools.bean.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface VisitorDao extends JpaRepository<Visitor, String> {

    @Query("select v from Visitor v where v.count>= ?1 or v.state = ?2")
    List<Visitor> findAllByCountsOrState(Integer counts,Integer state);

    @Query("select v from Visitor v where v.last_time like %?1% order by v.last_time asc")
    List<Visitor> findAllByLast_timeLike(String time);

}
