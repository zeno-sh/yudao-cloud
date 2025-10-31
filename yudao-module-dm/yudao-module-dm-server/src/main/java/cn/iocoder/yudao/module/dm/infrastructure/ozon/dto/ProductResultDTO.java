package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/8
 */
@Data
public class ProductResultDTO {

    private Integer total;

    private List<ProductOnlineItemDTO> items;
}
