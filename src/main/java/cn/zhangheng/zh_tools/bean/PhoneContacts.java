package cn.zhangheng.zh_tools.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 手机联系人
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-11-20 13:55
 */
@Entity
@Table(name = "[phone_contacts]")
@Data
public class PhoneContacts {
    @Id
    @Column(name = "[id]",length = 32)
    private String id;//id
    @Column(name = "[name]",length = 255)
    private String name;//名称
    @Column(name = "[tel]",length = 255)
    private String tel;//号码
    @Column(name = "[phone_id]",length = 100)
    private String phone_id;//phone_info的主键
    @Column(name = "[add_time]",length = 255)
    private Date add_time;//添加时间
}
