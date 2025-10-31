package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/08/05 16:20
 */
@Data
public class ProductVolumeReqVO extends ReportBaseRequest{
    /**
     * 货号
     */
    private String offerId;
    /**
     * 日期类型
     * @see cn.iocoder.yudao.module.dm.enums.DateTypeEnum
     */
    private Integer dateType;
    /**
     * 查询类型
     * @see cn.iocoder.yudao.module.dm.enums.VolumeQueryTypeEnum
     */
    private String queryType;
    /**
     * 本地 productId
     */
    private Long productId;
}
