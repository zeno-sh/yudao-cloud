package cn.iocoder.yudao.module.dm.infrastructure.service;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
public interface OrderQueryService<R,Q> {

    R queryOrder(Q orderQueryRequest);
}
