package cn.zhangheng.zh_tools.controller.web;

import cn.hutool.core.util.URLUtil;
import cn.zhangheng.zh_tools.bean.StatusCode;
import com.zhangheng.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-04-12 18:02
 * @version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/proxy")
public class ProxyController {
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 代理所有请求
     *
     * @param url      要请求的目标地址
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/request")
    public void proxy(@RequestParam("url") String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (!FormatUtil.isUrl(url)) {
                request.getRequestDispatcher(url).forward(request, response);
                return;
            }
            URI newUri = URLUtil.toURI(url, true);
            log.info("\n代理请求url：{}", newUri.toString());
            // 执行代理查询
            String methodName = request.getMethod();
            HttpMethod httpMethod = HttpMethod.resolve(methodName);
            if (httpMethod == null) {
                return;
            }
            ClientHttpRequest delegate = new SimpleClientHttpRequestFactory().createRequest(newUri, httpMethod);
            Enumeration<String> headerNames = request.getHeaderNames();
            // 设置请求头
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> v = request.getHeaders(headerName);
                List<String> arr = new ArrayList<>();
                while (v.hasMoreElements()) {
                    arr.add(v.nextElement());
                }
                delegate.getHeaders().addAll(headerName, arr);
            }
            StreamUtils.copy(request.getInputStream(), delegate.getBody());
            // 执行远程调用
            ClientHttpResponse clientHttpResponse = delegate.execute();
            response.setStatus(clientHttpResponse.getStatusCode().value());
            // 设置响应头
            clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
                response.setHeader(key, it);
            }));
            StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
        } catch (Exception e) {
            log.error("\n代理请求错误：{}\n", e.toString());
            response.sendError(404, StatusCode.Http404);
        }
    }

}
