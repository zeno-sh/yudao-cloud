package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author: Zeno
 * @createTime: 2024/06/30 22:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReportBaseRequest extends PageParam {
    private String[] clientIds;
    private String[] date;
}