package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: Zeno
 * @createTime: 2024/08/13 11:58
 */
@Slf4j
public class UrlImageConverter implements Converter<String> {
    public static int urlConnectTimeout = 2000;
    public static int urlReadTimeout = 6000;

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String url, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws IOException {
        InputStream inputStream = null;
        try {
            if (ObjectUtils.isEmpty(url)){
                return new WriteCellData<>("图片链接为空");
            }

            // 使用Hutool的HttpRequest发起请求
            HttpResponse response = HttpRequest.get(url)
                    .timeout(urlConnectTimeout + urlReadTimeout) // 设置连接和读取超时时间
                    .execute();

            // 检查响应状态
            if (response.getStatus() != 200) {
                log.info("图片获取异常，响应状态码：{}", response.getStatus());
                return new WriteCellData<>("图片获取异常");
            }

            // 获取输入流并转换为字节数组
            inputStream = response.bodyStream();
            byte[] bytes = IoUtil.readBytes(inputStream);
            return new WriteCellData<>(bytes);
        } catch (Exception e) {
            log.info("图片获取异常", e);
            return new WriteCellData<>("图片获取异常");
        } finally {
            IoUtil.close(inputStream);
        }
    }
}
