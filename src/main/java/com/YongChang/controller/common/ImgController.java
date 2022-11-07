package com.YongChang.controller.common;

import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.YongChang.config.Result;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


@Controller
@RequestMapping("file")
public class ImgController {
    //secretId
    public static final String secretId = "";
    //secretKey
    public static final String secretKey = "";
    //COS桶的根路径
    public static final String BASEURL = "";
    //桶的名称
    public static final String BUCKETNAME = "";
    //桶的key(文件夹名称)
    public static final String KEY = "";
    //你的REGION
    public static final String REGION = "";
    private static File transferToFile(MultipartFile multipartFile) {
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            System.out.println(originalFilename);
            String[] filename = originalFilename.split("\\.");
            file = File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    @PostMapping("/upload")
    @ResponseBody
    public Result uplaod(@RequestParam("file") MultipartFile file) throws Exception {
        //用来检测程序运行时间
        String fileName = IdWorker.get32UUID() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        try {
            File file1 = transferToFile(file);
            // 1 初始化用户身份信息（secretId, secretKey）。
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            // 2 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
// clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
            Region region = new Region(REGION);
            ClientConfig clientConfig = new ClientConfig(region);
// 这里建议设置使用 https 协议
            clientConfig.setHttpProtocol(HttpProtocol.https);
            // 3 生成 cos 客户端。
            COSClient cosClient = new COSClient(cred, clientConfig);
            // 指定要上传的文件
            // 指定文件将要存放的存储桶
            String bucketName = BUCKETNAME;
            // 指定文件上传到 COS 上的路径，即对象键。例如对象键为folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
            String key = KEY + fileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file1);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传文件失败");
        }
        return Result.success(BASEURL + fileName, "文件上传成功");
    }


}
