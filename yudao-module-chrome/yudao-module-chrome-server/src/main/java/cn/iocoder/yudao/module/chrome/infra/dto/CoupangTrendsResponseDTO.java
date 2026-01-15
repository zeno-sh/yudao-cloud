package cn.iocoder.yudao.module.chrome.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Coupang趋势查询API响应数据传输对象
 *
 * @author Jax
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoupangTrendsResponseDTO {

    /**
     * 相关关键词列表
     */
    @JsonProperty("relatedKeywords")
    private List<String> relatedKeywords;

    /**
     * 搜索商品列表
     */
    @JsonProperty("searchItems")
    private List<SearchItem> searchItems;

    /**
     * 搜索商品信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchItem {

        /**
         * 品牌名称
         */
        @JsonProperty("brandName")
        private String brandName;

        /**
         * 类目ID
         */
        @JsonProperty("categoryId")
        private Long categoryId;

        /**
         * 显示类目信息列表
         */
        @JsonProperty("displayCategoryInfos")
        private List<DisplayCategoryInfo> displayCategoryInfos;

        /**
         * 图片路径
         */
        @JsonProperty("imagePath")
        private String imagePath;

        /**
         * 商品项ID
         */
        @JsonProperty("itemId")
        private Long itemId;

        /**
         * 商品项名称
         */
        @JsonProperty("itemName")
        private String itemName;

        /**
         * 上架资格
         */
        @JsonProperty("listingEligibility")
        private String listingEligibility;

        /**
         * 最近28天最低页面浏览量
         */
        @JsonProperty("lowerPvLast28d")
        private Long lowerPvLast28d;

        /**
         * 制造商
         */
        @JsonProperty("manufacture")
        private String manufacture;

        /**
         * 可合并状态
         */
        @JsonProperty("mergeableStatus")
        private String mergeableStatus;

        /**
         * 产品ID
         */
        @JsonProperty("productId")
        private Long productId;

        /**
         * 产品名称
         */
        @JsonProperty("productName")
        private String productName;

        /**
         * 最近28天页面浏览量排名
         */
        @JsonProperty("pvLast28dRank")
        private Integer pvLast28dRank;

        /**
         * 昨日页面浏览量
         */
        @JsonProperty("pvLastDay")
        private Long pvLastDay;

        /**
         * 评分
         */
        @JsonProperty("rating")
        private Double rating;

        /**
         * 评分数量
         */
        @JsonProperty("ratingCount")
        private Integer ratingCount;

        /**
         * 销售价格
         */
        @JsonProperty("salesPrice")
        private SalesPrice salesPrice;

        /**
         * 最近28天最高页面浏览量
         */
        @JsonProperty("upperPvLast28d")
        private Long upperPvLast28d;

        /**
         * 供应商商品ID
         */
        @JsonProperty("vendorItemId")
        private Long vendorItemId;
    }

    /**
     * 显示类目信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisplayCategoryInfo {

        /**
         * 类目层级
         */
        @JsonProperty("categoryHierarchy")
        private String categoryHierarchy;

        /**
         * 叶子类目代码
         */
        @JsonProperty("leafCategoryCode")
        private Long leafCategoryCode;

        /**
         * 根类目代码
         */
        @JsonProperty("rootCategoryCode")
        private Long rootCategoryCode;
    }

    /**
     * 销售价格
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SalesPrice {

        /**
         * 金额
         */
        @JsonProperty("amount")
        private Long amount;

        /**
         * 货币
         */
        @JsonProperty("currency")
        private String currency;
    }
}
