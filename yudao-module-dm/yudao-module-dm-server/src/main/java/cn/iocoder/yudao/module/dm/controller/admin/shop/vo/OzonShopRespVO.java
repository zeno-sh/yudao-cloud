package cn.iocoder.yudao.module.dm.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/06/14 17:31
 */
@Schema(description = "管理后台 - ozon店铺")
@Data
public class OzonShopRespVO {

    private Long id;

    private String shopName;
}
