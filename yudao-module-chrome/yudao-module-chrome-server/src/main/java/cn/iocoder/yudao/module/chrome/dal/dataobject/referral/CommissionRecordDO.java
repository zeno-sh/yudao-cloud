package cn.iocoder.yudao.module.chrome.dal.dataobject.referral;

import lombok.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 推广佣金记录 DO
 *
 * @author Jax
 */
@TableName("chrome_commission_record")
@KeySequence("chrome_commission_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRecordDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 推广者用户ID
     */
    private Long referrerUserId;
    /**
     * 被推广者用户ID
     */
    private Long inviteeUserId;
    /**
     * 关联的订阅订单ID
     */
    private Long orderId;
    /**
     * 订单原始金额
     */
    private BigDecimal orderAmount;
    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;
    /**
     * 佣金金额
     */
    private BigDecimal commissionAmount;
    /**
     * 状态：10-待结算, 20-已结算, 30-已取消
     */
    private Integer status;

}
