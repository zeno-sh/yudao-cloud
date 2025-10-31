package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/13
 */
@Data
public class NotifyRequest {
    private String message_type;
    private String seller_id;
    private String posting_number;
}
