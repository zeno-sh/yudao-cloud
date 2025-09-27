package cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.message.ad;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/11/26 21:05
 */
@Data
public class AdMessage {

    private Long tenantId;

    private String clientId;

    private String beginDate;

    private String endDate;

}
