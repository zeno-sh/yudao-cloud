package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/07/13 20:18
 */
@Data
public class WbOrderStatusRequest extends WbHttpBaseRequest{

    private List<Long> orders;
}
