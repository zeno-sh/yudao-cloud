
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Dimensions {

    @JSONField(name ="height")
    private Long mHeight;
    @JSONField(name ="isValid")
    private Boolean mIsValid;
    @JSONField(name ="length")
    private Long mLength;
    @JSONField(name ="width")
    private Long mWidth;

    public Long getHeight() {
        return mHeight;
    }

    public void setHeight(Long height) {
        mHeight = height;
    }

    public Boolean getIsValid() {
        return mIsValid;
    }

    public void setIsValid(Boolean isValid) {
        mIsValid = isValid;
    }

    public Long getLength() {
        return mLength;
    }

    public void setLength(Long length) {
        mLength = length;
    }

    public Long getWidth() {
        return mWidth;
    }

    public void setWidth(Long width) {
        mWidth = width;
    }

}
