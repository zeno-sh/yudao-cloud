
package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import java.util.List;
import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.Expose;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class WarehouseDTO {

    @JSONField(name = "can_print_act_in_advance")
    private Boolean canPrintActInAdvance;
    @JSONField(name = "has_entrusted_acceptance")
    private Boolean hasEntrustedAcceptance;
    @JSONField(name = "has_postings_limit")
    private Boolean hasPostingsLimit;
    @JSONField(name = "is_able_to_set_price")
    private Boolean isAbleToSetPrice;
    @JSONField(name = "is_economy")
    private Boolean isEconomy;
    @JSONField(name = "is_karantin")
    private Boolean isKarantin;
    @JSONField(name = "is_kgt")
    private Boolean isKgt;
    @JSONField(name = "is_rfbs")
    private Boolean isRfbs;
    @JSONField(name = "is_timetable_editable")
    private Boolean isTimetableEditable;
    @JSONField(name = "min_postings_limit")
    private Long minPostingsLimit;
    @JSONField(name = "min_working_days")
    private Long minWorkingDays;
    @Expose
    private String name;
    @JSONField(name = "postings_limit")
    private Long postingsLimit;
    @Expose
    private String status;
    @JSONField(name = "warehouse_id")
    private Long warehouseId;
    @JSONField(name = "working_days")
    private List<Long> workingDays;

    public Boolean getCanPrintActInAdvance() {
        return canPrintActInAdvance;
    }

    public void setCanPrintActInAdvance(Boolean canPrintActInAdvance) {
        this.canPrintActInAdvance = canPrintActInAdvance;
    }

    public Boolean getHasEntrustedAcceptance() {
        return hasEntrustedAcceptance;
    }

    public void setHasEntrustedAcceptance(Boolean hasEntrustedAcceptance) {
        this.hasEntrustedAcceptance = hasEntrustedAcceptance;
    }

    public Boolean getHasPostingsLimit() {
        return hasPostingsLimit;
    }

    public void setHasPostingsLimit(Boolean hasPostingsLimit) {
        this.hasPostingsLimit = hasPostingsLimit;
    }

    public Boolean getIsAbleToSetPrice() {
        return isAbleToSetPrice;
    }

    public void setIsAbleToSetPrice(Boolean isAbleToSetPrice) {
        this.isAbleToSetPrice = isAbleToSetPrice;
    }

    public Boolean getIsEconomy() {
        return isEconomy;
    }

    public void setIsEconomy(Boolean isEconomy) {
        this.isEconomy = isEconomy;
    }

    public Boolean getIsKarantin() {
        return isKarantin;
    }

    public void setIsKarantin(Boolean isKarantin) {
        this.isKarantin = isKarantin;
    }

    public Boolean getIsKgt() {
        return isKgt;
    }

    public void setIsKgt(Boolean isKgt) {
        this.isKgt = isKgt;
    }

    public Boolean getIsRfbs() {
        return isRfbs;
    }

    public void setIsRfbs(Boolean isRfbs) {
        this.isRfbs = isRfbs;
    }

    public Boolean getIsTimetableEditable() {
        return isTimetableEditable;
    }

    public void setIsTimetableEditable(Boolean isTimetableEditable) {
        this.isTimetableEditable = isTimetableEditable;
    }

    public Long getMinPostingsLimit() {
        return minPostingsLimit;
    }

    public void setMinPostingsLimit(Long minPostingsLimit) {
        this.minPostingsLimit = minPostingsLimit;
    }

    public Long getMinWorkingDays() {
        return minWorkingDays;
    }

    public void setMinWorkingDays(Long minWorkingDays) {
        this.minWorkingDays = minWorkingDays;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPostingsLimit() {
        return postingsLimit;
    }

    public void setPostingsLimit(Long postingsLimit) {
        this.postingsLimit = postingsLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<Long> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(List<Long> workingDays) {
        this.workingDays = workingDays;
    }

}
