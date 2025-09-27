package cn.iocoder.yudao.module.dm.service.profitreport;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.profitreport.ProfitReportMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 财务账单报告 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ProfitReportServiceImpl implements ProfitReportService {

    @Resource
    private ProfitReportMapper profitReportMapper;

    @Override
    public Integer createProfitReport(ProfitReportSaveReqVO createReqVO) {
        // 插入
        ProfitReportDO profitReport = BeanUtils.toBean(createReqVO, ProfitReportDO.class);
        profitReportMapper.insert(profitReport);
        // 返回
        return profitReport.getId();
    }

    @Override
    public Boolean batchCreateProfitReport(List<ProfitReportSaveReqVO> createReqVOList) {
        if (CollectionUtils.isEmpty(createReqVOList)) {
            return Boolean.FALSE;
        }

        // 提取创建请求中的日期、产品ID、客户ID
        LocalDate date = createReqVOList.get(0).getFinanceDate();
        List<String> platformSkuIds = convertList(createReqVOList, ProfitReportSaveReqVO::getPlatformSkuId);
        List<String> clientIds = convertList(createReqVOList, ProfitReportSaveReqVO::getClientId);

        // 将请求转换为数据对象
        List<ProfitReportDO> reportList = BeanUtils.toBean(createReqVOList, ProfitReportDO.class);

        // 构造查询条件，根据日期、产品ID、客户ID进行查询
        LambdaQueryWrapperX<ProfitReportDO> queryWrapperX = new LambdaQueryWrapperX<ProfitReportDO>()
                .eqIfPresent(ProfitReportDO::getFinanceDate, date)
                .inIfPresent(ProfitReportDO::getClientId, clientIds)
                .inIfPresent(ProfitReportDO::getPlatformSkuId, platformSkuIds);

        // 查找数据库中已有的记录
        List<ProfitReportDO> existList = profitReportMapper.selectList(queryWrapperX);

        // 创建两个集合：一个用于存储要插入的记录，一个用于存储要更新的记录
        List<ProfitReportDO> insertList = new ArrayList<>();
        List<ProfitReportDO> updateList = new ArrayList<>();

        // 通过循环比较现有数据和新数据，决定是插入还是更新
        for (ProfitReportDO report : reportList) {
            boolean exists = false;
            for (ProfitReportDO existReport : existList) {
                // 如果日期、产品ID、客户ID均匹配，认为记录已存在
                if (report.getFinanceDate().equals(existReport.getFinanceDate())
                        && report.getClientId().equals(existReport.getClientId())
                        && report.getPlatformSkuId().equals(existReport.getPlatformSkuId())) {
                    // 设置ID，以便更新操作
                    report.setId(existReport.getId());
                    updateList.add(report);
                    exists = true;
                    break;
                }
            }
            // 如果记录不存在，则添加到插入列表中
            if (!exists) {
                insertList.add(report);
            }
        }

        // 批量插入
        if (!CollectionUtils.isEmpty(insertList)) {
            profitReportMapper.insertBatch(insertList);
        }

        // 批量更新
        if (!CollectionUtils.isEmpty(updateList)) {
            for (ProfitReportDO updateReport : updateList) {
                profitReportMapper.updateById(updateReport);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public void updateProfitReport(ProfitReportSaveReqVO updateReqVO) {
        // 校验存在
//        validateProfitReportExists(updateReqVO.getId());
        // 更新
        ProfitReportDO updateObj = BeanUtils.toBean(updateReqVO, ProfitReportDO.class);
        profitReportMapper.updateById(updateObj);
    }

    @Override
    public void deleteProfitReport(Integer id) {
        // 校验存在
//        validateProfitReportExists(id);
        // 删除
        profitReportMapper.deleteById(id);
    }

//    private void validateProfitReportExists(Integer id) {
//        if (profitReportMapper.selectById(id) == null) {
//            throw exception(PROFIT_REPORT_NOT_EXISTS);
//        }
//    }

    @Override
    public ProfitReportDO getProfitReport(Integer id) {
        return profitReportMapper.selectById(id);
    }

    @Override
    public PageResult<ProfitReportDO> getProfitReportPage(ProfitReportPageReqVO pageReqVO) {
        return profitReportMapper.selectPage(pageReqVO);
    }

    @Override
    public IPage<ProfitReportRespVO> getClientProfitReportPage(IPage<ProfitReportRespVO> page, ProfitReportPageReqVO pageReqVO) {
        return profitReportMapper.selectClientPage(page, pageReqVO);
    }

    @Override
    public IPage<ProfitReportRespVO> getSkuProfitReportPage(IPage<ProfitReportRespVO> page, ProfitReportPageReqVO pageReqVO) {
        return profitReportMapper.selectSkuPage(page, pageReqVO);
    }

    @Override
    public void deleteProfitReport(String[] clientIds, LocalDate[] financeDate) {
        profitReportMapper.deleteProfitReport(clientIds,financeDate);
    }
}