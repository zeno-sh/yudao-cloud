
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import java.util.Date;
import java.util.List;
import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Order {

    @JSONField(name ="article")
    private String mArticle;
    @JSONField(name ="cargoType")
    private Long mCargoType;
    @JSONField(name ="chrtId")
    private Long mChrtId;
    @JSONField(name ="colorCode")
    private String mColorCode;
    @JSONField(name ="convertedCurrencyCode")
    private Long mConvertedCurrencyCode;
    @JSONField(name ="convertedPrice")
    private Long mConvertedPrice;
    @JSONField(name ="createdAt")
    private Date mCreatedAt;
    @JSONField(name ="currencyCode")
    private Long mCurrencyCode;
    @JSONField(name ="deliveryType")
    private String mDeliveryType;
    @JSONField(name ="id")
    private Long mId;
    @JSONField(name ="isZeroOrder")
    private Boolean mIsZeroOrder;
    @JSONField(name ="nmId")
    private Long mNmId;
    @JSONField(name ="offices")
    private List<String> mOffices;
    @JSONField(name ="orderUid")
    private String mOrderUid;
    @JSONField(name ="price")
    private Long mPrice;
    @JSONField(name ="rid")
    private String mRid;
    @JSONField(name ="scanPrice")
    private Long mScanPrice;
    @JSONField(name ="skus")
    private List<String> mSkus;
    @JSONField(name ="supplyId")
    private String mSupplyId;
    @JSONField(name ="user")
    private User mUser;
    @JSONField(name ="warehouseId")
    private Long mWarehouseId;

    // 订单是否取消，兼容字段 for wb
    private Boolean canceled;

    public String getArticle() {
        return mArticle;
    }

    public void setArticle(String article) {
        mArticle = article;
    }

    public Long getCargoType() {
        return mCargoType;
    }

    public void setCargoType(Long cargoType) {
        mCargoType = cargoType;
    }

    public Long getChrtId() {
        return mChrtId;
    }

    public void setChrtId(Long chrtId) {
        mChrtId = chrtId;
    }

    public String getColorCode() {
        return mColorCode;
    }

    public void setColorCode(String colorCode) {
        mColorCode = colorCode;
    }

    public Long getConvertedCurrencyCode() {
        return mConvertedCurrencyCode;
    }

    public void setConvertedCurrencyCode(Long convertedCurrencyCode) {
        mConvertedCurrencyCode = convertedCurrencyCode;
    }

    public Long getConvertedPrice() {
        return mConvertedPrice;
    }

    public void setConvertedPrice(Long convertedPrice) {
        mConvertedPrice = convertedPrice;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        mCreatedAt = createdAt;
    }

    public Long getCurrencyCode() {
        return mCurrencyCode;
    }

    public void setCurrencyCode(Long currencyCode) {
        mCurrencyCode = currencyCode;
    }

    public String getDeliveryType() {
        return mDeliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        mDeliveryType = deliveryType;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public Boolean getIsZeroOrder() {
        return mIsZeroOrder;
    }

    public void setIsZeroOrder(Boolean isZeroOrder) {
        mIsZeroOrder = isZeroOrder;
    }

    public Long getNmId() {
        return mNmId;
    }

    public void setNmId(Long nmId) {
        mNmId = nmId;
    }

    public List<String> getOffices() {
        return mOffices;
    }

    public void setOffices(List<String> offices) {
        mOffices = offices;
    }

    public String getOrderUid() {
        return mOrderUid;
    }

    public void setOrderUid(String orderUid) {
        mOrderUid = orderUid;
    }

    public Long getPrice() {
        return mPrice;
    }

    public void setPrice(Long price) {
        mPrice = price;
    }

    public String getRid() {
        return mRid;
    }

    public void setRid(String rid) {
        mRid = rid;
    }

    public Long getScanPrice() {
        return mScanPrice;
    }

    public void setScanPrice(Long scanPrice) {
        mScanPrice = scanPrice;
    }

    public List<String> getSkus() {
        return mSkus;
    }

    public void setSkus(List<String> skus) {
        mSkus = skus;
    }

    public String getSupplyId() {
        return mSupplyId;
    }

    public void setSupplyId(String supplyId) {
        mSupplyId = supplyId;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public Long getWarehouseId() {
        return mWarehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        mWarehouseId = warehouseId;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }
}
