package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/4
 */
@Data
public class AcceptedDateDTO {
    @JSONField(name = "time_from")
    private String begin;
    @JSONField(name = "time_to")
    private String end;
}
