package com.cloudreach.connect.orm;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import lombok.Data;

import static org.junit.Assert.assertEquals;

public class QueryBuilderTest {

    private final long recordId = Math.abs(new Random().nextLong());
    private final Timestamp now = new Timestamp(System.currentTimeMillis());

    private QueryBuilder queryBuilder;
    private SampleTable record;

    @Before
    public void givenSampleTable() {
        queryBuilder = new QueryBuilder<>(
                "sage300",
                "sample_table",
                Arrays.asList("id_sample_table", "data", "insert_date"),
                SampleTable.class
        );
        queryBuilder.addColumnCast("data", "JSON");

        record = new SampleTable();
        record.setIdSampleTable(recordId);
        record.setData("{\"field\":123}");
        record.setInsertDate(now);
    }

    @Test
    public void whenBuildInsertThenQueryAndParamsAreCorrect() throws Exception {
        List params = new LinkedList();
        String query = queryBuilder.buildInsertQuery(record, params);

        assertEquals("INSERT INTO sage300.sample_table (\"data\", \"insert_date\") VALUES (?::JSON, ?)", query);
        assertEquals(2, params.size());
        assertEquals("{\"field\":123}", params.get(0));
        assertEquals(now, params.get(1));
    }

    @Test
    public void whenBuildUpdateThenQueryAndParamsAreCorrect() throws Exception {
        List params = new LinkedList();
        String query = queryBuilder.buildUpdateQuery(record, params);

        String expectedQuery = "UPDATE sage300.sample_table " +
                "SET \"data\" = ?::JSON, \"insert_date\" = ? " +
                "WHERE id_sample_table = ?";
        assertEquals(expectedQuery, query);
        assertEquals(3, params.size());
        assertEquals("{\"field\":123}", params.get(0));
        assertEquals(now, params.get(1));
        assertEquals(recordId, params.get(2));
    }

    @Test
    public void whenBuildDeleteThenQueryAndParamsAreCorrect() throws Exception {
        List params = new LinkedList();
        String query = queryBuilder.buildDeleteQuery(record, params);

        assertEquals("DELETE FROM sage300.sample_table WHERE id_sample_table = ?", query);
        assertEquals(1, params.size());
        assertEquals(recordId, params.get(0));
    }

}

@Data
class SampleTable {
    private Long idSampleTable;
    private String data;
    private Timestamp insertDate;
}
