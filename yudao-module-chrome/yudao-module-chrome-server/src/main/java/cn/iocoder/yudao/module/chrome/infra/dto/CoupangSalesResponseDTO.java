package cn.iocoder.yudao.module.chrome.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Coupang销量查询API响应数据传输对象
 *
 * @author Jax
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoupangSalesResponseDTO {

    /**
     * 下一页搜索页码
     */
    @JsonProperty("nextSearchPage")
    private Integer nextSearchPage;

    /**
     * 搜索结果列表
     */
    @JsonProperty("result")
    private List<ProductSalesInfo> result;

    /**
     * 是否有下一页
     */
    @JsonProperty("hasNext")
    private Boolean hasNext;

    /**
     * 产品销量信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductSalesInfo {

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
         * 品牌名称
         */
        @JsonProperty("brandName")
        private String brandName;

        /**
         * 商品ID
         */
        @JsonProperty("itemId")
        private Long itemId;

        /**
         * 商品名称
         */
        @JsonProperty("itemName")
        private String itemName;

        /**
         * 分类显示信息
         */
        @JsonProperty("displayCategoryInfo")
        private List<CategoryInfo> displayCategoryInfo;

        /**
         * 制造商
         */
        @JsonProperty("manufacture")
        private String manufacture;

        /**
         * 分类ID
         */
        @JsonProperty("categoryId")
        private Integer categoryId;

        /**
         * 产品下商品数量
         */
        @JsonProperty("itemCountOfProduct")
        private Integer itemCountOfProduct;

        /**
         * 图片路径
         */
        @JsonProperty("imagePath")
        private String imagePath;

        /**
         * 匹配类型
         */
        @JsonProperty("matchType")
        private String matchType;

        /**
         * 销售价格
         */
        @JsonProperty("salePrice")
        private Long salePrice;

        /**
         * 供应商商品ID
         */
        @JsonProperty("vendorItemId")
        private Long vendorItemId;

        /**
         * 评分数量
         */
        @JsonProperty("ratingCount")
        private Integer ratingCount;

        /**
         * 评分
         */
        @JsonProperty("rating")
        private Double rating;

        /**
         * 是否赞助
         */
        @JsonProperty("sponsored")
        private String sponsored;

        /**
         * 匹配结果ID
         */
        @JsonProperty("matchingResultId")
        private String matchingResultId;

        /**
         * 最近28天页面访问量
         */
        @JsonProperty("pvLast28Day")
        private Long pvLast28Day;

        /**
         * 最近28天销量
         */
        @JsonProperty("salesLast28d")
        private Long salesLast28d;

        /**
         * 配送方式
         */
        @JsonProperty("deliveryMethod")
        private String deliveryMethod;

        /**
         * 属性类型
         */
        @JsonProperty("attributeTypes")
        private String attributeTypes;
    }

    /**
     * 分类信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryInfo {

        /**
         * 叶子分类代码
         */
        @JsonProperty("leafCategoryCode")
        private Integer leafCategoryCode;

        /**
         * 根分类代码
         */
        @JsonProperty("rootCategoryCode")
        private Integer rootCategoryCode;

        /**
         * 分类层级
         */
        @JsonProperty("categoryHierarchy")
        private String categoryHierarchy;
    }
}
