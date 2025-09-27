package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class FinancialDataDTO {
    private List<ProductFinancialDataDTO> products;
    @JSONField(name = "cluster_from")
    private String clusterFrom;
    @JSONField(name = "cluster_to")
    private String clusterTo;
}
