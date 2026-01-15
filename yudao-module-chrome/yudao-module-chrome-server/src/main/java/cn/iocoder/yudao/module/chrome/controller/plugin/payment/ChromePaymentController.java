package cn.iocoder.yudao.module.chrome.controller.plugin.payment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentCreateReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentRespVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentSubmitReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentSubmitRespVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
import cn.iocoder.yudao.module.chrome.service.payment.ChromePaymentService;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.SUBSCRIPTION_ORDER_NOT_EXISTS;

/**
 * Chrome 支付控制器
 *
 * @author Jax
 */
@Tag(name = "插件端 - Chrome 支付")
@RestController
@RequestMapping("/chrome/payment")
@Validated
@Slf4j
public class ChromePaymentController {

    @Resource
    private ChromePaymentService chromePaymentService;

    @Resource
    private SubscriptionOrderMapper subscriptionOrderMapper;

    @Resource
    private PayOrderService payOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建支付订单")
    @PermitAll // 无需登录，通过邮箱识别用户（建议前端先验证邮箱验证码）
    public CommonResult<Long> createPaymentOrder(@Valid @RequestBody ChromePaymentCreateReqVO createReqVO) {
        // 创建支付订单，返回订单ID
        // 前端拿到订单ID后，通过get接口获取payOrderId，然后调用chrome/payment/submit提交支付
        return success(chromePaymentService.createPaymentOrder(createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取支付订单详情")
    @Parameter(name = "orderId", description = "订单ID", required = true, example = "1")
    @PermitAll // 无需登录（可考虑添加订单号+邮箱验证）
    public CommonResult<ChromePaymentRespVO> getPaymentOrder(@RequestParam("orderId") Long orderId) {
        // 1. 获取订单信息
        SubscriptionOrderDO order = subscriptionOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 2. 构建返回结果
        ChromePaymentRespVO respVO = ChromePaymentRespVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .payOrderId(order.getPayOrderId())
                .actualPrice(order.getActualPrice())
                .currency(order.getCurrency())
                .paymentStatus(order.getPaymentStatus())
                .expireTime(order.getExpireTime())
                .build();

        return success(respVO);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交支付订单")
    @PermitAll // 无需登录，插件端通过邮箱识别用户
    public CommonResult<ChromePaymentSubmitRespVO> submitPayOrder(@Valid @RequestBody ChromePaymentSubmitReqVO reqVO) {
        // 提交支付，调用pay模块service
        reqVO.setChannelCode(PayChannelEnum.ALIPAY_QR.getCode());
        reqVO.setDisplayMode(PayOrderDisplayModeEnum.BAR_CODE.getMode());

        PayOrderSubmitRespVO respVO = payOrderService.submitOrder(BeanUtils.toBean(reqVO, PayOrderSubmitReqVO.class), getClientIP());
        return success(BeanUtils.toBean(respVO, ChromePaymentSubmitRespVO.class));
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消支付订单")
    @Parameter(name = "orderId", description = "订单ID", required = true, example = "1")
    @PermitAll // 无需登录
    public CommonResult<Boolean> cancelPaymentOrder(@RequestParam("orderId") Long orderId) {
        chromePaymentService.cancelOrder(orderId);
        return success(true);
    }

    @PostMapping("/notify-paid")
    @Operation(summary = "支付成功回调接口")
    @PermitAll // 无需登录，由支付模块回调，安全由内部校验支付单合法性保证
    public CommonResult<Boolean> notifyPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        log.info("[notifyPaid][收到支付成功回调，订单号:{}，支付单ID:{}]",
                notifyReqDTO.getMerchantOrderId(), notifyReqDTO.getPayOrderId());

        // 更新订单为已支付（merchantOrderId就是订单号orderNo）
        chromePaymentService.updateOrderPaid(
                notifyReqDTO.getMerchantOrderId(),
                notifyReqDTO.getPayOrderId());

        return success(true);
    }

    @GetMapping("/check-status")
    @Operation(summary = "检查订单支付状态")
    @Parameter(name = "orderId", description = "订单ID", required = true, example = "1")
    @PermitAll // 无需登录
    public CommonResult<Boolean> checkPaymentStatus(@RequestParam("orderId") Long orderId) {
        // 1. 获取订单信息
        SubscriptionOrderDO order = subscriptionOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 2. 返回是否已支付
        return success(order.getPaymentStatus() == 20); // 20为已支付状态
    }
}
