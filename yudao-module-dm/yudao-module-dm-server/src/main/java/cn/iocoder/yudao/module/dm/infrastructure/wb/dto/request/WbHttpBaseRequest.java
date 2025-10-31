package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/07/10 23:17
 */
@Data
public class WbHttpBaseRequest {

    private String clientId;
    private String token;
}
