
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Settings {

    @SerializedName("cursor")
    private Cursor mCursor;
    @SerializedName("filter")
    private Filter mFilter;
    @SerializedName("sort")
    private Sort mSort;

    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

    public Filter getFilter() {
        return mFilter;
    }

    public void setFilter(Filter filter) {
        mFilter = filter;
    }

    public Sort getSort() {
        return mSort;
    }

    public void setSort(Sort sort) {
        mSort = sort;
    }

}
