package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


/**
 * Created by aa on 2017/6/22.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String uploadFile(File file,String path)
    {
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxx");
        String fileName =file.getName();
        System.out.println("fileName:"+fileName);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxx");
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID()+"."+fileExtensionName;
        logger.info("开始上传文件，文件名{}新文件名{}",fileName,uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists())
        {
            fileDir.setWritable(true);
            fileDir.mkdirs();// mkdirs , mkdir的区别
        }
        File targerFile = new File(path,uploadFileName);
        try {
            fileCopy(file,targerFile);
            //将文件上传至FTP服务器
            boolean result =  FTPUtil.uploadFile(Lists.<File>newArrayList(targerFile));
            //将文件删除掉
            if(result)
            {
                //targerFile.delete();
                System.out.println("upload success:"+targerFile.getName());
            }else{
                System.out.println("upload faild :"+targerFile.getName());
            }
        } catch (Exception e) {
            logger.error("文件上传异常",e);
            return null;
        }
        return targerFile.getName();
    }

    private void fileCopy(File source,File dest)
    {
        FileInputStream input = null;
        FileOutputStream out = null;
        try{
            input = new FileInputStream(source);
            out = new FileOutputStream(dest);
            byte[] bytes = new byte[1024];
            int len = -1;
            while((len = input.read(bytes)) != -1)
            {
                out.write(bytes,0,len);
                out.flush();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(input != null)
            {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null)
            {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public String upload(MultipartFile file,String path)
    {
        String fileName =file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID()+"."+fileExtensionName;
        logger.info("开始上传文件，文件名{}, 路径{} , 新文件名{}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists())
        {
            fileDir.setWritable(true);
            fileDir.mkdirs();// mkdirs , mkdir的区别
        }
        File targerFile = new File(path,uploadFileName);
        try {
            file.transferTo(targerFile);
            //将文件上传至FTP服务器
           boolean result =  FTPUtil.uploadFile(Lists.<File>newArrayList(targerFile));
            //将文件删除掉
            if(result)
            {
               // targerFile.delete();
            }

        } catch (IOException e) {
            logger.error("文件上传异常",e);
            return null;
        }
        return targerFile.getName();
    }
}
