package cn.zhangheng.zh_tools.bean;

import lombok.Data;

import javax.persistence.*;

/**
 * web日志
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-12-08 16:52
 */
@Entity
@Table(name = "[web_log]")
@Data
public class WebLog {
    @Id
    @GeneratedValue
    @Column(name = "[id]")
    private Integer id;
    @Column(name = "[session_id]",length = 255)
    private String session_id;
    @Column(name = "[ip]",length = 255)
    private String ip;
    @Column(name = "[time]",length = 255)
    private String time;
    @Column(name = "[count]")
    private Integer count;
    @Column(name = "[location]")
    private String location;
    @Column(name = "[user_agent]")
    private String user_agent;
}
