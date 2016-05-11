package com.cloudreach.connect.orm;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class BaseDao<T> {

    protected static final QueryRunner queryRunner = new QueryRunner();
    protected static final BeanProcessor beanProcessor = new GenerousBeanProcessor();
    protected static final RowProcessor rowProcessor = new BasicRowProcessor(beanProcessor);

    protected final Connection db;
    protected final QueryBuilder queryBuilder;
    protected final ResultSetHandler<T> beanHandler;
    protected final ResultSetHandler<List<T>> listHandler;

    protected final ResultSetHandler<Long> idHandler = new ScalarHandler<>("id_" + getTable());

    public BaseDao(Connection db, Class<T> beanClass, Map<String, String> columnCastMap)
            throws SQLException {
        this(db, beanClass);
        queryBuilder.addColumnCast(columnCastMap);
    }

    public BaseDao(Connection db, Class<T> beanClass) throws SQLException {
        this.db = db;
        queryBuilder = new QueryBuilder<>(getSchema(), getTable(), getColumns(), beanClass);
        beanHandler = new BeanHandler<>(beanClass, rowProcessor);
        listHandler = new BeanListHandler<>(beanClass, rowProcessor);
    }

    public Long create(T record) throws Exception {
        List parameters = new LinkedList();
        String query = queryBuilder.buildInsertQuery(record, parameters);
        return queryRunner.insert(db, query, idHandler, parameters.toArray());
    }

    public List<T> findAll() throws SQLException {
        String query = "SELECT * FROM " + getSchema() + "." + getTable();
        return queryRunner.query(db, query, listHandler);
    }

    public T findOneById(long id) throws Exception {
        String query = "SELECT * " +
                "FROM " + getSchema() + "." + getTable() + " " +
                "WHERE id_" + getTable() + " = ?";
        Object[] params = new Object[]{id};
        return queryRunner.query(db, query, beanHandler, params);
    }

    public void update(T record) throws Exception {
        List parameters = new LinkedList();
        String query = queryBuilder.buildUpdateQuery(record, parameters);
        queryRunner.update(db, query, parameters.toArray());
    }

    public void delete(T record) throws Exception {
        List parameters = new LinkedList();
        String query = queryBuilder.buildDeleteQuery(record, parameters);
        queryRunner.update(db, query, parameters.toArray());
    }

    public List<String> getColumns() throws SQLException {
        String query = "SELECT column_name " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? " +
                "AND table_name = ?";
        Object[] params = new Object[]{getSchema(), getTable()};
        ResultSetHandler<List<String>> rsHandler = new ColumnListHandler<>("column_name");
        return queryRunner.query(db, query, rsHandler, params);
    }

    public abstract String getSchema();

    public abstract String getTable();

}
