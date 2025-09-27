package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.iocoder.yudao.module.dm.infrastructure.service.dto.ProductVolumeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/05 16:28
 */
@Data
public class ProductVolumeRespVO {

    private List<String> dateList;
    private List<ProductVolumeDTO> volumeList;
}
