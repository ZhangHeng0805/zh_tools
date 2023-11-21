package cn.zhangheng.zh_tools.controller;

import cn.hutool.core.util.StrUtil;
import cn.zhangheng.zh_tools.bean.SettingConfig;
import cn.zhangheng.zh_tools.bean.StatusCode;
import cn.zhangheng.zh_tools.bean.Visitor;
import cn.zhangheng.zh_tools.service.ConfigureService;
import cn.zhangheng.zh_tools.service.entity_service.VisitorService;
import com.zhangheng.file.FiletypeUtil;
import com.zhangheng.util.CusAccessObjectUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-17 12:35
 */

@Controller
public class FilesController {

    @Autowired
    private SettingConfig setting;
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private ConfigureService configureService;

    /**
     * 文件下载请求
     *
     * @param moduleBaseName
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/fileload/download/{moduleBaseName}/**")
    public void download(@PathVariable("moduleBaseName") String moduleBaseName,
                         HttpServletRequest request,
                         HttpServletResponse response) throws UnsupportedEncodingException {
        String err = null;
        FileInputStream input = null;
        File file = null;
        ServletOutputStream outputStream = null;
        try {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

//        log.info("moduleBaseName:{}",moduleBaseName);

            String ipMessage;
            Optional<Visitor> visitorByIp = visitorService.getVisitorByIp(CusAccessObjectUtil.getClientIp(request,setting.getIpHeaders()));
            if (visitorByIp.isPresent()) {
                Visitor visitor = visitorByIp.get();
                ipMessage = "[" + visitor.getIp() + "] " + visitor.getLocation();
            } else {
                ipMessage = CusAccessObjectUtil.getRequst(request);
            }
//        String ipMessage = IPAnalysisAPI.getIPMessage(request);
            log.info("\n下载请求：{}，[{}]",ipMessage,CusAccessObjectUtil.getUri(request));
//        log.info("下载IP："+ipAddress);
            //请求的完整路径（地址）
            final String pathq =
                    request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
//        log.info("pathq:{}",pathq);
            final String bestMatchingPattern =
                    request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
//        log.info("bestMatchingPattern:{}",bestMatchingPattern);
            String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, pathq);
//        log.info("arguments:{}",arguments);
            String moduleName;
            if (null != arguments && !arguments.isEmpty()) {
                moduleName = moduleBaseName + '/' + arguments;
            } else {
                moduleName = moduleBaseName;
            }
//        log.info(moduleName);
            String type = "";
            String name = "";
            if (moduleName.lastIndexOf("/") > 0) {
                type = moduleName.substring(0, moduleName.lastIndexOf("/"));
                name = moduleName.substring(moduleName.lastIndexOf("/") + 1);
            }
            file = new File(setting.getBaseDir() + type + "/" + name);
            if (moduleBaseName.equals("update")) {
                file = getNewUpdate(file);
            }
//            FileInputStream input = null;
            outputStream = response.getOutputStream();

            response.setHeader("Content-Type", FiletypeUtil.getFileContentType(file.getName()));
            //显示文件大小
            response.setHeader("Content-Length", String.valueOf(file.length()));
            //设置文件下载方式为附件方式，以及设置文件名
            response.setHeader("Content-Disposition", "attchment;filename=" + file.getName());
            input = FileUtils.openInputStream(file);
            IOUtils.copy(input, outputStream);
//            log.info("下载请求成功:"+file.getPath());
        } catch (Exception e) {
            if (e.toString().indexOf("找不到") > 1) {
                err = "对不起o(╥﹏╥)o，没有找到你需要的文件";
            } else {
                err = "错误o(╥﹏╥)o，下载出错误了";
            }
            log.error("下载错误1：" + e.getMessage());
        } finally {
            try {
                if (file.exists()) {
                    input.close();
                }
            } catch (Exception e) {
                log.error("下载错误2：" +e.getMessage());
                err = "错误o(╥﹏╥)o，下载出错误了";
//                response.sendError(404, e.getMessage());
            }
            if (err != null) {
                try {
                    if (err.startsWith("对不起")) {
                        response.sendError(404, StatusCode.Http404);
                    }else{
                        response.sendError(500, StatusCode.Http500);
                    }
                } catch (Exception e) {
//                    log.error(e.toString());
                }
            }
            try {
                if (outputStream!=null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("下载错误3：" +e.toString());
            }
        }
    }

    private File getNewUpdate(File file) throws Exception {
        String[] path = StrUtil.splitToArray(file.getPath().replace("\\", "/"), "/");
        if (path[1].indexOf("update") > -1) {
            if (!file.exists()) {
                List<String> update = configureService.getUpdate(path[2]);
                String s = update.get(update.size() - 1);
                return new File("files/" + s);
            }
        }
        return file;
    }

    /**
     * 分片断点下载
     * @param moduleBaseName
     * @param response
     * @param request
     * @param range
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/download/split/{moduleBaseName}/**")
    public void downloadFile(@PathVariable("moduleBaseName") String moduleBaseName,
                             HttpServletResponse response,
                             HttpServletRequest request,
                             @RequestHeader(name = "Range", required = false) String range) throws UnsupportedEncodingException {

        File file = pares(request,response,moduleBaseName);
        String filename = file.getName();
        long length = file.length();
        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<>();
        //处理Range
        try {

            if (!file.exists()) {
                String msg = "需要下载的文件不存在：" + file.getAbsolutePath();
                log.error("下载spilt错误1："+msg);
                throw new RuntimeException(msg);
            }

            if (file.isDirectory()) {
                String msg = "需要下载的文件的路径对应的是一个文件夹：" + file.getAbsolutePath();
                log.error("下载spilt错误2："+msg);
                throw new RuntimeException(msg);
            }
            dealRanges(full, range, ranges, response, length);
        }catch (IOException e){
            log.error("下载spilt错误3："+e.getMessage());
            throw new RuntimeException("文件分片下载异常：" + e.getMessage());
        }
        // 如果浏览器支持内容类型，则设置为“内联”，否则将弹出“另存为”对话框. attachment inline
        String disposition = "attachment";

        // 将需要下载的文件段发送到客服端，准备流.
        try (RandomAccessFile input = new RandomAccessFile(file, "r");
             ServletOutputStream output = response.getOutputStream()) {
            //最后修改时间
            FileTime lastModifiedObj = Files.getLastModifiedTime(file.toPath());
            long lastModified = LocalDateTime.ofInstant(lastModifiedObj.toInstant(),
                    ZoneId.of(ZoneId.systemDefault().getId())).toEpochSecond(ZoneOffset.UTC);
            //初始化response.
            response.reset();
            response.setBufferSize(20480);
            response.setHeader("Content-type", "application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", disposition + ";filename=" +
                    URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("ETag", URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
            response.setDateHeader("Last-Modified", lastModified);
            response.setDateHeader("Expires", System.currentTimeMillis() + 604800000L);
            //输出Range到response
            outputRange(response, ranges, input, output, full, length);
            output.flush();
            response.flushBuffer();
        }catch (Exception e){
//            e.printStackTrace();
            log.error("文件分片下载异常：" + e.getMessage());
        }
    }

    private File pares(HttpServletRequest request,HttpServletResponse response,String moduleBaseName) throws UnsupportedEncodingException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//        log.info("moduleBaseName:{}",moduleBaseName);
        String ipMessage;
        ipMessage = CusAccessObjectUtil.getCompleteRequest(request);
//        String ipMessage = IPAnalysisAPI.getIPMessage(request);
        log.info("\n文件分片下载请求：" + ipMessage+"\n");
//        log.info("下载IP："+ipAddress);
        //请求的完整路径（地址）
        final String pathq =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
//        log.info("pathq:{}",pathq);
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
//        log.info("bestMatchingPattern:{}",bestMatchingPattern);
        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, pathq);
//        log.info("arguments:{}",arguments);
        String moduleName;
        if (null != arguments && !arguments.isEmpty()) {
            moduleName = moduleBaseName + '/' + arguments;
        } else {
            moduleName = moduleBaseName;
        }
//        log.info(moduleName);
        String type = "";
        String name = "";
        if (moduleName.lastIndexOf("/") > 0) {
            type = moduleName.substring(0, moduleName.lastIndexOf("/"));
            name = moduleName.substring(moduleName.lastIndexOf("/") + 1);
        }
        return new File(setting.getBaseDir() + type + "/" + name);
    }

    /**
     * 处理请求中的Range(多个range或者一个range，每个range范围)
     * @author kevin
     * @param range :
     * @param ranges :
     * @param response :
     * @param length :
     */
    private void dealRanges(Range full, String range, List<Range> ranges, HttpServletResponse response,
                            long length) throws IOException {
        if (range != null) {
            // Range 头的格式必须为 "bytes=n-n,n-n,n-n...". 如果不是此格式, 返回 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader("Content-Range", "bytes */" + length);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // 处理传入的range的每一段.
            for (String part : range.substring(6).split(",")) {
                part = part.split("/")[0];
                // 对于长度为100的文件，以下示例返回:
                // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                int delimiterIndex = part.indexOf("-");
                long start = Range.sublong(part, 0, delimiterIndex);
                long end = Range.sublong(part, delimiterIndex + 1, part.length());

                //如果未设置起始点，则计算的是最后的 end 个字节；设置起始点为 length-end，结束点为length-1
                //如果未设置结束点，或者结束点设置的比总长度大，则设置结束点为length-1
                if (start == -1) {
                    start = length - end;
                    end = length - 1;
                } else if (end == -1 || end > length - 1) {
                    end = length - 1;
                }

                // 检查Range范围是否有效。如果无效，则返回416.
                if (start > end) {
                    response.setHeader("Content-Range", "bytes */" + length);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }
                // 添加Range范围.
                ranges.add(new Range(start, end, end - start + 1));
            }
        }else{
            //如果未传入Range，默认下载整个文件
            ranges.add(full);
        }
    }



