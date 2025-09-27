package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2025/03/24 20:21
 */
@Data
public class ProductOnlineResponse {
    private List<ProductOnlineDTO> items;
}
