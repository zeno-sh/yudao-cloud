package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdExpenseDTO {
    private List<AdExpenseItemDTO> rows;
}
