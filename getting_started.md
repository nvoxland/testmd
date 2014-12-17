---
layout: default
title: TestMD | Getting Started
---

<div class="container" markdown="1">

# Getting Started: SQL Example

#### Start The Test ####

The _insertData()_ method in [com.example.ExampleLogic](src/test/java/com/example/ExampleLogic.java#L9) is a simple example of a class that generates SQL.

If you are using JUnit, it is easiest to use the _TestMDRule_ class to automatically manage test naming. For more control, see [testmd.TestMd](http://nvoxland.github.io/testmd/javadoc/testmd/TestMD.html).

{% highlight java %}
@Rule
public TestMDRule testmd = new TestMDRule();
{% endhighlight %}

You can then begin writing your test by capturing the output of _insertData_ for your input under test

{% highlight java %}
@Test
public void insertingData_simple() throws Exception {
    String tableName = "test_table";
    String[] columns = new String[] {"age", "name"};
    Object[] values = new Object[] {42, "Fred"};
    String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);

    //GOING TO ADD MORE HERE
}
{% endhighlight %}

All the test is doing so far is calling the method that generates an insert statement and saving the SQL for testing. At this point, there should be no calls out to external systems or anything else that would slow the test execution because this portion of the test is ALWAYS executed.

Once you have the SQL, we will use TestMD to ensure that it is correct by actually executing it if and only if it is changed.

#### Define Parameteres ####

First, we define a new permutation and describe it with parameters. The parameters uniquely identify the permutation:

{% highlight java %}
@Test
public void insertingData_simple() throws Exception {
    String tableName = "test_table";
    String[] columns = new String[] {"age", "name"};
    Object[] values = new Object[] {42, "Fred"};
    String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);

    testmd.permutation()
        .addParameter("tableName", tableName)
        .addParameter("columns", columns)
        .addParameter("values", values);
    //GOING TO ADD MORE HERE
}
{% endhighlight %}

Notice that the _TestMD_ object is designed to simply chain method calls together for an easier to read test. You should add a parameter for each key/value that helps uniquely identify this particular test case.

#### Define Results ####

Once we have a parameters defined, we add our "results" to the permutation:

{% highlight java %}
@Test
public void insertingData_simple() throws Exception {
    String tableName = "test_table";
    String[] columns = new String[] {"age", "name"};
    Object[] values = new Object[] {42, "Fred"};
    String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);

    testmd.permutation()
        .addParameter("tableName", tableName)
        .addParameter("columns", columns)
        .addParameter("values", values)
        .addResult("sql", sql);
    //GOING TO ADD MORE HERE
}
{% endhighlight %}

You can include as many result key/value pairs as you need and if any of them are different, the permutation will be re-verified. In our case, we just have one result to worry about.

#### Define Setup Logic ####

Now that we have the permutation defined, we can add logic for how to test it (if needed). First, there is usually some setup that needs to be done. This may include connecting to the database, truncating existing data, or anything else you need:


{% highlight java %}
private Connection connection;

@Test
public void insertingData_simple() throws Exception {
    String tableName = "test_table";
    String[] columns = new String[] {"age", "name"};
    Object[] values = new Object[] {42, "Fred"};
    String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);

    testmd.permutation()
        .addParameter("tableName", tableName)
        .addParameter("columns", columns)
        .addParameter("values", values)
        .addResult("sql", sql)
        .setup(new Setup() {
            @Override
            public void run() throws SetupResult {
                openConnection();
                if (connection == null) {
                    throw new SetupResult.CannotVerify("Connection not available");
                }
                resetDatabase();
                throw SetupResult.OK;
            }
        });
    //GOING TO ADD MORE HERE
}
{% endhighlight %}

If you cannot set up the environment for testing, throw SetupResult.CannotVerify. If everything is correctly set up, throw SetupResult.OK.

#### Define Cleanup Logic ####

If anything needs to be cleaned up after the test runs, add a "cleanup" call:

