package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Data
public class OrderResultDTO {
    private List<PostingDTO> postings;
    private boolean hasNext;
}
