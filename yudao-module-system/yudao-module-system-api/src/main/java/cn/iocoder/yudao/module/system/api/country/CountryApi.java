package cn.iocoder.yudao.module.system.api.country;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.api.country.dto.CountryRespDTO;
import cn.iocoder.yudao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 国家信息 API 接口
 *
 * @author Zeno
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 国家信息")
public interface CountryApi {

    String PREFIX = ApiConstants.PREFIX + "/country";

    /**
     * 获得国家信息
     *
     * @param id 国家编号
     * @return 国家信息
     */
    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得国家信息")
    @Parameter(name = "id", description = "国家编号", required = true, example = "1")
    CommonResult<CountryRespDTO> getCountry(@RequestParam("id") Integer id);

    /**
     * 获得国家信息列表
     *
     * @return 国家信息列表
     */
    @GetMapping(PREFIX + "/list")
    @Operation(summary = "获得国家信息列表")
    CommonResult<List<CountryRespDTO>> getCountryList();

    /**
     * 根据国家名称获得国家信息
     *
     * @param country 国家名称
     * @return 国家信息
     */
    @GetMapping(PREFIX + "/get-by-name")
    @Operation(summary = "根据国家名称获得国家信息")
    @Parameter(name = "country", description = "国家名称", required = true, example = "中国")
    CommonResult<CountryRespDTO> getCountryByName(@RequestParam("country") String country);

    /**
     * 根据地区获得国家信息列表
     *
     * @param region 地区
     * @return 国家信息列表
     */
    @GetMapping(PREFIX + "/list-by-region")
    @Operation(summary = "根据地区获得国家信息列表")
    @Parameter(name = "region", description = "地区", required = true, example = "亚洲")
    CommonResult<List<CountryRespDTO>> getCountryListByRegion(@RequestParam("region") String region);

    /**
     * 根据货币代码获得国家信息列表
     *
     * @param currencyCode 货币代码
     * @return 国家信息列表
     */
    @GetMapping(PREFIX + "/list-by-currency")
    @Operation(summary = "根据货币代码获得国家信息列表")
    @Parameter(name = "currencyCode", description = "货币代码", required = true, example = "CNY")
    CommonResult<List<CountryRespDTO>> getCountryListByCurrency(@RequestParam("currencyCode") String currencyCode);

} 