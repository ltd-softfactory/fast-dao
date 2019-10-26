package com.fast.db.template.template;

import com.fast.db.template.mapper.FastMapperUtil;

import java.util.Collection;
import java.util.Map;

/**
 * SQL条件操作
 *
 * @param <T> 操作的类泛型
 * @author 张亚伟 https://github.com/kaixinzyw/fast-db-template
 */
public class FastExample<T> {

    /**
     * 条件拼接工具
     */
    private Criteria<T> criteria;

    private FastExample() {
    }

    /**
     * 初始化创建
     *
     * @param pojoClass 操作类信息
     */
    public FastExample(Class<T> pojoClass) {
        criteria = new Criteria<>(pojoClass, this);
    }

    /**
     * 初始化创建
     *
     * @param pojoClass 操作的类
     * @param t         如果对象不为空,会将对象中参数不为null的字段作为AND条件
     */
    public FastExample(Class<T> pojoClass, T t) {
        criteria = new Criteria<>(pojoClass, this);
        if (t != null) {
            this.equalPojo(t);
        }
    }

    /**
     * 设置操作字段
     *
     * @param fieldName 字段名
     * @return 条件操作工具
     */
    public Criteria<T> field(String fieldName) {
        this.criteria.fieldName = fieldName;
        this.criteria.conditionPackages.setWay(FastCondition.Way.AND);
        return this.criteria;
    }

    /**
     * 获取条件封装
     *
     * @return 条件封装
     */
    public ConditionPackages conditionPackages() {
        return this.criteria.conditionPackages;
    }

    /**
     * 开始Dao操作
     *
     * @return Dao执行器
     */
    public FastDao<T> dao() {
        return criteria.dao();
    }


    /**
     * 对象查询
     *
     * @param t 对象在不为空的字段作为AND条件
     */
    public void equalPojo(T t) {
        if (t == null) {
            return;
        }
        criteria.conditionPackages.setEqualObject(t);
    }

    /**
     * 自定义SQL查询,使用AND进行拼接
     *
     * @param sql    自定义SQL语句,如果有参数需要使用#{参数名}进行占位
     * @param params 参数值MAP集合
     */
    public void andSql(String sql, Map<String, Object> params) {
        criteria.conditionPackages.setWay(FastCondition.Way.AND);
        criteria.conditionPackages.addSql(sql, params);
    }

    /**
     * 自定义SQL查询,使用OR进行拼接
     *
     * @param sql    自定义SQL语句,如果有参数需要使用#{参数名}进行占位
     * @param params 参数值MAP集合
     */
    public void orSql(String sql, Map<String, Object> params) {
        criteria.conditionPackages.setWay(FastCondition.Way.OR);
        criteria.conditionPackages.addSql(sql, params);
    }

    /**
     * 关闭逻辑删除保护,关闭后所有操作会影响到被逻辑删除标记的数据
     */
    public void closeLogicDeleteProtect() {
        criteria.conditionPackages.closeLogicDeleteProtect();
    }


    public static class Criteria<P> {

        /**
         * 操作的类信息
         */
        private Class<P> pojoClass;

        /**
         * SQL封装操作器
         */
        private FastExample<P> fastExample;

        /**
         * 操作的字段
         */
        private String fieldName;

        public Criteria(Class<P> pojoClass, FastExample<P> fastExample) {
            this.pojoClass = pojoClass;
            this.fastExample = fastExample;
        }

        /**
         * 条件封装类
         */
        public final ConditionPackages conditionPackages = new ConditionPackages();

        /**
         * 查询特定字段
         *
         * @return 条件操作工具
         */
        public Criteria<P> showField() {
            conditionPackages.addShowField(fieldName);
            return this;
        }

        /**
         * 屏蔽特定字段
         *
         * @return 条件操作工具
         */
        public Criteria<P> hideField() {
            conditionPackages.addHideField(fieldName);
            return this;
        }

        /**
         * 字段去重
         *
         * @return 条件操作工具
         */
        public Criteria<P> distinctField() {
            conditionPackages.addDistinctField(fieldName);
            return this;
        }

