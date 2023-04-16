package com.wsj.xunyou.utils;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

public class FileUpload {
    //上传操作
    public static String uploadImage(MultipartFile file, String filePath){
        if (file == null) {
            return "请选择要上传的图片";
        }
        if (file.getSize() > 1024 * 1024 * 10) {
            return "文件大小不能大于10M";
        }
        //获取文件后缀
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename()
                .lastIndexOf(".") + 1, file.getOriginalFilename().length());
        if (!"jpg,jpeg,gif,png".toUpperCase().contains(suffix.toUpperCase())) {
            return "请选择jpg,jpeg,gif,png格式的图片";
        }

        File savePathFile = new File(filePath);
        if (!savePathFile.exists()) {
            //若不存在该目录，则创建目录
            savePathFile.mkdir();
        }
        //通过UUID生成唯一文件名
        String filename = UUID.randomUUID().toString().replaceAll("-","") + "." + suffix;
        try {
            //将文件保存指定目录
            //file.transferTo(new File(savePath + filename));
            //File file1 = new File(file.getOriginalFilename());
            FileUtils.copyInputStreamToFile(file.getInputStream(),new File(filePath + filename));
        } catch (Exception e) {
            e.printStackTrace();
            return "保存文件异常";
        }
        //返回文件名称
        return filePath + "/"+ filename;
    }

}
