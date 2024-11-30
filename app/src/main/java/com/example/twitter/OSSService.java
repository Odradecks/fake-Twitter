package com.example.twitter;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OSSService {
    private static final String TAG = "OSSService";

    private OSS ossClient;
    private String bucketName = "odradek"; // 替换为你的OSS存储桶名称
    private String endpoint = "https://oss-cn-hangzhou.aliyuncs.com"; // 替换为你的OSS区域Endpoint
    private String accessKeyId;
    private String accessKeySecret;

    /**
     * 加载 Access Key 和 Secret Key
     *
     * @param context 应用上下文
     */
    public void loadKeys(Context context) {
        try {
            Properties properties = new Properties();

            // 使用 AssetManager 加载配置文件
            InputStream inputStream = context.getAssets().open("config.properties");
            properties.load(inputStream);

            accessKeyId = properties.getProperty("ALI_ACCESS_KEY_ID");
            accessKeySecret = properties.getProperty("ALI_ACCESS_KEY_SECRET");

            Log.d(TAG, "Access Key ID: " + accessKeyId);
            Log.d(TAG, "Access Key Secret: " + accessKeySecret);
        } catch (IOException e) {
            Log.e(TAG, "Error loading keys from config.properties", e);
        }
    }

    /**
     * 初始化OSS客户端
     *
     * @param context 应用上下文
     */
    public void initOSSClient(Context context) {
        if (accessKeyId == null || accessKeySecret == null) {
            Log.e(TAG, "Access keys are not loaded. Call loadKeys() first.");
            return;
        }

        OSSPlainTextAKSKCredentialProvider credentialProvider =
                new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        ossClient = new OSSClient(context, endpoint, credentialProvider);
        Log.d(TAG, "OSSClient initialized successfully.");
    }

    /**
     * 上传图片到OSS
     *
     * @param objectKey OSS中保存的文件名（例如：images/test.jpg）
     * @param filePath  本地图片文件路径
     * @return 上传结果URL
     */
    public String uploadImage(String objectKey, String filePath) {
        if (ossClient == null) {
            Log.e(TAG, "OSSClient is not initialized.");
            return null;
        }

        try {
            // 创建上传请求
            PutObjectRequest put = new PutObjectRequest(bucketName, objectKey, filePath);
            // 上传图片
            PutObjectResult putResult = ossClient.putObject(put);
            Log.d(TAG, "Upload Success: " + putResult.getETag());
            // 返回图片的访问URL
            return "https://" + bucketName + ".oss-cn-hangzhou.aliyuncs.com/" + objectKey;
        } catch (ClientException e) {
            Log.e(TAG, "ClientException: " + e.getMessage());
        } catch (ServiceException e) {
            Log.e(TAG, "ServiceException: " + e.getRawMessage());
        }
        return null;
    }

    /**
     * 下载图片
     *
     * @param objectKey 图片在 OSS 中的路径
     * @param localFile 本地保存图片的路径
     */
    public void downloadImage(String objectKey, File localFile) {
        if (ossClient == null) {
            Log.e(TAG, "OSSClient is not initialized.");
            return;
        }

        // 创建下载请求
        GetObjectRequest get = new GetObjectRequest(bucketName, objectKey);

        try {
            // 执行下载操作
            GetObjectResult result = ossClient.getObject(get);
            InputStream inputStream = result.getObjectContent();

            // 保存图片到本地
            FileOutputStream fos = new FileOutputStream(localFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            inputStream.close();
            Log.d(TAG, "Image downloaded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error downloading image", e);
        }
    }
}