    /**
     * output写流输出到response
     * @author kevin
     * @param response :
     * @param ranges :
     * @param input :
     * @param output :
     * @param full :
     * @param length :
     */
    private void outputRange(HttpServletResponse response, List<Range> ranges, RandomAccessFile input,
                             ServletOutputStream output, Range full, long length) throws IOException {
        if (ranges.isEmpty() || ranges.get(0) == full) {
            // 返回整个文件.
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Range", "bytes " + full.start + "-" + full.end + "/" + full.total);
            response.setHeader("Content-length", String.valueOf(full.length));
            response.setStatus(HttpServletResponse.SC_OK); // 200.
            Range.copy(input, output, length, full.start, full.length);
        } else if (ranges.size() == 1) {
            // 返回文件的一个分段.
            Range r = ranges.get(0);
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
            response.setHeader("Content-length", String.valueOf(r.length));
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
            // 复制单个文件分段.
            Range.copy(input, output, length, r.start, r.length);
        } else {
            // 返回文件的多个分段.
            response.setContentType("multipart/byteranges; boundary=MULTIPART_BYTERANGES");
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

            // 复制多个文件分段.
            for (Range r : ranges) {
                //为每个Range添加MULTIPART边界和标题字段
                output.println();
                output.println("--MULTIPART_BYTERANGES");
                output.println("Content-Type: application/octet-stream;charset=UTF-8");
                output.println("Content-length: " + r.length);
                output.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);
                // 复制多个需要复制的文件分段当中的一个分段.
                Range.copy(input, output, length, r.start, r.length);
            }

            // 以MULTIPART文件的边界结束.
            output.println();
            output.println("--MULTIPART_BYTERANGES--");
        }
    }
    private static class Range{
        long start;
        long end;
        long length;
        long total;

        /**
         * Range段构造方法.
         *
         * @param start range起始位置.
         * @param end   range结束位置.
         * @param total range段的长度.
         */
        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

        public static long sublong(String value, int beginIndex, int endIndex) {
            String substring = value.substring(beginIndex, endIndex);
            return (substring.length() > 0) ? Long.parseLong(substring) : -1;
        }

        private static void copy(RandomAccessFile randomAccessFile, OutputStream output, long fileSize, long start, long length) throws IOException {
            byte[] buffer = new byte[4096];
            int read = 0;
            long transmitted = 0;
            if (fileSize == length) {
                randomAccessFile.seek(start);
                //需要下载的文件长度与文件长度相同，下载整个文件.
                while ((transmitted + read) <= length && (read = randomAccessFile.read(buffer)) != -1){
                    output.write(buffer, 0, read);
                    transmitted += read;
                }
                //处理最后不足buff大小的部分
                if(transmitted < length){
//                    log.info("最后不足buff大小的部分大小为：" + (length - transmitted));
                    read = randomAccessFile.read(buffer, 0, (int)(length - transmitted));
                    output.write(buffer, 0, read);
                }
            } else {
                randomAccessFile.seek(start);
                long toRead = length;

                //如果需要读取的片段，比单次读取的4096小，则使用读取片段大小读取
                if(toRead < buffer.length){
                    output.write(buffer, 0, randomAccessFile.read(new byte[(int) toRead]));
                    return;
                }
                while ((read = randomAccessFile.read(buffer)) > 0) {
                    toRead -= read;
                    if (toRead > 0) {
                        output.write(buffer, 0, read);
                    } else {
                        output.write(buffer, 0, (int) toRead + read);
                        break;
                    }
                }

            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Range range = (Range) o;
            return start == range.start &&
                    end == range.end &&
                    length == range.length &&
                    total == range.total;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end, length, total);
        }
    }

}

