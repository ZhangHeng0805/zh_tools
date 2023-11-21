package cn.zhangheng.zh_tools.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-19 00:07
 */
@Entity
@Table(name = "[visitor]")
@Data
public class Visitor {
    @Id
    @Column(name = "[ip]",length = 100)
    private String ip;//ip
    @Column(name = "[location]",length = 255)
    private String location;//位置
    @Column(name = "[user_agent]",length = 1000)
    private String user_agent;//请求头
    @Column(name = "[last_time]",length = 20)
    private String last_time;//最近一次时间
    @Column(name = "[first_time]",length = 20)
    private String first_time;//第一次时间
    @Column(name = "[counts]",columnDefinition = "int",length = 9)
    private Integer count;//次数
    @Column(name = "[total]",columnDefinition = "int",length = 9)
    private Integer total;//总数
    @Column(name = "[state]",columnDefinition = "int",length = 2)
    private Integer state;//状态【0-正常 1-黑名单】
}
