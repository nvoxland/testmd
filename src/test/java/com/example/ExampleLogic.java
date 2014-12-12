package com.example;

import testmd.util.StringUtils;

import java.util.Arrays;

public class ExampleLogic {

    public String insertData(String tableName, String[] columns, Object[] values) {
        String sql = "INSERT INTO "+tableName+" ("+ StringUtils.join(Arrays.asList(columns), ", ", false)+") VALUES (";

        int i=0;
        String[] valueSql = new String[values.length];
        for (Object obj : values) {
            if (obj == null) {
                valueSql[i++] = "NULL";
            } else if (obj instanceof Number) {
                valueSql[i++] = obj.toString();
            } else {
                valueSql[i++] = "'"+obj.toString()+"'";
            }
        }

        sql += StringUtils.join(Arrays.asList(valueSql), ", ", false)+");";

        return sql;
    }

    public String queryService(int version, String keywords) {
        return "/api/"+version+"/search.json?q="+(keywords.replace(" ", "+"));
    }
}
