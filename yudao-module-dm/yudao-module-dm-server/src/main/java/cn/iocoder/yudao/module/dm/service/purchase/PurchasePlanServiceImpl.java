package cn.iocoder.yudao.module.dm.service.purchase;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchaseorder.DmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.dm.dal.redis.dao.DmNoRedisDAO;
import cn.iocoder.yudao.module.dm.enums.PurchaseOrderStatusEnum;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.purchase.PurchasePlanMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchase.PurchasePlanItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 采购计划 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class PurchasePlanServiceImpl implements PurchasePlanService {

    public static final String PROCESS_KEY = "bpm_purchase_plan";
    @Resource
    private PurchasePlanMapper purchasePlanMapper;
    @Resource
    private PurchasePlanItemMapper purchasePlanItemMapper;
    @Resource
    private DmNoRedisDAO dmNoRedisDAO;
    @Resource
    private BpmProcessInstanceApi bpmProcessInstanceApi;
    @Resource
    private ProductInfoMapper productInfoMapper;
    @Resource
    private ConfigApi configApi;
    @Resource
    private DmPurchaseOrderItemMapper purchaseOrderItemMapper;
    @Resource
    private ProductInfoService productInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchasePlan(PurchasePlanSaveReqVO createReqVO) {
        // 插入
        PurchasePlanDO purchasePlan = BeanUtils.toBean(createReqVO, PurchasePlanDO.class);
        purchasePlan.setBatchNumber(dmNoRedisDAO.generate(DmNoRedisDAO.PURCHASE_PLAN_GROUP_NO_PREFIX));
        purchasePlan.setTotalCount(createReqVO.getPurchasePlanItems().size());
        purchasePlan.setDeptId(SecurityFrameworkUtils.getLoginUserDeptId());
        purchasePlanMapper.insert(purchasePlan);

        // 插入子表
        createPurchasePlanItemList(purchasePlan.getId(), createReqVO.getPurchasePlanItems());
        // 返回
        return purchasePlan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importPurchasePlan(List<PurchasePlanImportVO> purchasePlanImportVOList) {

        validateImportVO(purchasePlanImportVOList);

        // 插入
        PurchasePlanDO purchasePlan = new PurchasePlanDO();
        purchasePlan.setBatchNumber(dmNoRedisDAO.generate(DmNoRedisDAO.PURCHASE_PLAN_GROUP_NO_PREFIX));
        purchasePlan.setTotalCount(purchasePlanImportVOList.size());
        purchasePlanMapper.insert(purchasePlan);

        List<PurchasePlanItemDO> purchasePlanItemDOList = buildItemByImportExcelVO(purchasePlanImportVOList);
        createPurchasePlanItemList(purchasePlan.getId(), purchasePlanItemDOList);

        return purchasePlan.getId();
    }

    private void validateImportVO(List<PurchasePlanImportVO> purchasePlanImportVOList) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(purchasePlanImportVOList)) {
            throw exception(PURCHASE_PLAN_IMPORT_EMPTY_ERROR);
        }

        for (int i = 0; i < purchasePlanImportVOList.size(); i++) {
            PurchasePlanImportVO vo = purchasePlanImportVOList.get(i);
            String skuId = vo.getSkuId();
            String quantity = vo.getQuantity();

            if (StringUtils.isBlank(skuId)) {
                throw exception(PURCHASE_PLAN_IMPORT_ERROR, String.format("第%d行Sku为空", i + 1));
            }
            if (StringUtils.isBlank(quantity)) {
                throw exception(PURCHASE_PLAN_IMPORT_ERROR, String.format("第%d行采购数量为空", i + 1));
            }

            if (StringUtils.isNotBlank(skuId) && "必填".equals(skuId)) {
                throw exception(PURCHASE_PLAN_IMPORT_ERROR, String.format("第%d行样例数据未删除", i + 1));
            }

            ProductInfoDO productInfoDO = productInfoMapper.selectOne(ProductInfoDO::getSkuId, skuId);
            if (null == productInfoDO) {
                throw exception(PURCHASE_PLAN_IMPORT_ERROR, String.format("第%d行产品不存在", i + 1));
            }

            List<ProductPurchaseDO> productPurchaseDOList = productInfoService.getProductPurchaseListByProductId(productInfoDO.getId());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(productPurchaseDOList)) {
                throw exception(PURCHASE_PLAN_IMPORT_ERROR, String.format("第%d行产品没有采购信息", i + 1));
            }
        }

    }


    private List<PurchasePlanItemDO> buildItemByImportExcelVO(List<PurchasePlanImportVO> purchasePlanImportVOList) {
        List<PurchasePlanItemDO> purchasePlanItemDOList = new ArrayList<>();

        List<String> skuIds = convertList(purchasePlanImportVOList, PurchasePlanImportVO::getSkuId);
        Map<String, Long> productIdMapping = productInfoService.batchQueryProductIdsBySkuIds(skuIds);
        Set<Long> productIds = new HashSet<>(productIdMapping.values());
        Map<Long, ProductPurchaseDO> productPurchaseDOMap = productInfoService.batchProductPurchaseListByProductIds(ArrayUtils.toArray(productIds));

        for (PurchasePlanImportVO vo : purchasePlanImportVOList) {
            Long productId = productIdMapping.get(vo.getSkuId());
            ProductPurchaseDO productPurchaseDO = productPurchaseDOMap.get(productId);

            PurchasePlanItemDO itemDO = new PurchasePlanItemDO();
            itemDO.setProductId(productId);
            itemDO.setSkuId(vo.getSkuId());
            itemDO.setQuantity(Integer.parseInt(vo.getQuantity()));
            LocalDateTime localDateTime = DateUtil.parseLocalDateTime(vo.getArrivedDate());
            itemDO.setExpectedArrivalDate(localDateTime);
            itemDO.setPcs(productPurchaseDO.getQuantityPerBox());
            itemDO.setNumberOfBox((int) Math.ceil((double) itemDO.getQuantity() / productPurchaseDO.getQuantityPerBox()));

            purchasePlanItemDOList.add(itemDO);
        }

        return purchasePlanItemDOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchasePlan(PurchasePlanSaveReqVO updateReqVO) {
        // 校验存在
        validatePurchasePlanExists(updateReqVO.getId());
        // 更新
        PurchasePlanDO updateObj = BeanUtils.toBean(updateReqVO, PurchasePlanDO.class);
        updateObj.setTotalCount(updateReqVO.getPurchasePlanItems().size());
        // 重置审核状态
        updateObj.setAuditStatus(0);
        purchasePlanMapper.updateById(updateObj);

        // 更新子表
        updatePurchasePlanItemList(updateReqVO.getId(), updateReqVO.getPurchasePlanItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchasePlan(Long id) {
        // 校验存在
        validatePurchasePlanExists(id);
        // 删除
        purchasePlanMapper.deleteById(id);

        // 删除子表
        deletePurchasePlanItemByPlanId(id);
    }

    private void validatePurchasePlanExists(Long id) {
        if (purchasePlanMapper.selectById(id) == null) {
            throw exception(PURCHASE_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public PurchasePlanDO getPurchasePlan(Long id) {
        return purchasePlanMapper.selectById(id);
    }

    @Override
    public List<PurchasePlanDO> batchQueryPurchasePlan(Collection<Long> ids) {
        return purchasePlanMapper.selectList(PurchasePlanDO::getId, ids);
    }

    @Override
    public List<PurchasePlanDO> batchQueryPurchasePlanByNumber(Collection<String> batchNumbers) {
        return purchasePlanMapper.selectList(PurchasePlanDO::getBatchNumber, batchNumbers);
    }

    @Override
    public PageResult<PurchasePlanDO> getPurchasePlanPage(PurchasePlanPageReqVO pageReqVO) {
        return purchasePlanMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（采购计划详情） ====================


    @Override
    public PurchasePlanItemDO getPurchasePlanItem(String planNumber) {
        return purchasePlanItemMapper.selectOne(PurchasePlanItemDO::getPlanNumber, planNumber);
    }

    @Override
    public List<PurchasePlanItemDO> getPurchasePlanItemListByPlanId(Long planId) {
        return purchasePlanItemMapper.selectListByPlanId(planId);
    }

    @Override
    public List<PurchasePlanItemDO> batchPurchasePlanItemListByPlanIds(Collection<Long> planIds) {
        return purchasePlanItemMapper.selectList(PurchasePlanItemDO::getPlanId, planIds);
    }

    @Override
    public List<PurchasePlanItemDO> batchPurchasePlanItemListByPlanNumbers(Collection<String> planNumbers) {
        return purchasePlanItemMapper.selectList(PurchasePlanItemDO::getPlanNumber, planNumbers);
    }

    @Override
    public void updatePurchasePlanAuditStatus(Long planId, Integer status) {
        purchasePlanMapper.update(new PurchasePlanDO().setAuditStatus(status),
                new LambdaQueryWrapper<PurchasePlanDO>()
                        .eq(PurchasePlanDO::getId, planId));
        purchasePlanItemMapper.update(new PurchasePlanItemDO().setAuditStatus(status),
                new LambdaUpdateWrapper<PurchasePlanItemDO>()
                        .eq(PurchasePlanItemDO::getPlanId, planId));
    }

    @Override
    public void submitReview(Long planId) {
        // 发起 BPM 流程
        Map<String, Object> processInstanceExtras = new HashMap<>();
        processInstanceExtras.put("planId", planId);

        String configByKey = configApi.getConfigValueByKey("dm_bpm_purchase_plan_flow").getCheckedData();
        String processKey = StringUtils.isBlank(configByKey) ? PROCESS_KEY : configByKey;

        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        String processInstanceId = bpmProcessInstanceApi.createProcessInstance(loginUserId, new BpmProcessInstanceCreateReqDTO()
                .setVariables(processInstanceExtras)
                .setProcessDefinitionKey(processKey).setBusinessKey(String.valueOf(planId))).getCheckedData();

        purchasePlanMapper.update(new PurchasePlanDO().setProcessInstanceId(processInstanceId)
                        .setAuditStatus(BpmTaskStatusEnum.RUNNING.getStatus()),
                new LambdaQueryWrapper<PurchasePlanDO>()
                        .eq(PurchasePlanDO::getId, planId));
    }

    @Override
    public void updatePurchasePlanItemStatus(String planNumber, Integer status) {
        // check 采购单明细是否有未作废的，如果有，则不允许作废
        PurchaseOrderItemDO purchaseOrderItemDO = purchaseOrderItemMapper.selectOne(new LambdaQueryWrapperX<PurchaseOrderItemDO>()
                .eq(PurchaseOrderItemDO::getPlanNumber, planNumber)
                .ne(PurchaseOrderItemDO::getStatus, PurchaseOrderStatusEnum.DO_DELETED.getStatus()));
        if (purchaseOrderItemDO != null) {
            throw exception(PURCHASE_PLAN_ITEM_NOT_ALLOW_CANCEL);
        }

        purchasePlanItemMapper.update(new PurchasePlanItemDO().setStatus(status),
                new LambdaUpdateWrapper<PurchasePlanItemDO>()
                        .eq(PurchasePlanItemDO::getPlanNumber, planNumber));
    }

    private void createPurchasePlanItemList(Long planId, List<PurchasePlanItemDO> list) {

        list.forEach(o -> {
            o.setPlanId(planId);
            o.setPlanNumber(dmNoRedisDAO.generate(DmNoRedisDAO.PURCHASE_PLAN_NO_PREFIX));
        });
        purchasePlanItemMapper.insertBatch(list);
    }

    private void updatePurchasePlanItemList(Long planId, List<PurchasePlanItemDO> list) {
        deletePurchasePlanItemByPlanId(planId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createPurchasePlanItemList(planId, list);
    }

    private void deletePurchasePlanItemByPlanId(Long planId) {
        purchasePlanItemMapper.deleteByPlanId(planId);
    }

}