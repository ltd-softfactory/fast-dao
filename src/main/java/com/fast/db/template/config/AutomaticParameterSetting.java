package com.fast.db.template.config;

import cn.hutool.core.util.StrUtil;
import com.fast.db.template.dao.DaoActuator;

import java.util.concurrent.TimeUnit;

/**
 * 框架配置
 *
 * @author 张亚伟 https://github.com/kaixinzyw/fast-db-template
 */
public class AutomaticParameterSetting {

    /**
     * 设置主要信息
     *
     * @param mapperImpl      使用的ORM实现 ,框架自身可选值
     *                        1:SpringJDBCMySqlImpl.class:使用SpringJDBC实现
     *                        2:FastMyBatisImpl.class: 使用MyBatis实现
     * @param primaryKeyField 框架操作的主键字段名称
     * @param keyType         框架操作的主键类型
     */
    public AutomaticParameterSetting(Class<? extends DaoActuator> mapperImpl, String primaryKeyField, PrimaryKeyType keyType) {
        openAutoSetPrimaryKey(primaryKeyField, keyType);
        AutomaticParameterAttributes.setDBActuator(mapperImpl);
    }


    /**
     * 默认设置
     * 开启字段下划线转换 例 user_name = userName
     * 开启数据自动创建时间设置,字段名为createTime
     * 开启数据自动更新设置,updateTime
     * 开启数据逻辑删除,字段名为deleted,删除标记值为true
     */
    public void defaultSetting() {
        openToCamelCase();
        openAutoSetCreateTime("createTime");
        openAutoSetUpdateTime("updateTime");
        openLogicDelete("deleted", Boolean.TRUE);
    }

    /**
     * 开启SQL日志打印,默认使用项目的日志框架
     * @param isPrint 是否打印SQL语句
     * @param isPrintResult 是否打印SQL执行结果
     */
    public void openSqlPrint(Boolean isPrint, Boolean isPrintResult) {
        AutomaticParameterAttributes.isSqlPrint = isPrint;
        AutomaticParameterAttributes.isSqlPrintResult = isPrintResult;
    }

    /**
     * 开启自动对数据 新增操作 进行创建时间设置
     * @param createTimeField 需要设置的字段名
     */
    public void openAutoSetCreateTime(String createTimeField) {
        AutomaticParameterAttributes.isAutoSetCreateTime = Boolean.TRUE;
        AutomaticParameterAttributes.createTimeField = createTimeField;
        AutomaticParameterAttributes.createTimeTableField = getToUnderlineCase(createTimeField);
    }

    /**
     * 开启自动对数据 更新操作|逻辑删除操作 进行更新时间设置
     * @param updateTimeField 需要设置的字段名
     */
    public void openAutoSetUpdateTime(String updateTimeField) {
        AutomaticParameterAttributes.isAutoSetUpdateTime = Boolean.TRUE;
        AutomaticParameterAttributes.updateTimeField = updateTimeField;
        AutomaticParameterAttributes.updateTimeTableField = getToUnderlineCase(updateTimeField);
    }

    /**
     * 开启 设置模板操作的主键信息,默契在创建框架配置时候强制开启,暂无需单独设置
     * @param primaryKeyField 主键字段名
     * @param keyType 主键类型
     */
    public void openAutoSetPrimaryKey(String primaryKeyField, PrimaryKeyType keyType) {
        AutomaticParameterAttributes.isAutoSetPrimaryKey = Boolean.TRUE;
        AutomaticParameterAttributes.primaryKeyField = primaryKeyField;
        AutomaticParameterAttributes.primaryKeyType = keyType;
        AutomaticParameterAttributes.primaryKeyTableField = getToUnderlineCase(primaryKeyField);
    }

    /**
     * 开启逻辑删除功能,开启后会对逻辑删除标记的数据在 更新|删除|查询 时进行保护,可通过模板进行单次操作逻辑删除保护的关闭
     * @param deleteField 逻辑删除字段名
     * @param defaultDeleteValue 逻辑删除默认值
     */
    public void openLogicDelete(String deleteField, Boolean defaultDeleteValue) {
        AutomaticParameterAttributes.isOpenLogicDelete = Boolean.TRUE;
        AutomaticParameterAttributes.deleteField = deleteField;
        AutomaticParameterAttributes.deleteTableField = getToUnderlineCase(deleteField);
        AutomaticParameterAttributes.defaultDeleteValue = defaultDeleteValue;
    }

    /**
     * 开启框架缓存功能,开启后可使用@FastRedisCache,@FastRedisLocalCache,@FastStatisCache 三种数据缓存类型
     * @param defaultCacheTime 默认全局缓存时间
     * @param defaultCacheTimeType 默认全局缓存时间类型
     */
    public void openCache(Long defaultCacheTime, TimeUnit defaultCacheTimeType) {
        AutomaticParameterAttributes.isOpenCache = Boolean.TRUE;
        AutomaticParameterAttributes.defaultCacheTime = defaultCacheTime;
        AutomaticParameterAttributes.defaultCacheTimeType = defaultCacheTimeType;
    }

    /**
     * 开启字段驼峰转换 例 user_name = userName
     */
    public void openToCamelCase() {
        AutomaticParameterAttributes.isToCamelCase = Boolean.TRUE;
        AutomaticParameterAttributes.primaryKeyTableField = getToUnderlineCase(AutomaticParameterAttributes.primaryKeyTableField);
        AutomaticParameterAttributes.createTimeTableField = getToUnderlineCase(AutomaticParameterAttributes.createTimeTableField);
        AutomaticParameterAttributes.updateTimeTableField = getToUnderlineCase(AutomaticParameterAttributes.updateTimeTableField);
        AutomaticParameterAttributes.deleteTableField = getToUnderlineCase(AutomaticParameterAttributes.deleteTableField);
    }

    /**
     * 驼峰转换
     * @param val 需要转换的值
     * @return 转换后的值
     */
    private String getToUnderlineCase(String val) {
        if (val == null) {
            return val;
        }
        if (AutomaticParameterAttributes.isToCamelCase) {
            val = StrUtil.toUnderlineCase(val);
        }
        return val;
    }


}