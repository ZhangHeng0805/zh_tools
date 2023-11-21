package cn.zhangheng.zh_tools.service;

import com.zhangheng.file.FolderFileScannerUtil;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-18 22:23
 */
@Service
public class FileService {

    /**
     * 查找路径所有中文件
     * @param path
     * @return
     * @throws Exception
     */
    public List<String> getFliesByPath(String path) throws Exception {
        Set<String> set=new HashSet<>();
        List<String> list=new ArrayList<>();
        List<String> files=new ArrayList<>();
        try {
            files = FolderFileScannerUtil.scanFilesWithRecursion(path);
        }catch (Exception e){
            throw e;
        }
        if (!files.isEmpty()){
            for (Object file : files) {
                String s = String.valueOf(file);
                s=s.replace("\\","/");
                set.add(s);
            }
            if (!set.isEmpty()){
                list.addAll(set);
                Collections.sort(list);
            }
        }else {
            throw new RuntimeException("没有找到更新文件");
        }
        return list;
    }
}
