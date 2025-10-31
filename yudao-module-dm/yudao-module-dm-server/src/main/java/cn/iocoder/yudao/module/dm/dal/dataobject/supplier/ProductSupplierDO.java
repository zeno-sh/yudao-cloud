package cn.iocoder.yudao.module.dm.dal.dataobject.supplier;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 供应商信息 DO
 *
 * @author Zeno
 */
@TableName("dm_product_supplier")
@KeySequence("dm_product_supplier_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplierDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 供应商名称
     */
    private String supplierName;
    /**
     * 供应商代码
     */
    private String supplierCode;
    /**
     * 网址
     */
    private String website;
    /**
     * 备注
     */
    private String remark;
    /**
     * 源头工厂
     *
     * 枚举 {@link TODO dm_first_choice 对应的类}
     */
    private String sourceFactory;

}