{% highlight java %}
@Test
public void insertingData_simple() throws Exception {
    String tableName = "test_table";
    String[] columns = new String[]{"age", "name"};
    Object[] values = new Object[]{42, "Fred"};
    String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);

    testmd.permutation()
        .addParameter("tableName", tableName)
        .addParameter("columns", columns)
        .addParameter("values", values)
        .addResult("sql", sql)
        .setup(new Setup() {
            @Override
            public void run() throws SetupResult {
                openConnection();
                if (connection == null) {
                    throw new SetupResult.CannotVerify("Connection not available");
                }
                resetDatabase();
                throw SetupResult.OK;
            }
        }).cleanup(new Cleanup() {
            @Override
                public void run() throws CleanupException {
                    closeConnection();
            }
        });
    //GOING TO ADD MORE HERE
}
{% endhighlight %}

#### Define Assertion Logic ####

We are now ready for our actual assertions and our final code:

{% highlight java %}
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
        .addResult("sql", sql)
        .setup(new Setup() {
            @Override
            public void run() throws SetupResult {
                openConnection();
                if (connection == null) {
                    throw new SetupResult.CannotVerify("Connection not available");
                }
                resetDatabase();
                throw SetupResult.OK;
            }
        }).cleanup(new Cleanup() {
            @Override
            public void run() throws CleanupException {
                closeConnection();
            }
        }).run(new Verification() {
            @Override
            public void run() throws CannotVerifyException, AssertionError {
                executeSql(sql);
                assertDataInserted(tableName, columns, values);
            }
        });
    }
}
{% endhighlight %}

Within the _Verification_ call, you actually execute the genearted SQL, then run any assertion logic you need to ensure that it runs successfully.

#### Run The Test ####

Now run your test method like you would run any JUnit test. The first time it executes, it will take longer to execute--especially if you have real database connection logic in _openConnection()_ and _executeSql()_.

Running it a second time, however, will take virtually no time at all. The secret is the [src/test/resources/com/example/ExampleJUnitTest.insertingData_simple.accepted.md](src/test/resources/com/example/ExampleJUnitTest.insertingData_simple.accepted.md) file that gets created in on your file system. That file stores the fact that the *ExampleJUnitTest.insertingData_simple* test was ran successfully with "table=test_table, columns=[age, name] and values=[42, Fred]". It also lists the SQL that was executed as the result.

If you manually edit the sql stored in the accepted.md file and re-run your test, you will see that the SQL is re-executed and verified and the file is re-written as verified again.

If you delete the existing accepted.md file and change the _Setup_ logic to throw a _SetupResult.CannotVerify_ exception, when you rerun the test the file will be re-written and the test will be marked as "UNVERIFIED: Connection not available". Make the test no longer throw _CannotVerify_ and re-run the test and it will be saved as VERIFIED.

#### Commit The accepted.md File ####

Whenever you have a new test using TestMD, commit the corresponding accepted.md files. Now any other developers running your tests will only need to re-verify the SQL if they make a change that alters the generated SQL.

As you are working on your application, watch for changes to accepted.md files. Are there suddenly results that changed unexpectedly? Are those changes verified? The markdown format is designed to be easily readable in any diff program so watch them as part of every commit.

Are you wondering what has been tested? The markdown format is also designed to be readable through GitHub or any other markdown compatible web view. Simply go to the directory containing your tests such as [src/test/resources/com/example](src/test/resources/com/example/) and read through what has been tested.

#### Multiple Permutation Per Test ####

Usually, one test of a method is not enough to ensure it is working correctly--you need to test it with multiple different value permutations. The TestMD accepted.md format is designed to group multiple different test runs into a single test file for easier readability.

For a given testClass/testName combination, you can define as many permutations as you want. Each one is uniquely identified by its parameters and re-ran if the results change.

In this test:

