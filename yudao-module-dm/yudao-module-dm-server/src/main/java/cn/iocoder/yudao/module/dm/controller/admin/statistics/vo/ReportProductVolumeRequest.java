package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/06/30 22:11
 */
@Data
public class ReportProductVolumeRequest extends ReportBaseRequest {
    private String timeUnit;
}