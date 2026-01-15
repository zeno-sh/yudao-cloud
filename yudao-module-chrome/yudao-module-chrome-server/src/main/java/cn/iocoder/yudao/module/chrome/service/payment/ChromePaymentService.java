package cn.iocoder.yudao.module.chrome.service.payment;

import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentCreateReqVO;

import javax.validation.Valid;

/**
 * Chrome 支付服务接口
 *
 * @author Jax
 */
public interface ChromePaymentService {

    /**
     * 创建支付订单
     * 
     * @param createReqVO 创建请求（包含用户邮箱）
     * @return 订单ID
     */
    Long createPaymentOrder(@Valid ChromePaymentCreateReqVO createReqVO);

    /**
     * 更新订单为已支付状态
     * 
     * @param orderNo 订单号
     * @param payOrderId 支付订单ID
     */
    void updateOrderPaid(String orderNo, Long payOrderId);

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     */
    void cancelOrder(Long orderId);
}