{% highlight java %}
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
            .addResult("sql", sql)
            .run(new Verification() {
                @Override
                public void run() {
                    executeSql(sql);
                    assertDataInserted(tableName, columns, values);
                }
            });
    }
}
{% endhighlight %}

we define multiple permutations and then loop through them with a resulting file that looks like [src/test/resources/com/example/ExampleJUnitTest.insertingData.accepted.md](src/test/resources/com/example/ExampleJUnitTest.insertingData.accepted.md)

#### Formatting Permutations As A Table ####

Depending on what is being tested, it sometimes helps readability to group permutations in a table. Adding a call to _.asTable(parameterNames)_ in the permutation definition allows you define the names of the parameters that should be used in the table.

For example:

{% highlight java %}
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
            .asTable("columns", "values")
            .addResult("sql", sql)
            .run(new Verification() {
                @Override
                public void run() {
                    executeSql(sql);
                    assertDataInserted(tableName, columns, values);
                }
        });
    }
}
{% endhighlight %}

will save the results as [src/test/resources/com/example/ExampleJUnitTest.insertingDataFormattedAsTable.accepted.md](src/test/resources/com/example/ExampleJUnitTest.insertingDataFormattedAsTable.accepted.md).

Functionality-wise there is no difference between table-formatted and regular-formatted accepted.md files. Use whatever is most readable for each test.

## Other Uses: Not Just SQL

TestMD works well any time you are able to describe the interaction between two systems in a simple yet deterministic way.

REST-style webservices work well with TestMD:

{% highlight java %}
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
            .asTable("keywords", "version")
            .addResult("query", query)
            .run(new Verification() {
                @Override
                public void run() {
                    assertQueryResults(query, keywords);
                }
        });

    }
}
{% endhighlight %}

As do:

- NoSQL database interactions
- SOAP and RPC calls
- And More

## Spock Example ##

TestMD is designed to work well with the Spock testing framework. Because of the improved permutation support in Spock, the tests tend to be even cleaner than JUnit.

{% highlight groovy %}
@Unroll
def "inserting data"() {
expect:
ExampleLogic logic = new ExampleLogic();

//Using TestMD.test directly to avoid a separate accepted.md file for each permutation
def sql = logic.generateInsertSql(tableName, columns, values)
TestMD.test(this.class, "inserting data").permutation([table: tableName, columns: columns, values: values])
    .addResult("sql", sql)
    .run({
        executeSql(sql)
        assertDataInserted(tableName, columns, values)
    } as Verification)

where:
tableName | columns                                      | values
"person"  | ["name"] as String[]                         | ["Bob"] as Object[]
"person"  | ["age"] as String[]                          | [42] as Object[]
"person"  | ["name", "age"] as String[]                  | ["Joe", 55] as Object[]
"address" | ["address1", "address2", "city"] as String[] | ["121 Main", null, "New Town"] as Object[]
}
{% endhighlight %}

## Final Details ##

- If you have any bugs or feature enhancements, use the GitHub Project Issues
- Pull requests area always appreciated
- Feel free to contact me at nathan@liquibase.org or [@nvoxland](https://twitter.com/nvoxland) with any additional questions or comments
- Each permutation in the accepted.md files contain a 6 digit alphanumeric "key" that is used to easily identify the permutation, even if the order is shifted.
- The accepted.md file stores results in an automatically-sorted manner to make diffs more consistent.
- You can control the formatting of parameters and results in the accepted.md file with a [ValueFormat](http://nvoxland.github.io/testmd/javadoc/testmd/ValueFormat.html) object if needed. Just use the overloaded _addParameter()_ and _addResult()_ methods.
- You can call _addNote(key, note)_ to your permutation definition to save informational notes in the accepted.md file. These notes do not affect the identification of permutations or the decision to re-verify them.
- Javadoc is available at [http://nvoxland.github.io/testmd/javadoc](http://nvoxland.github.io/testmd/javadoc/index.html)
- Happy Testing!

</div>