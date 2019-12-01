package com.fast.fast;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.collection.CollUtil;
import com.fast.aspect.DaoActuatorAspect;
import com.fast.cache.DataCache;
import com.fast.condition.FastBase;
import com.fast.config.FastDaoAttributes;
import com.fast.dao.DaoActuator;
import com.fast.mapper.TableMapper;
import com.fast.mapper.TableMapperUtil;
import com.fast.utils.FastValueUtil;
import com.fast.utils.page.PageInfo;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ORM执行器
 *
 * @author 张亚伟 https://github.com/kaixinzyw
 */
public class DaoTemplate<T> {

    private static final FastThreadLocal<DaoTemplate> daoTemplateThreadLocal = new FastThreadLocal<>();

    /**
     * 条件封装
     */
    private FastBase<T> fastBase;
    /**
     * ORM实现
     */
    private DaoActuator<T> daoActuator;
    /**
     * 操作对象映射
     */
    private TableMapper<T> tableMapper;

    private DaoTemplate() {
    }

    /**
     * 初始化
     *
     * @param <T>         操作对象的泛型信息
     * @param fastBase 条件封装
     * @param clazz       执行类
     * @return ORM执行器
     */
    public static <T> DaoTemplate<T> init(Class<T> clazz, FastBase<T> fastBase) {
        DaoTemplate<T> template = daoTemplateThreadLocal.get();
        if (template == null) {
            template = new DaoTemplate<>();
            template.daoActuator = ProxyUtil.proxy(FastDaoAttributes.<T>getDaoActuator(), DaoActuatorAspect.class);
            daoTemplateThreadLocal.set(template);
        }
        template.fastBase = fastBase;
        template.tableMapper = TableMapperUtil.getTableMappers(clazz);
        FastDaoParam.init(template.tableMapper, template.fastBase);
        return template;
    }


    /**
     * 新增操作
     *
     * @param pojo 需要新增的数据
     * @return 新增结果
     */
    public T insert(T pojo) {
        if (pojo != null) {
            FastValueUtil.setPrimaryKey(pojo, tableMapper);
            FastValueUtil.setCreateTime(pojo);
            FastValueUtil.setNoDelete(pojo);
            FastDaoParam.<T>get().setInsertList(Collections.singletonList(pojo));
        }
        List<T> ts = daoActuator.insert();
        DataCache.upCache(tableMapper);
        if (CollUtil.isEmpty(ts)) {
            return null;
        }
        return ts.get(0);
    }

    /**
     * 新增操作
     *
     * @param ins 需要新增的数据
     * @return 新增结果
     */
    public List<T> insertList(List<T> ins) {
        if (CollUtil.isNotEmpty(ins)) {
            for (T bean : ins) {
                FastValueUtil.setPrimaryKey(bean, tableMapper);
                FastValueUtil.setCreateTime(bean);
                FastValueUtil.setNoDelete(bean);
            }
            FastDaoParam.<T>get().setInsertList(ins);
        }
        List<T> insertList = daoActuator.insert();
        DataCache.upCache(tableMapper);
        return insertList;
    }

    /**
     * 新增操作
     *
     * @param ins  需要新增的数据
     * @param size 每次插入条数
     * @return 新增结果
     */
    public List<T> insertList(List<T> ins, Integer size) {
        if (CollUtil.isEmpty(ins)) {
            return ins;
        }
        for (T bean : ins) {
            FastValueUtil.setPrimaryKey(bean, tableMapper);
            FastValueUtil.setCreateTime(bean);
            FastValueUtil.setNoDelete(bean);
        }
        List<List<T>> inSplit = CollUtil.split(ins, size);
        FastDaoParam<T> daoParam = FastDaoParam.<T>get();
        for (List<T> ts : inSplit) {
            FastDaoParam.<T>get().setInsertList(ts);
            daoActuator.insert();
            FastDaoParam.init(tableMapper, fastBase);
        }
        DataCache.upCache(tableMapper);
        return ins;
    }


    /**
     * 单条数据查询
     *
     * @return 查询结果
     */
    public T findOne() {
        this.fastBase.conditionPackages().setLimit(1);
        List<T> pojos = findAll();
        if (CollUtil.isNotEmpty(pojos)) {
            return pojos.get(0);
        }
        return null;
    }

    /**
     * 列表数据查询
     *
     * @return 查询结果
     */
    public List<T> findAll() {
        if (FastDaoAttributes.isOpenCache && tableMapper.getCacheType() != null) {
            List<T> list = DataCache.<T>init(tableMapper, fastBase).getList();
            if (list != null) {
                return list;
            }
        }
        List<T> query = daoActuator.select();
        if (FastDaoAttributes.isOpenCache && tableMapper.getCacheType() != null && query != null) {
            DataCache.<T>init(tableMapper, fastBase).setList(query);
        }
        return query;
    }

    /**
     * 数据条数查询
     *
     * @return 查询结果
     */
    public Integer findCount() {
        if (FastDaoAttributes.isOpenCache && tableMapper.getCacheType() != null) {
            Integer one = DataCache.<Integer>init(tableMapper, fastBase).getCount();
            if (one != null) {
                return one;
            }
        }
        Integer count = daoActuator.count();
        if (FastDaoAttributes.isOpenCache && tableMapper.getCacheType() != null && count != null) {
            DataCache.<Integer>init(tableMapper, fastBase).setCount(count);
        }
        return count;
    }

    /**
     * 分页查询
     *
     * @param pageNum       页数
     * @param pageSize      每页条数
     * @param navigatePages 显示页数
     * @return 查询结果
     */
    public PageInfo<T> findPage(int pageNum, int pageSize, int navigatePages) {
        Integer count = findCount();
        if (count == null || count < 1) {
            return new PageInfo<>(0, pageNum, pageSize, new ArrayList<T>(), navigatePages);
        }
        if (pageNum == 1) {
            this.fastBase.conditionPackages().setPage(0);
        } else {
            this.fastBase.conditionPackages().setPage((pageNum - 1) * pageSize);
        }
        this.fastBase.conditionPackages().setSize(pageSize);
        FastDaoParam.init(tableMapper, fastBase);
        List<T> list = findAll();
        if (list == null) {
            list = new ArrayList<>();
        }
        return new PageInfo<>(count, pageNum, pageSize, list, navigatePages);
    }

    /**
     * 数据更新
     *
     * @param pojo        需要更新的数据
     * @param isSelective 是否对null值属性不操作
     * @return 更新条数
     */
    public Integer update(T pojo, boolean isSelective) {
        if (pojo != null) {
            FastValueUtil.setUpdateTime(pojo);
        }
        FastDaoParam<T> fastDaoParam = FastDaoParam.get();
        fastDaoParam.setUpdate(pojo);
        fastDaoParam.setUpdateSelective(isSelective);
        return DataCache.upCache(daoActuator.update(), tableMapper);
    }


    /**
     * 删除数据
     *
     * @return 删除条数
     */
    public Integer delete() {
        if (!FastDaoAttributes.isOpenLogicDelete || !fastBase.conditionPackages().getLogicDeleteProtect() || fastBase.conditionPackages().getCustomSql() != null) {
            return DataCache.upCache(daoActuator.delete(), tableMapper);
        }

        try {
            FastDaoParam.get().setLogicDelete(true);
            T pojo = tableMapper.getObjClass().newInstance();
            FastValueUtil.setDeleted(pojo);
            return update(pojo, Boolean.TRUE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
