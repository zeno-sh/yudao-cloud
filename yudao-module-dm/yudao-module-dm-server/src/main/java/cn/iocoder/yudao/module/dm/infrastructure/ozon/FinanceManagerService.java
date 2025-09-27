package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.OzonFinanceTransactionSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.*;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FinanceTransactionRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * @Author zeno
 * @Date 2024/1/28
 * @Description 交易清单
 */
@Service
@Slf4j
public class FinanceManagerService {

    @Resource
    private OzonFinanceTransactionService ozonFinanceTransactionService;
    @Resource
    private OzonHttpUtil<FinanceTransactionRequest> ozonHttpUtil;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    /**
     *
     * @param clientId
     * @param beginDate         yyyy-MM-dd'T'HH:mm:ss'Z'
     * @param endDate           yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public void doSync(String clientId, String beginDate, String endDate) {

        OzonShopMappingDO dmShopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == dmShopMapping) {
            log.error("没有找到店铺信息");
            return;
        }

        if (StringUtils.isBlank(dmShopMapping.getApiKey())) {
            log.error("门店ID：{}，未配置密钥", dmShopMapping.getClientId());
            return;
        }

        FinanceTransactionRequest request = new FinanceTransactionRequest();
        request.setClientId(dmShopMapping.getClientId());
        request.setApiKey(dmShopMapping.getApiKey());
        FinanceFilter filter = new FinanceFilter();

        DateDTO dateDTO = new DateDTO();
        dateDTO.setBegin(beginDate);
        dateDTO.setEnd(endDate);

        filter.setDate(dateDTO);

        request.setPage(1);
        request.setPageSize(1000);
        request.setFilter(filter);

        saveFinanceTransaction(request, dmShopMapping);
    }

    private void saveFinanceTransaction(FinanceTransactionRequest request, OzonShopMappingDO dmShopMapping) {
        OzonHttpResponse<FinanceTransactionResultDTO> httpResponse = getFinanceTransaction(request);

        if (httpResponse.getResult() != null) {
            FinanceTransactionResultDTO transactionResultDTO = httpResponse.getResult();
            Integer pageSize = request.getPageSize(); // 请求参数中的每页大小
            Integer rowCount = transactionResultDTO.getRowCount(); // 总记录数
            Integer pageCount = transactionResultDTO.getPageCount(); // 总页数

            // 插入第一页的数据
            insert(dmShopMapping, transactionResultDTO.getOperations());

            // 计算总页数，确保分页循环正确（pageCount 通常已经返回，但以防万一可以通过 rowCount 计算）
            if (rowCount != null && pageSize != null) {
                pageCount = (int) Math.ceil((double) rowCount / pageSize); // 计算总页数
            }

            // 如果只有一页数据，直接返回
            if (pageCount <= 1) {
                return;
            }

            // 从第二页开始请求剩余数据
            for (int i = 2; i <= pageCount; i++) { // 循环从第2页开始
                request.setPage(i); // 设置当前请求页码
                httpResponse = getFinanceTransaction(request); // 发起请求

                if (httpResponse.getResult() != null) {
                    insert(dmShopMapping, httpResponse.getResult().getOperations()); // 插入当前页数据
                } else {
                    // 如果返回为空，考虑异常情况，终止循环
                    break;
                }
            }
        }
    }

    private void insert(OzonShopMappingDO shopMappingDO, List<FinanceTransactionDTO> operations) {
        if (CollectionUtils.isNotEmpty(operations)) {
            // 预先查询所有的operationId，减少多次查询
            List<Long> operationIds = convertList(operations, FinanceTransactionDTO::getOperationId);

            List<OzonFinanceTransactionDO> ozonFinanceTransactionDOList = ozonFinanceTransactionService.batchOzonFinanceTransactionList(operationIds);
            Map<Long, OzonFinanceTransactionDO> existingTransactions = new HashMap<>();
            if (CollectionUtils.isNotEmpty(ozonFinanceTransactionDOList)) {
                existingTransactions.putAll(convertMap(ozonFinanceTransactionDOList, OzonFinanceTransactionDO::getOperationId));
            }

            List<OzonFinanceTransactionDO> transactionsToInsert = new ArrayList<>();
            List<OzonFinanceTransactionDO> transactionsToUpdate = new ArrayList<>();

            for (FinanceTransactionDTO operation : operations) {
                OzonFinanceTransactionDO dmFinanceTransaction = new OzonFinanceTransactionDO();
                dmFinanceTransaction.setClientId(shopMappingDO.getClientId());
                dmFinanceTransaction.setOperationId(operation.getOperationId());
                dmFinanceTransaction.setOperationType(operation.getOperationType());
                dmFinanceTransaction.setOperationDate(LocalDateTimeUtil.parseDate(operation.getOperationDate(), DatePattern.NORM_DATETIME_FORMATTER));
                dmFinanceTransaction.setOperationTypeName(operation.getOperationTypeName());
                dmFinanceTransaction.setDeliveryCharge(operation.getDeliveryCharge());
                dmFinanceTransaction.setReturnDeliveryCharge(operation.getReturnDeliveryCharge());
                dmFinanceTransaction.setAccrualsForSale(operation.getAccrualsForSale());
                dmFinanceTransaction.setSaleCommission(operation.getSaleCommission());
                dmFinanceTransaction.setAmount(operation.getAmount());
                dmFinanceTransaction.setType(operation.getType());
                dmFinanceTransaction.setPosting(JSON.toJSONString(operation.getPosting()));
                dmFinanceTransaction.setItems(JSON.toJSONString(operation.getItems()));
                dmFinanceTransaction.setServices(JSON.toJSONString(operation.getServices()));
                dmFinanceTransaction.setTenantId(shopMappingDO.getTenantId());

                if (operation.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "订单"))
                        || operation.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "退货和取消订单"))
                        || operation.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "其他"))
                        || operation.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "服务费"))
                        || operation.getType().equals(DictFrameworkUtils.parseDictDataValue("dm_finance_transaction_type", "退款"))) {
                    PostingsDTO posting = operation.getPosting();
                    if (posting != null) {
                        dmFinanceTransaction.setPostingNumber(posting.getPostingNumber());
                    }
                }

                // 根据 operationId 从缓存的 Map 中获取是否存在记录
                OzonFinanceTransactionDO existDO = existingTransactions.get(operation.getOperationId());
                OzonFinanceTransactionDO saveReqVO = BeanUtils.toBean(dmFinanceTransaction, OzonFinanceTransactionDO.class);
                if (existDO == null) {
                    // 不存在则新增
                    transactionsToInsert.add(saveReqVO);
                } else {
                    // 存在则更新
                    saveReqVO.setId(existDO.getId());
                    transactionsToUpdate.add(saveReqVO);
                }
            }

            // 批量插入
            if (!transactionsToInsert.isEmpty()) {
                ozonFinanceTransactionService.batchSaveTransactions(transactionsToInsert);
            }

            // 批量更新
            if (!transactionsToUpdate.isEmpty()) {
                ozonFinanceTransactionService.batchUpdateTransactions(transactionsToUpdate);
            }
        }
    }

    private OzonHttpResponse<FinanceTransactionResultDTO> getFinanceTransaction(FinanceTransactionRequest request) {
        TypeReference<OzonHttpResponse<FinanceTransactionResultDTO>> typeReference = new TypeReference<OzonHttpResponse<FinanceTransactionResultDTO>>() {
        };
        return ozonHttpUtil.post(OzonConfig.OZON_FINANCE_TRANSACTION_API, request, typeReference);
    }
}
