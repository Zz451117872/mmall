package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Created by aa on 2017/6/22.
 */
public interface IFileService {
    public String upload(MultipartFile file, String path);
    public String uploadFile(File file,String path);
}
