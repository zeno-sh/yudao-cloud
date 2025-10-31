package cn.iocoder.yudao.module.dm.dal.dataobject.transport;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 头程计划明细 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransportPlanDetailDTO extends TransportPlanItemDO {

    /**
     * 发运时间
     */
    private LocalDateTime despatchDate;

    /**
     * 预计抵达时间
     */
    private LocalDateTime arrivalDate;

    /**
     * 实际到达时间
     */
    private LocalDateTime finishedDate;

    /**
     * 货代公司
     */
    private String forwarder;

    /**
     * 发货计划编号
     */
    private String code;

    /**
     * 运输状态
     */
    private Integer transportStatus;
} 