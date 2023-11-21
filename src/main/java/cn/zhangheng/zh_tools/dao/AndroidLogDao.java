package cn.zhangheng.zh_tools.dao;


import cn.zhangheng.zh_tools.bean.AndroidLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Repository
public interface AndroidLogDao extends JpaRepository<AndroidLog, String> {

    /**
     * 根据时间模糊查询
     * @param time 时间
     * @return
     */
    @Query("select a from AndroidLog a where a.time like %?1%")
    List<AndroidLog> findAllByTimeLike(String time);

    @Query("select a from AndroidLog a where a.time like %?1% and a.operation_name= ?2")
    List<AndroidLog> findAllByTimeLikeAndOperation_name(String time,String operation_name);

    @Query("select a from AndroidLog a where a.time like %?1% and a.operation_name= ?2 and a.phone_id= ?3")
    List<AndroidLog> findAllByTimeLikeAndOperation_nameAnAndPhone_id(String time,String operation_name,String phone_id);

    /**
     * 查询自定时间到现在的的每日访问数据统计
     * @param before 几天前
     * @return
     */
    @Query(value = "SELECT " +
            "DATE_FORMAT( DATA.DAY, '%m-%d' ) AS day," +
            "IFNULL( DATA.count, 0) AS num," +
            "day_list.DAY AS date " +
            "FROM" +
            "( SELECT DATE_FORMAT( time, '%Y-%m-%d' ) DAY, count( id ) count FROM android_log GROUP BY DAY )" +
            "DATA RIGHT JOIN (" +
            "SELECT" +
            "@date \\:= DATE_ADD( @date, INTERVAL - 1 DAY ) DAY " +
            "FROM" +
            "( SELECT @date \\:= DATE_ADD( DATE_FORMAT( SYSDATE(), '%Y-%m-%d' ), INTERVAL 1 DAY ) FROM android_log ) days " +
            "LIMIT ?1 " +
            ") day_list ON day_list.DAY = DATA.DAY " +
            "ORDER BY date ASC",
            nativeQuery=true)
    List<Map<String,Object>> findBeforeByDay(int before);

}