        /**
         * 对后续条件使用OR封装
         *
         * @return 条件操作工具
         */
        public Criteria<P> or() {
            this.conditionPackages.setWay(FastCondition.Way.OR);
            return this;
        }

        /**
         * 范围条件
         *
         * @param betweenMin 最小值
         * @param betweenMax 最大值
         * @return 条件操作工具
         */
        public Criteria<P> between(Object betweenMin, Object betweenMax) {
            if (betweenMin == null || betweenMax == null) {
                return this;
            }
            conditionPackages.addBetweenQuery(fieldName, betweenMin, betweenMax);
            return this;
        }

        /**
         * 排序-降序 查询时有用
         *
         * @return 条件操作工具
         */
        public Criteria<P> orderByDesc() {
            conditionPackages.addOrderByQuery(fieldName, true);
            return this;

        }

        /**
         * 排序-升序 查询时有用
         *
         * @return 条件操作工具
         */
        public Criteria<P> orderByAsc() {
            conditionPackages.addOrderByQuery(fieldName, false);
            return this;
        }

        /**
         * 包含条件
         *
         * @param inValues 所包含的值(a,b,c)
         * @return 条件操作工具
         */
        public Criteria<P> in(Object... inValues) {
            conditionPackages.addInQuery(fieldName, inValues);
            return this;
        }

        /**
         * 包含查询
         *
         * @param inValues 所包含的值([a,b,c])
         * @return 条件操作工具
         */
        public Criteria<P> in(Collection inValues) {
            conditionPackages.addInQuery(fieldName, inValues);
            return this;
        }

        /**
         * 值不为空条件
         *
         * @return 条件操作工具
         */
        public Criteria<P> notNull() {
            conditionPackages.addNotNullFieldsQuery(fieldName);
            return this;
        }

        /**
         * 值为空条件
         *
         * @return 条件操作工具
         */
        public Criteria<P> isNull() {
            conditionPackages.addNullFieldsQuery(fieldName);
            return this;
        }

        /**
         * @param value 值等于条件
         * @return 条件操作工具
         */
        public Criteria<P> valEqual(Object value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addEqualFieldQuery(fieldName, value);
            return this;
        }

        /**
         * 值大于等于条件
         *
         * @param value 值等于条件
         * @return 条件操作工具
         */
        public Criteria<P> greaterOrEqual(Object value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addGreaterOrEqualFieldsQuery(fieldName, value);
            return this;
        }

        /**
         * 值小于等于条件
         *
         * @param value 值等于条件
         * @return 条件操作工具
         */
        public Criteria<P> lessOrEqual(Object value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addLessOrEqualFieldsQuery(fieldName, value);
            return this;
        }


        /**
         * 值大于条件
         *
         * @param value 值等于条件
         * @return 条件操作工具
         */
        public Criteria<P> greater(Object value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addGreaterFieldsQuery(fieldName, value);
            return this;
        }

        /**
         * 值小于条件
         *
         * @param value 值等于条件
         * @return 条件操作工具
         */
        public Criteria<P> less(Object value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addLessFieldsQuery(fieldName, value);
            return this;
        }

        /**
         * 值模糊查询条件
         *
         * @param value 值
         * @return 条件操作工具
         */
        public Criteria<P> like(String value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addLikeQuery(fieldName, "%" + value + "%");
            return this;
        }

        /**
         * 值模糊查询条件
         *
         * @param value 值
         * @return 条件操作工具
         */
        public Criteria<P> likeLeft(String value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addLikeQuery(fieldName, "%" + value);
            return this;
        }

        /**
         * 值模糊查询条件
         *
         * @param value 值
         * @return 条件操作工具
         */
        public Criteria<P> likeRight(String value) {
            if (value == null) {
                return this;
            }
            conditionPackages.addLikeQuery(fieldName, value + "%");
            return this;
        }

        /**
         * 开始Dao操作
         *
         * @return Dao执行器
         */
        public FastDao<P> dao() {
            return FastMapperUtil.fastDao(pojoClass, fastExample);
        }

    }

}
