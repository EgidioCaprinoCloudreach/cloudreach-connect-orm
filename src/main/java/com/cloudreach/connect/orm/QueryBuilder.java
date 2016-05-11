package com.cloudreach.connect.orm;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class QueryBuilder<T> {

    private final String schema;
    private final String table;

    private final String idColumn;
    private final String idColumnWithoutUnderscores;

    private final List<String> columns = new LinkedList<>();
    private final List<String> columnsWithoutUnderscores = new LinkedList<>();

    private final Map<String, String> columnCastMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Method> methodMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public QueryBuilder(String schema, String table, List<String> columns, Class<T> type) {
        this.schema = schema;
        this.table = table;
        this.columns.addAll(columns);

        idColumn = "id_" + table;
        idColumnWithoutUnderscores = idColumn.replace("_", "");
        this.columns.remove(idColumn);

        for (String column : this.columns) {
            columnsWithoutUnderscores.add(column.replace("_", ""));
        }

        for (Method method : type.getMethods()) {
            String methodName = method.getName();
            if (!methodMap.containsKey(methodName)) {
                methodMap.put(methodName, method);
            }
        }
    }

    public void addColumnCast(String column, String castType) {
        columnCastMap.put(column, castType);
    }

    public void addColumnCast(Map<String, String> columnCastMap) {
        this.columnCastMap.putAll(columnCastMap);
    }

    public String buildInsertQuery(T record, List params) throws Exception {
        StringBuilder query = new StringBuilder("INSERT INTO ").append(schema).append(".").append(table)
                .append(" (").append('"').append(columns.get(0)).append('"');
        for (int i = 1; i < columns.size(); ++i) {
            query.append(", ").append('"').append(columns.get(i)).append('"');
        }
        query.append(") VALUES (?");
        if (columnCastMap.containsKey(columns.get(0))) {
            query.append("::").append(columnCastMap.get(columns.get(0)));
        }
        params.add(methodMap.get("get" + columnsWithoutUnderscores.get(0)).invoke(record));
        for (int i = 1; i < columns.size(); ++i) {
            query.append(", ?");
            if (columnCastMap.containsKey(columns.get(i))) {
                query.append("::").append(columnCastMap.get(columns.get(i)));
            }
            params.add(methodMap.get("get" + columnsWithoutUnderscores.get(i)).invoke(record));
        }
        query.append(")");
        return query.toString();
    }

    public String buildUpdateQuery(T record, List params) throws Exception {
        StringBuilder query = new StringBuilder("UPDATE ").append(schema).append(".").append(table).append(" SET ")
                .append('"').append(columns.get(0)).append('"').append(" = ?");
        if (columnCastMap.containsKey(columns.get(0))) {
            query.append("::").append(columnCastMap.get(columns.get(0)));
        }
        params.add(methodMap.get("get" + columnsWithoutUnderscores.get(0)).invoke(record));
        for (int i = 1; i < columns.size(); ++i) {
            query.append(", ").append('"').append(columns.get(i)).append('"').append(" = ?");
            if (columnCastMap.containsKey(columns.get(i))) {
                query.append("::").append(columnCastMap.get(columns.get(i)));
            }
            params.add(methodMap.get("get" + columnsWithoutUnderscores.get(i)).invoke(record));
        }
        query.append(" WHERE ").append(idColumn).append(" = ?");
        params.add(methodMap.get("get" + idColumnWithoutUnderscores).invoke(record));
        return query.toString();
    }

    public String buildDeleteQuery(T record, List params) throws Exception {
        StringBuilder query = new StringBuilder("DELETE FROM ").append(schema).append(".").append(table)
                .append(" WHERE ").append(idColumn).append(" = ?");
        Object id = methodMap.get("get" + idColumnWithoutUnderscores).invoke(record);
        params.add(id);
        return query.toString();
    }

}
