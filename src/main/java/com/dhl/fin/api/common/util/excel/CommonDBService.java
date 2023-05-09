package com.dhl.fin.api.common.util.excel;

import java.util.List;
import java.util.Map;

/**
 * 用于从数据库分页加载数据到excel里
 *
 * @author becui
 * @date 4/11/2020
 */
public interface CommonDBService {

    /**
     * 列表查询
     *
     * @param sql
     * @return
     */
    List<Map<String, Object>> query(String sql);


    /**
     * 分页查询
     *
     * @param sql
     * @param order
     * @param pageIndex
     * @param pageSize
     * @return
     */
    List<Map<String, Object>> pageQuery(String sql, String order, int pageIndex, int pageSize);




    /**
     * 统计
     *
     * @param sql
     * @return
     */
    int count(String sql);


    /**
     * 处理每行数据
     *
     * @param row
     */
    void dealRow(List<Map<String, Object>> row);
}
