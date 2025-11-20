package cn.iocoder.yudao.module.dm.framework.datapermission;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import com.google.common.collect.Sets;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 门店数据权限规则
 * 
 * @Author: Jax
 * @Date: Created in 12:12 2025/11/19
 */
@Component
public class ShopDataPermissionRule implements DataPermissionRule {

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionCommonApi permissionCommonApi;

    @Override
    public Set<String> getTableNames() {
        return Sets.newHashSet("dm_ozon_shop_mapping");
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }

        // 获取用户信息
        AdminUserRespDTO user = adminUserApi.getUser(userId).getCheckedData();
        // 超级管理员，则不进行数据权限控制
        if (permissionCommonApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode()).getCheckedData()) {
            return null;
        }

        // 如果授权门店列表为空，表示可以访问所有门店
        if (CollectionUtils.isEmpty(user.getShopIds())) {
            return null; // 无限制，可以访问所有门店
        }

        // 构建条件：creator = userId OR id IN (授权门店ID)
        List<Expression> conditions = new ArrayList<>();

        // 添加创建者条件
        conditions.add(new EqualsTo(
                MyBatisUtils.buildColumn(tableName, tableAlias, "creator"),
                new StringValue(userId.toString())
        ));

        // 添加授权门店条件
        Set<Long> authorizedShopIds = user.getShopIds();
        if (!authorizedShopIds.isEmpty()) {
            ExpressionList<LongValue> idList = new ExpressionList<>();
            authorizedShopIds.forEach(id -> idList.getExpressions().add(new LongValue(id)));
            conditions.add(new InExpression(
                    MyBatisUtils.buildColumn(tableName, tableAlias, "id"),
                    // Parenthesis 的目的，是提供 (1,2,3) 的 () 左右括号
                    new Parenthesis(idList)
            ));
        }

        // 合并所有条件
        Expression result = conditions.get(0);
        for (int i = 1; i < conditions.size(); i++) {
            result = new OrExpression(result, conditions.get(i));
        }

        return result;
    }
}
