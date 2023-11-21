package cn.zhangheng.zh_tools.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-03-02 16:08
 * @version: 1.0
 * @description: IP黑名单
 */
@Entity
@Table(name = "[black_ip]")
@Data
public class IPBlack {
    @Id
    @Column(name = "[ip]")
    private String ip;
    @Column(name = "[add_time]")
    private Date add_time;
    @Column(name = "[explain]")
    private String explain;
    @Column(name = "[flag]")
    private String flag;
}
