package cn.iocoder.yudao.module.dm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.PurchaseOrderRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author: Zeno
 * @createTime: 2024/07/16 21:39
 */
@Tag(name = "管理后台 - 报表Api")
@RestController
@RequestMapping("/dm/jimu/api")
@Validated
public class JimuReportApiController {

    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/purchaser-order")
    @Operation(summary = "获取采购单数据")
    public JSONObject getPurchaseData(@RequestParam("orderId") Long orderId) {

        PurchaseOrderDO purchaseOrder = purchaseOrderService.getPurchaseOrder(Long.valueOf(orderId));
        PurchaseOrderRespVO purchaseOrderVO = BeanUtils.toBean(purchaseOrder, PurchaseOrderRespVO.class);

        Long userId = Objects.isNull(purchaseOrder.getOwner()) ? Long.valueOf(purchaseOrder.getCreator()) : purchaseOrder.getOwner();
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(Lists.newArrayList(userId));

        // 积木报表的规则：http://report.jeecg.com/2114258
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", Lists.newArrayList(purchaseOrderVO));
        return jsonObject;
    }
}
