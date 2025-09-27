package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/07/05 17:30
 */
@Data
public class OzonMessageErrorDetailDTO {
    private String code;
    private String message;
    private String detail;
}
