package cn.iocoder.yudao.module.dm.infrastructure.service;

/**
 * @author: Zeno
 * @createTime: 2024/08/05 16:18
 */
public interface ProductVolumeQueryService<R,Q> {

    R queryProductVolume(Q productVolumeQueryRequest);
}
