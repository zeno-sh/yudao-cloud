package cn.iocoder.yudao.module.dm.infrastructure.ozon.response;

import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class OzonHttpResponse<T> {
    private T result;
}
