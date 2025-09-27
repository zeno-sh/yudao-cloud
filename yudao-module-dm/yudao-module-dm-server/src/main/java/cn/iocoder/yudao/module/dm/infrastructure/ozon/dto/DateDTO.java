package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/31
 */
@Data
public class DateDTO {
    @JSONField(name = "from")
    private String begin;
    @JSONField(name = "to")
    private String end;
}
