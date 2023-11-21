package cn.zhangheng.zh_tools.bean;

import cn.hutool.core.util.ArrayUtil;
import com.zhangheng.util.EncryptUtil;
import com.zhangheng.util.TimeUtil;

import java.util.Date;


/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-03-08 16:35
 * @version: 1.0
 * @description:
 */

public class AppLife {
    private Long createTime;
    private Integer maxDay;
    private Integer[] index;
    private String appId;

    private String sign;

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getMaxDay() {
        return maxDay;
    }

    public void setMaxDay(Integer maxDay) {
        this.maxDay = maxDay;
    }

    public Integer[] getIndex() {
        return index;
    }

    public void setIndex(Integer[] index) {
        this.index = index;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String createSign() throws Exception {
        String token=createTime
                +maxDay
                + ArrayUtil.toString(index)
                +appId
                ;
        return EncryptUtil.getSignature(token, TimeUtil.toTime(new Date(createTime), TimeUtil.EnDateFormat_Detailed));
    }

}
