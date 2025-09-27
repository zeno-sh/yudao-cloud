package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 23:01
 */
@Data
public class FbsPlatformWarehouseReqVO {

    @Size(min = 1, message = "门店不能为空")
    private List<String> clientIds;
}
