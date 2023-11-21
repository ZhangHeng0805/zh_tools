package cn.zhangheng.zh_tools.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 手机信息类
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-02-23 15:10
 */
@Entity
@Table(name = "[phone_info]")
@Data
public class PhoneInfo {


    @Id
    @Column(name = "[id]",length = 100)
    private String id;//设备ID
    @Column(name = "[ip]",length = 255)
    private String ip;//设备IP地址
    @Column(name = "[model]",length = 255)
    private String model;//设备型号
    @Column(name = "[net_address]",length = 255)
    private String net_address;//sdk版本
    @Column(name = "[release]",length = 255)
    private String release;//android版本
    @Column(name = "[app_version]",length = 255)
    private String app_version;//app型号
    @Column(name = "[tel]",length = 15)
    private String tel;//手机号
    @Column(name = "[tel_type]",length = 20)
    private String tel_type;//手机运营商
    @Column(name = "[notice]",length = 255)
    private String notice;//提示信息
    @Column(name = "[last_time]",length = 30)
    private String last_time;//最近一次访问时间
    @Column(name = "[start_time]",length = 30)
    private String start_time;//第一次访问时间


}
