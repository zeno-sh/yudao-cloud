package cn.iocoder.yudao.module.chrome.infra.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 评论查询参数DTO
 *
 * @author Jax
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQueryParamsDTO {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 查询模式
     */
    private QueryMode queryMode;

    /**
     * 页码（从1开始）
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 排序方式
     */
    private String sortBy;

    /**
     * 开始时间（时间戳，毫秒）
     */
    private Long startTime;

    /**
     * 结束时间（时间戳，毫秒）
     */
    private Long endTime;

    /**
     * 最大页数限制
     */
    private Integer maxPages;

    /**
     * 查询模式枚举
     */
    public enum QueryMode {
        /**
         * 默认查询
         */
        DEFAULT,
        /**
         * 全量查询
         */
        ALL,
        /**
         * 查询最早评论
         */
        EARLIEST,
        /**
         * 指定时间范围查询
         */
        TIME_RANGE
    }

}