package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: Zeno
 * @createTime: 2024/09/01 21:37
 */
@Data
public class FbsPushInfoVO {

    private Boolean success;

    private String message;

    private LocalDateTime pushDateTime;

    private String request;
}
