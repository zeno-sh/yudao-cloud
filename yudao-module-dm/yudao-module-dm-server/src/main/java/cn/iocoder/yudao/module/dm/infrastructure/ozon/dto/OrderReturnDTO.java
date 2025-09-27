package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/28
 */
@Data
public class OrderReturnDTO {

    private List<ReturnItemDTO> returns;

    @JSONField(name = "last_id")
    private Integer lastId;
}
