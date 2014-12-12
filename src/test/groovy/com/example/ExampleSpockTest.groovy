package com.example

import spock.lang.Specification
import spock.lang.Unroll
import testmd.Permutation
import testmd.TestMD

class ExampleSpockTest extends Specification {

    @Unroll
    def "inserting data"() {
        expect:
        ExampleLogic logic = new ExampleLogic();

        //Using TestMD.define directly to avoid a separate accepted.md file for each permutation
        def sql = logic.insertData(tableName, columns, values)
        TestMD.define(this.class, "inserting data").permutation([table: tableName, columns: columns, values: values])
                .addResult("sql", sql)
                .run({
            executeSql(sql)
            assertDataInserted(tableName, columns, values)
        } as Permutation.Verification)

        where:
        tableName | columns                                      | values
        "person"  | ["name"] as String[]                         | ["Bob"] as Object[]
        "person"  | ["age"] as String[]                          | [42] as Object[]
        "person"  | ["name", "age"] as String[]                  | ["Joe", 55] as Object[]
        "address" | ["address1", "address2", "city"] as String[] | ["121 Main", null, "New Town"] as Object[]
    }

    @Unroll
    def "inserting data formatted as a table"() {
        expect:
        ExampleLogic logic = new ExampleLogic();

        //Using TestMD.define directly to avoid a separate accepted.md file for each permutation
        def sql = logic.insertData(tableName, columns, values)
        TestMD.define(this.class, "inserting data formatted as a table").permutation([table: tableName, columns: columns, values: values])
                .asTable("columns", "values")
                .addResult("sql", sql)
                .run({
            executeSql(sql)
            assertDataInserted(tableName, columns, values)
        } as Permutation.Verification)

        where:
        tableName | columns                                      | values
        "person"  | ["name"] as String[]                         | ["Bob"] as Object[]
        "person"  | ["age"] as String[]                          | [42] as Object[]
        "person"  | ["name", "age"] as String[]                  | ["Joe", 55] as Object[]
        "address" | ["address1", "address2", "city"] as String[] | ["121 Main", null, "New Town"] as Object[]
        "address" | ["address1", "address2", "city"] as String[] | [null, null, null] as Object[]
    }

    @Unroll
    def "query APIs"() {
        expect:
        ExampleLogic logic = new ExampleLogic();

        //Using TestMD.define directly to avoid a separate accepted.md file for each permutation
        def query = logic.queryService(version, keywords)
        TestMD.define(this.class, "query API").permutation([keywords: keywords, version: version])
                .asTable("keywords", "version")
                .addResult("query", query)
                .run({ assertQueryResults(query, keywords) } as Permutation.Verification)

        where:
        keywords             | version
        "cars"               | 4
        "testing examples"   | 3
        "junit alternatives" | 3
        "junit alternatives" | 3
    }

    def assertQueryResults(query, keywords) {
        println "Executing ${query} and looking for ${keywords}"
    }

    def executeSql(sql) {
        //logic to execute the SQL would go here
        println "Executing ${sql}"
    }

    def assertDataInserted(String table, String[] columns, Object[] values) {
        //normally do assertion logic here

        println "Checking data in ${table} ${columns}"
        //UNCOMMENT TO TEST FAILING TEST:     assert table == "person" : "Did not insert into " + table
    }
}
