
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Warehouse {

    @Expose
    private Long cargoType;
    @Expose
    private Long deliveryType;
    @Expose
    private Long id;
    @Expose
    private String name;
    @Expose
    private Long officeId;

    public Long getCargoType() {
        return cargoType;
    }

    public void setCargoType(Long cargoType) {
        this.cargoType = cargoType;
    }

    public Long getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Long deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

}
