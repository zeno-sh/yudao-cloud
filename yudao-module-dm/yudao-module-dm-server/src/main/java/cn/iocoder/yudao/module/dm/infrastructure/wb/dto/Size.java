
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import java.util.List;
import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Size {

    @JSONField(name ="chrtID")
    private Long mChrtID;
    @JSONField(name ="skus")
    private List<String> mSkus;
    @JSONField(name ="techSize")
    private String mTechSize;
    @JSONField(name ="wbSize")
    private String mWbSize;

    public Long getChrtID() {
        return mChrtID;
    }

    public void setChrtID(Long chrtID) {
        mChrtID = chrtID;
    }

    public List<String> getSkus() {
        return mSkus;
    }

    public void setSkus(List<String> skus) {
        mSkus = skus;
    }

    public String getTechSize() {
        return mTechSize;
    }

    public void setTechSize(String techSize) {
        mTechSize = techSize;
    }

    public String getWbSize() {
        return mWbSize;
    }

    public void setWbSize(String wbSize) {
        mWbSize = wbSize;
    }

}
