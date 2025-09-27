package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/10/08 14:39
 */
@Data
public class ClientSimpleInfoDTO {

    private String clientId;

    private String shopName;

    private Integer platform;

    private String platformName;
}
