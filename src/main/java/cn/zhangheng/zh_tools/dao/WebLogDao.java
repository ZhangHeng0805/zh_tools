package cn.zhangheng.zh_tools.dao;


import cn.zhangheng.zh_tools.bean.WebLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Repository
public interface WebLogDao extends JpaRepository<WebLog, String> {
    @Query("select w from WebLog w where w.time like %?1% order by w.time asc")
    List<WebLog> findAllByTimeLike(String time);


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
            "( SELECT DATE_FORMAT( time, '%Y-%m-%d' ) DAY, count( id ) count FROM web_log GROUP BY DAY )" +
            "DATA RIGHT JOIN (" +
            "SELECT" +
            "@date \\:= DATE_ADD( @date, INTERVAL - 1 DAY ) DAY " +
            "FROM" +
            "( SELECT @date \\:= DATE_ADD( DATE_FORMAT( SYSDATE(), '%Y-%m-%d' ), INTERVAL 1 DAY ) FROM web_log ) days " +
            "LIMIT ?1 " +
            ") day_list ON day_list.DAY = DATA.DAY " +
            "ORDER BY date ASC",
            nativeQuery=true)
    List<Map<String,Object>> findBeforeByDay(int before);

    @Query("select sum(w.count) from WebLog w")
    Long sumByCount();
}
