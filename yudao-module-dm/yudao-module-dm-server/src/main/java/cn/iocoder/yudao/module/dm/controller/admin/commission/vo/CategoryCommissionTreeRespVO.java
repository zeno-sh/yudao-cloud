package cn.iocoder.yudao.module.dm.controller.admin.commission.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/04/17 16:51
 */
@Data
public class CategoryCommissionTreeRespVO {

    private Long id;

    private Long parentId;

    private String categoryName;

    private BigDecimal rate;

    private List<CategoryCommissionTreeRespVO> childrenList;
}
