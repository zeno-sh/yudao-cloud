
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import java.util.Date;
import java.util.List;
import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Card {

    @JSONField(name ="brand")
    private String mBrand;
    @JSONField(name ="characteristics")
    private List<Characteristic> mCharacteristics;
    @JSONField(name ="createdAt")
    private Date mCreatedAt;
    @JSONField(name ="description")
    private String mDescription;
    @JSONField(name ="dimensions")
    private Dimensions mDimensions;
    @JSONField(name ="imtID")
    private Long mImtID;
    @JSONField(name ="nmID")
    private Long mNmID;
    @JSONField(name ="nmUUID")
    private String mNmUUID;
    @JSONField(name ="photos")
    private List<Photo> mPhotos;
    @JSONField(name ="sizes")
    private List<Size> mSizes;
    @JSONField(name ="subjectID")
    private Long mSubjectID;
    @JSONField(name ="subjectName")
    private String mSubjectName;
    @JSONField(name ="title")
    private String mTitle;
    @JSONField(name ="updatedAt")
    private String mUpdatedAt;
    @JSONField(name ="vendorCode")
    private String mVendorCode;

    public String getBrand() {
        return mBrand;
    }

    public void setBrand(String brand) {
        mBrand = brand;
    }

    public List<Characteristic> getCharacteristics() {
        return mCharacteristics;
    }

    public void setCharacteristics(List<Characteristic> characteristics) {
        mCharacteristics = characteristics;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        mCreatedAt = createdAt;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Dimensions getDimensions() {
        return mDimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        mDimensions = dimensions;
    }

    public Long getImtID() {
        return mImtID;
    }

    public void setImtID(Long imtID) {
        mImtID = imtID;
    }

    public Long getNmID() {
        return mNmID;
    }

    public void setNmID(Long nmID) {
        mNmID = nmID;
    }

    public String getNmUUID() {
        return mNmUUID;
    }

    public void setNmUUID(String nmUUID) {
        mNmUUID = nmUUID;
    }

    public List<Photo> getPhotos() {
        return mPhotos;
    }

    public void setPhotos(List<Photo> photos) {
        mPhotos = photos;
    }

    public List<Size> getSizes() {
        return mSizes;
    }

    public void setSizes(List<Size> sizes) {
        mSizes = sizes;
    }

    public Long getSubjectID() {
        return mSubjectID;
    }

    public void setSubjectID(Long subjectID) {
        mSubjectID = subjectID;
    }

    public String getSubjectName() {
        return mSubjectName;
    }

    public void setSubjectName(String subjectName) {
        mSubjectName = subjectName;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public String getVendorCode() {
        return mVendorCode;
    }

    public void setVendorCode(String vendorCode) {
        mVendorCode = vendorCode;
    }

}
