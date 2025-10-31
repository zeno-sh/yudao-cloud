package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Ozon 供应订单商品请求
 *
 * @author Zeno
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplyOrderBundleRequest extends HttpBaseRequest {
    @JSONField(name = "bundle_ids")
    private List<String> bundleIds;
    @JSONField(name = "limit")
    private Integer limit;
    @JSONField(name = "last_id")
    private String lastId;
} 