package com.example;

import org.junit.Rule;
import org.junit.Test;
import testmd.junit.TestMDRule;
import testmd.logic.*;
import testmd.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;

public class ExampleJUnitTest {

    @Rule
    public TestMDRule testmd = new TestMDRule();
    private Connection connection;

    @Test
    public void insertingData_simple() throws Exception {
        final String tableName = "test_table";
        final String[] columns = new String[]{"age", "name"};
        final Object[] values = new Object[]{42, "Fred"};
        final String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);

        testmd.permutation()
                .addParameter("tableName", tableName)
                .addParameter("columns", columns)
                .addParameter("values", values)
                .addOperation("sql", sql)
                .setup(new Runnable() {
                    @Override
                    public void run() throws SetupResult {
                        openConnection();
//                        if (connection == null) {
//                            throw new SetupResult.CannotVerify("Connection not available");
//                        }
                        resetDatabase();
                        throw SetupResult.OK;
                    }
                }).cleanup(new Runnable() {
                    @Override
                    public void run() {
                        closeConnection();
                    }
        }).run(new Runnable() {
            @Override
            public void run() throws CannotVerifyException, AssertionError {
                executeSql(sql);
                assertDataInserted(tableName, columns, values);
            }
        });
        //NOTHING MORE TO ADD!
    }

    @Test
    public void insertingData() throws Exception {
        ExampleLogic logic = new ExampleLogic();

        Object[][] permutations = new Object[][]{
                new Object[]{"person", new String[]{"name"}, new Object[]{"Bob"}},
                new Object[]{"person", new String[]{"age"}, new Object[]{42}},
                new Object[]{"person", new String[]{"name", "age"}, new Object[]{"Joe", 55}},
                new Object[]{"address", new String[]{"address1", "address2", "city"}, new Object[]{"121 Main", null, "New Town"}}
        };

        for (Object[] permutation : permutations) {
            final String tableName = (String) permutation[0];
            final String[] columns = (String[]) permutation[1];
            final Object[] values = (Object[]) permutation[2];

            final String sql = logic.generateInsertSql(tableName, columns, values);
            testmd.permutation()
                    .addParameter("table", tableName)
                    .addParameter("columns", columns)
                    .addParameter("values", values)
                    .addOperation("sql", sql)
                    .run(new Runnable() {
                        @Override
                        public void run() {
                            executeSql(sql);
                            assertDataInserted(tableName, columns, values);
                        }
                    });
        }
    }

    @Test
    public void insertingDataFormattedAsTable() throws Exception {
        ExampleLogic logic = new ExampleLogic();

        Object[][] permutations = new Object[][]{
                new Object[]{"person", new String[]{"name"}, new Object[]{"Bob"}},
                new Object[]{"person", new String[]{"age"}, new Object[]{42}},
                new Object[]{"person", new String[]{"name", "age"}, new Object[]{"Joe", 55}},
                new Object[]{"address", new String[]{"address1", "address2", "city"}, new Object[]{"121 Main", null, "New Town"}}
        };

        for (Object[] permutation : permutations) {
            final String tableName = (String) permutation[0];
            final String[] columns = (String[]) permutation[1];
            final Object[] values = (Object[]) permutation[2];

            final String sql = logic.generateInsertSql(tableName, columns, values);
            testmd.permutation()
                    .addParameter("table", tableName)
                    .addParameter("columns", columns)
                    .addParameter("values", values)
                    .formattedAsTable("columns", "values")
                    .addOperation("sql", sql)
                    .run(new Runnable() {
                        @Override
                        public void run() {
                            executeSql(sql);
                            assertDataInserted(tableName, columns, values);
                        }
                    });
        }

    }

    @Test
    public void queryAPI() throws Exception {
        ExampleLogic logic = new ExampleLogic();

        Object[][] permutations = new Object[][]{
                new Object[]{"cars", 4},
                new Object[]{"testing examples", 3},
                new Object[]{"junit alternatives", 3},
                new Object[]{"junit alternatives", 3}
        };

        for (Object[] permutation : permutations) {
            final String keywords = (String) permutation[0];
            int version = (Integer) permutation[1];
            final String query = logic.generateQueryRequest(version, keywords);
            testmd.permutation().addParameter("keywords", keywords)
                    .addParameter("version", version)
                    .formattedAsTable("keywords", "version")
                    .addOperation("query", query)
                    .run(new Runnable() {
                        @Override
                        public void run() {
                            assertQueryResults(query, keywords);
                        }
                    });

        }
    }

    private void assertDataInserted(String table, String[] columns, Object[] values) {
        //normally do assertion logic here
        System.out.println("Checking data in " + table + " " + StringUtils.join(Arrays.asList(columns), ", ", false) + ")");

        //UNCOMMENT TO TEST FAILING TEST:     assert table == "person" : "Did not insert into " + table
    }

    private void executeSql(String sql) {
        System.out.println("Executing " + sql);
    }

    private void assertQueryResults(String query, String keywords) {
        System.out.println("Executing " + query + " and looking for " + keywords);
    }

    private void openConnection() {
    }

    private void closeConnection() {
    }

    private void resetDatabase() {
    }

}
