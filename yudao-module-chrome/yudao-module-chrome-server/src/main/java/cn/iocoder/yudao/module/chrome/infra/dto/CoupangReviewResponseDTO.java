package cn.iocoder.yudao.module.chrome.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Coupang评论API响应数据传输对象
 *
 * @author Jax
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoupangReviewResponseDTO {

    /**
     * 响应代码
     */
    @JsonProperty("rCode")
    private String rCode;

    /**
     * 重定向代码
     */
    @JsonProperty("rRedirectCode")
    private String rRedirectCode;

    /**
     * 重定向URL
     */
    @JsonProperty("rRedirectUrl")
    private String rRedirectUrl;

    /**
     * 视图名称
     */
    @JsonProperty("rViewName")
    private String rViewName;

    /**
     * 响应消息
     */
    @JsonProperty("rMessage")
    private String rMessage;

    /**
     * 响应数据
     */
    @JsonProperty("rData")
    private ReviewData rData;

    /**
     * 评论数据
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewData {
        /**
         * Web构建号
         */
        @JsonProperty("webbuildNo")
        private String webbuildNo;

        /**
         * 是否登录
         */
        @JsonProperty("isLogin")
        private Boolean isLogin;

        /**
         * 会员ID
         */
        @JsonProperty("memberId")
        private String memberId;

        /**
         * 追踪客户ID
         */
        @JsonProperty("trCid")
        private String trCid;

        /**
         * 追踪广告ID
         */
        @JsonProperty("trAid")
        private String trAid;

        /**
         * 是否为评论激励目标
         */
        @JsonProperty("isIncentiveForReviewTarget")
        private Boolean isIncentiveForReviewTarget;

        /**
         * 视频是否可用
         */
        @JsonProperty("videoAvailable")
        private Boolean videoAvailable;

        /**
         * 评分汇总总计
         */
        @JsonProperty("ratingSummaryTotal")
        private RatingSummaryTotal ratingSummaryTotal;

        /**
         * 选定市场评分汇总总计
         */
        @JsonProperty("selectedMarketRatingSummaryTotal")
        private RatingSummaryTotal selectedMarketRatingSummaryTotal;

        /**
         * 评分选定数量
         */
        @JsonProperty("ratingSelectedCount")
        private Integer ratingSelectedCount;

        /**
         * 分页信息
         */
        @JsonProperty("paging")
        private Paging paging;

        @JsonProperty("reviewTotalCount")
        private Integer reviewTotalCount;
    }

    /**
     * 评分汇总总计
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RatingSummaryTotal {
        /**
         * 评分数量
         */
        @JsonProperty("ratingCount")
        private Integer ratingCount;

        /**
         * 平均评分
         */
        @JsonProperty("ratingAverage")
        private Integer ratingAverage;

        /**
         * 评分汇总列表
         */
        @JsonProperty("ratingSummaries")
        private List<RatingSummary> ratingSummaries;
    }

    /**
     * 评分汇总
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RatingSummary {
        /**
         * ID
         */
        private Long id;

        /**
         * 产品ID
         */
        private Long productId;

        /**
         * 评分
         */
        private Integer rating;

        /**
         * 数量
         */
        private Integer count;

        /**
         * 百分比
         */
        private Integer percentage;
    }

    /**
     * 分页信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paging {
        /**
         * 页码
         */
        private Integer page;

        /**
         * 当前页
         */
        private Integer currentPage;

        /**
         * 每页大小
         */
        private Integer sizePerPage;

        /**
         * 显示页码栏限制
         */
        private Integer displayPageBarLimit;

        /**
         * 总数量
         */
        private Integer totalCount;

        /**
         * 开始页
         */
        private Integer startPage;

        /**
         * 结束页
         */
        private Integer endPage;

        /**
         * 总页数
         */
        private Integer totalPage;

        /**
         * 是否有上一页
         */
        private Boolean isPrev;

        /**
         * 是否有下一页
         */
        private Boolean isNext;

        /**
         * 是否分页
         */
        private Boolean isPaging;

        /**
         * 页码列表
         */
        private List<Integer> pageList;

        /**
         * 评论内容列表
         */
        private List<ReviewContent> contents;
    }

    /**
     * 评论内容
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewContent {
        /**
         * 评论ID
         */
        private Long reviewId;

        /**
         * 产品ID
         */
        private Long productId;

        /**
         * 供应商商品ID
         */
        private Long vendorItemId;

        /**
         * 商品ID
         */
        private Long itemId;

        /**
         * 评分
         */
        private Integer rating;

        /**
         * 评论数量
         */
        private Integer commentCount;

        /**
         * 有用数量
         */
        private Integer helpfulCount;

        /**
         * 有用真实数量
         */
        private Integer helpfulTrueCount;

        /**
         * 有用虚假数量
         */
        private Integer helpfulFalseCount;

        /**
         * 用户是否标记有用
         */
        private Boolean userHelpfulExist;

        /**
         * 用户有用值
         */
        private Boolean userHelpfulValue;

        /**
         * 商品名称
         */
        private String itemName;

        /**
         * 商品图片路径
         */
        private String itemImagePath;

        /**
         * 标题
         */
        private String title;

        /**
         * 内容
         */
        private String content;

        /**
         * 评论时间
         */
        private Long reviewAt;

        /**
         * 会员信息
         */
        private Member member;

        /**
         * 显示作者
         */
        private String displayWriter;

        /**
         * 显示名称
         */
        private String displayName;

        /**
         * 显示作者图标URLs
         */
        private List<String> displayWriterIconUrls;

        /**
         * 是否我的评论
         */
        private Boolean isMyReview;

        /**
         * 评论调查答案
         */
        private List<Object> reviewSurveyAnswers;

        /**
         * 附件列表
         */
        private List<Attachment> attachments;

        /**
         * 视频附件列表
         */
        private List<Object> videoAttachments;

        /**
         * 评论者显示图片
         */
        private ReviewerDisplayImage reviewerDisplayImage;

        /**
         * 是否冒险者评论
         */
        private Boolean isAdventurerReview;

        /**
         * 评论者等级
         */
        private String reviewerRank;

        /**
         * 评论者徽章
         */
        private List<Object> reviewerBadges;

        /**
         * 上一个图片评论ID
         */
        private Long previousImageReviewId;

        /**
         * 下一个图片评论ID
         */
        private Long nextImageReviewId;

        /**
         * 创建时间
         */
        private Long createdAt;

        /**
         * 内容高亮DTO
         */
        private Object contentHighlightingDto;

        /**
         * 标题高亮DTO
         */
        private Object titleHighlightingDto;

        /**
         * 额外评论信息
         */
        private Object additionalReviewInfo;

        /**
         * 供应商名称
         */
        private String vendorName;

        /**
         * 是否需要更多
         */
        private Boolean needMore;
    }

    /**
     * 会员信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Member {
        /**
         * 类型
         */
        private String type;

        /**
         * ID
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 邮箱
         */
        private String email;
    }

    /**
     * 附件
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        /**
         * ID
         */
        private Long id;

        /**
         * 上传文件路径
         */
        private String uploadedFilePath;

        /**
         * 排序
         */
        private Integer ordering;

        /**
         * 原始图片源
         */
        private String imgSrcOrigin;

        /**
         * 缩略图源
         */
        private String imgSrcThumbnail;

        /**
         * 评论ID
         */
        private Long reviewId;

        /**
         * 索引
         */
        private Integer index;

        /**
         * 是否需要换行
         */
        private Boolean needNewLine;

        /**
         * 附件类型
         */
        private String attachmentType;

        /**
         * 标题
         */
        private String caption;
    }

    /**
     * 评论者显示图片
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewerDisplayImage {
        /**
         * 显示图片原始路径
         */
        private String displayImageOriginalPath;

        /**
         * 显示图片缩略图路径
         */
        private String displayImageThumbnailPath;

        /**
         * 显示图片是否被屏蔽
         */
        private Boolean displayImageBlinded;

        /**
         * 显示图片是否私有
         */
        private Boolean displayImagePrivate;
    }
}