
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Filter {

    @SerializedName("allowedCategoriesOnly")
    private Boolean mAllowedCategoriesOnly;
    @SerializedName("brands")
    private List<Object> mBrands;
    @SerializedName("imtID")
    private Long mImtID;
    @SerializedName("objectIDs")
    private List<Object> mObjectIDs;
    @SerializedName("tagIDs")
    private List<Object> mTagIDs;
    @SerializedName("textSearch")
    private String mTextSearch;
    @SerializedName("withPhoto")
    private Long mWithPhoto;

    public Boolean getAllowedCategoriesOnly() {
        return mAllowedCategoriesOnly;
    }

    public void setAllowedCategoriesOnly(Boolean allowedCategoriesOnly) {
        mAllowedCategoriesOnly = allowedCategoriesOnly;
    }

    public List<Object> getBrands() {
        return mBrands;
    }

    public void setBrands(List<Object> brands) {
        mBrands = brands;
    }

    public Long getImtID() {
        return mImtID;
    }

    public void setImtID(Long imtID) {
        mImtID = imtID;
    }

    public List<Object> getObjectIDs() {
        return mObjectIDs;
    }

    public void setObjectIDs(List<Object> objectIDs) {
        mObjectIDs = objectIDs;
    }

    public List<Object> getTagIDs() {
        return mTagIDs;
    }

    public void setTagIDs(List<Object> tagIDs) {
        mTagIDs = tagIDs;
    }

    public String getTextSearch() {
        return mTextSearch;
    }

    public void setTextSearch(String textSearch) {
        mTextSearch = textSearch;
    }

    public Long getWithPhoto() {
        return mWithPhoto;
    }

    public void setWithPhoto(Long withPhoto) {
        mWithPhoto = withPhoto;
    }

}
