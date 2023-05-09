package com.dhl.fin.api.common.dto;

import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CuiJianbo on 2020.02.21.
 */
public class QueryBuilder {

    private QueryDto queryDto = new QueryDto();

    public QueryBuilder startIndex(int startIndex) {
        this.queryDto.setStartIndex(startIndex);
        return this;
    }

    public QueryBuilder length(int length) {
        this.queryDto.setLength(length);
        return this;
    }


    private String checkWhereCondition(String condition) {
        if (ObjectUtil.isNull(condition) || StringUtils.isEmpty(condition)) {
            return null;
        }
        if (!condition.trim().startsWith("(")) {
            boolean flag = Arrays.stream(condition.trim().split(" "))
                    .map(String::trim)
                    .filter(p -> p.equalsIgnoreCase("or"))
                    .findAny().isPresent();
            if (flag) {
                return String.format("(%s)", condition);
            }
        }
        return condition;
    }

    public QueryBuilder addWhere(String condition) {
        if (ObjectUtil.isNull(this.queryDto.getWhereCondition())) {
            this.queryDto.setWhereCondition(new LinkedList<>());
        }

        autoAddJoinDomain(condition);

        condition = checkWhereCondition(condition);
        if (StringUtil.isNotEmpty(condition)) {
            this.queryDto.getWhereCondition().add(condition);
        }
        return this;
    }

    public QueryBuilder addWhereWhen(String condition, Boolean test) {
        if (!test) {
            return this;
        }
        return addWhere(condition);
    }

    public QueryBuilder addAllWhere(List<String> conditions) {
        if (CollectorUtil.isEmpty(conditions)) {
            return this;
        }

        List<String> conditionList = new LinkedList<>();
        for (String c : conditions) {
            autoAddJoinDomain(c);
            conditionList.add(checkWhereCondition(c));
        }

        if (ObjectUtil.isNull(this.queryDto.getWhereCondition())) {
            this.queryDto.setWhereCondition(new LinkedList<>());
        }
        this.queryDto.getWhereCondition().addAll(conditionList);
        return this;
    }

    private void autoAddJoinDomain(String condition) {
        List<String> newJoinDomain = new LinkedList<>();
        Arrays.stream(condition.split(" "))
                .map(String::trim)
                .filter(item -> item.contains("."))
                .map(item -> item.split("\\.")[0])
                .filter(StringUtil::isNotEmpty)
                .map(String::trim)
                .distinct()
                .forEach(item -> {
                    String camelItem = StringUtil.toCamelCase(item);
                    if (!queryDto.getJoinDomain().contains(camelItem)) {
                        newJoinDomain.add(camelItem);
                    }
                });
        queryDto.getJoinDomain().addAll(newJoinDomain);
    }

    public QueryBuilder addOrder(String condition) {
        if (ObjectUtil.isNull(condition) || StringUtils.isEmpty(condition)) {
            return this;
        }
        if (ObjectUtil.isNull(this.queryDto.getOrderCondition())) {
            this.queryDto.setOrderCondition(new LinkedList<>());
        }
        this.queryDto.getOrderCondition().add(condition);
        return this;
    }

    public QueryBuilder addOrderWhen(String condition, Boolean test) {
        if (!test || ObjectUtil.isNull(condition) || StringUtils.isEmpty(condition)) {
            return this;
        }
        if (ObjectUtil.isNull(this.queryDto.getOrderCondition())) {
            this.queryDto.setOrderCondition(new LinkedList<>());
        }
        this.queryDto.getOrderCondition().add(condition);
        return this;
    }

    public QueryBuilder addAllOrder(List<String> conditions) {
        if (CollectorUtil.isEmpty(conditions)) {
            return this;
        }
        if (ObjectUtil.isNull(this.queryDto.getOrderCondition())) {
            this.queryDto.setOrderCondition(new LinkedList<>());
        }
        this.queryDto.getOrderCondition().addAll(conditions);
        return this;
    }

    public QueryBuilder addJoinDomain(String condition) {
        if (ObjectUtil.isNull(condition) || StringUtils.isEmpty(condition)) {
            return this;
        }
        if (ObjectUtil.isNull(this.queryDto.getJoinDomain())) {
            this.queryDto.setJoinDomain(new LinkedList());
        }
        if (!this.queryDto.getJoinDomain().contains(condition)) {
            this.queryDto.getJoinDomain().add(condition);
        }
        return this;
    }

    public QueryBuilder addJoinDomainWhen(String condition, Boolean test) {
        if (!test || ObjectUtil.isNull(condition) || StringUtils.isEmpty(condition)) {
            return this;
        }
        addJoinDomain(condition);
        return this;
    }

    public QueryBuilder addAllJoinDomain(List<String> conditions) {
        if (CollectorUtil.isEmpty(conditions)) {
            return this;
        }
        if (ObjectUtil.isNull(this.queryDto.getJoinDomain())) {
            this.queryDto.setJoinDomain(new LinkedList<>());
        }
        for (String condition : conditions) {
            addJoinDomain(condition);
        }
        return this;
    }

    public QueryBuilder available() {
        this.queryDto.setRemove(false);
        return this;
    }

    public QueryBuilder disable() {
        this.queryDto.setRemove(true);
        return this;
    }

    public QueryDto build() {
        return this.queryDto;
    }

}
