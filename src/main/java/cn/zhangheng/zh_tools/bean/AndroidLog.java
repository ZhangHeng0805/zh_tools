package cn.zhangheng.zh_tools.bean;

import lombok.Data;

import javax.persistence.*;

/**
 * 安卓客户端日志
 *
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-12-08 17:10
 */
@Entity
@Table(name = "[android_log]")
@Data
public class AndroidLog {
    @Id
    @GeneratedValue
    @Column(name = "[id]")
    private Integer id;
    @Column(name = "[ip]",length = 255)
    private String ip;
    @Column(name = "[phone_id]",length = 255)
    private String phone_id;
    @Column(name = "[operation_name]",length = 255)
    private String operation_name;
    @Column(name = "[operation_content]",length = 255)
    private String operation_content;
    @Column(name = "[time]",length = 255)
    private String time;


}
