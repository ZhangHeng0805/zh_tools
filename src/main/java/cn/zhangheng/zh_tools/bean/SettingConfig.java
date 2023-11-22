package cn.zhangheng.zh_tools.bean;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-19 16:33
 */
@Configuration
@PropertySource(value = {"file:setting.properties"},encoding = "UTF-8")
@ConfigurationProperties(prefix = "setting")
@Validated
@Data
public class SettingConfig {
    @NotNull
    private String application_name;
    @NotNull
    private String baseDir;
    @Min(1)
    @NotNull
    private Integer maxRequestCounts;
    @Min(100)
    @NotNull
    private Integer RequestInterval;
    @NotNull
    private List<Integer> accessPrintCode;
    @NotNull
    private Boolean isPrintAllRequestInfo;
    @NotNull
    private Boolean isUseDownLoadUrl;
    @URL
    @NotNull
    private String appDownLoadUrl;
    @NotNull
    private String dataHost;
    private String dataPwd;
    @NotNull
    @URL
    private String mainUrl;
    @NotNull
    @Value(value = "#{'${setting.admin-email}'.split(',')}")
    private String[] adminEmail;
    private String app_introduce;
    @NotNull
    @URL
    private String weixin_url;
    @NotNull
    private String index_img_config_file;
    @NotNull
    private String index_html;
    @NotNull
    private String version;
    @NotNull
    private Boolean IP_change_verify;
    @NotNull
    @Value(value = "#{'${setting.ip-headers-name}'.split(',')}")
    private String[] ipHeaders;

}
