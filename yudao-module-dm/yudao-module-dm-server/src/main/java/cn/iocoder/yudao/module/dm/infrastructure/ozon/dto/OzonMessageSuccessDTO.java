package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/07/05 17:32
 */
@Data
public class OzonMessageSuccessDTO {
    private String version;
    private String name;
    /**
     * utc时间格式
     */
    private String time;
}
