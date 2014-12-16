# TestMD: Fast Integration Testing For Java

Automated testing interactions between systems is difficult. The two standard options are:

- "Unit Testing" where you mock the external system so your tests can run quickly at the expense of not really knowing if your interaction is correct
- "Integration Testing" where you actually interact with the external system which ensures the code is correct but the tests take far too long to execute and rely on specific environments to be set up

TestMD gives you the speed of unit testing with the safety of integration testing by tracking how the integration is done and performing a time-consuming integration test IF AND ONLY IF there has been a change.

Results of new and previous results are stored in markdown-formatted files that are designed to be human readable both in pull requests and through github source views.


##Features

**Previously Accepted Tests Stored As Markdown Format** integrates well with GitHub or any other version control views that support markdown formatting. 

**Previous Accepted Permutations Can Be Grouped As Tables** as needed to improve readability.

**Works with JUnit, Spock, or any other testing framework**

**Supports Saving "Unverified" results** Found a change in logic but you cannot run the automated tests? New accepted results can be committed that are marked as "unverified" and future runs will attempt to verify them automatically. 


## Getting Started: SQL Example

#### Write The Test ####

The _insertData()_ method in [com.example.ExampleLogic](blob/master/src/test/java/com/example/ExampleLogic.java#L9) is a simple example of a class that generates SQL.

There are many permutation of values that can be passed into the method including null values so there are many tests that need to be to run. 

If you are using JUnit, it is easiest to use the _TestMDRule_ class to automatically manage test naming rather than calling _TestMD.test(testClass, testName)_ yourself.

```
@Rule
public TestMDRule testmd = new TestMDRule();
```

You can then begin writing your test by capturing the output of _insertData_ for your input under test

```java
@Test
public void insertingData_simple() throws Exception {
    String tableName = "test_table";
    String[] columns = new String[] {"age", "name"};
    Object[] values = new Object[] {42, "Fred"}; 
    String sql = new ExampleLogic().generateInsertSql(tableName, columns, values);
    
    //GOING TO ADD MORE HERE
}
```

All the test is doing so far is calling the method that generates an insert statement and saving the SQL for testing. At this point, there should be no calls out to external systems or anything else that would slow the test execution because this portion of the test is ALWAYS executed.

Once you have the SQL, we will use TestMD to ensure that is is correct by actually executing it if and only if it is changed.

First, we define a new permutation and describe it with parameters. The parameters uniquely identify the permutation:

```java
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
```   
    
Notice that the _testmd_ object is designed to simply chain method calls together for an easier to read test. You should add a "parameter" for each key/value that helps uniquely identify this particular test case.

Once we have a parameters defined, we add our "results" to the permutation:

```java
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
```

You can include as many result key/value pairs as you need and if any of them are different, the permutation will be re-verified. In our case, we just have one result to test.

Now that we have the permutation defined, we can add logic for how to test it (if needed). First, there is usually some setup that needs to be done. This may include connecting to the database, truncating existing data, or anything else you need:


```java
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
```

If you cannot set up the environment for testing, throw SetupResult.CannotVerify. If everything is correctly set up, throw SetupResult.OK.

If anything needs to be cleaned up after the test runs, add a "cleanup" call:

```java
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
``` 

We are now ready for our actual assertions and our final code:

```java
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
```

Within the _Verification_ call, you actually execute the genearted SQL, then run any assertion logic you need to ensure that it runs successfully.

#### Run The Test ####

Now run your test method like you would run any JUnit test. The first time it executes, it will take longer to execute--especially if you have real database connection logic in openConnection() and executeSql.

Running it a second time, however, will take no time at all. The secret is the [src/test/resources/com/example/ExampleJUnitTest.insertingData_simple.accepted.md](blob/master/src/test/resources/com/example/ExampleJUnitTest.insertingData_simple.accepted.md) file that gets created in on your file system. That file stores the fact that the *ExampleJUnitTest.insertingData_simple* test was ran successfully with "table=test_table, columns=[age, name] and values=[42, Fred]". It also lists the SQL that was executed as the result. 

If you manually edit the sql stored in the accepted.md file and re-run your test, you will see that the SQL is re-executed and verified and the file is re-written as verified again. 

If you delete the existing accepted.md file and change the _Setup_ logic to throw a _SetupResult.CannotVerify_ exception, when you rerun the test the file will be re-written and the test will be marked as "UNVERIFIED: Connection not available". Make the test no longer throw _CannotVerify_ and re-run the test and it will be saved as VERIFIED.

#### Commit the accepted.md File ####

Whenever you have a new test using TestMD, commit the corresponding accepted.md files. Now any other developers running your tests will only need to re-verify the SQL if they make a change that alters the generated SQL. 

As you are working on your application, watch for changes to accepted.md files. Are there suddenly results that changed unexpectedly? Are those changes verified? The markdown format is designed to be easily readable in any diff program so watch them as part of every commit.

Are you wondering what has been tested? The markdown format is also designed to be readable through GitHub or any other markdown compatible web view. Simply go to the directory containing your tests such as [src/test/resources/com/example](blob/master/src/test/resources/com/example/) and read through what has been tested.

#### Multiple Permutation Per Test ####

Usually, one test of a method is not enough to ensure it is working correctly--you need to test it with multiple different value permutations. The TestMD accepted.md format is designed to group multiple different test runs into a single test file for easier readability. 

The name of the accepted.md file is based on the parameters passed to _TestMD.test(testClass, testName)_ which when managed by TestMDRule is the JUnit class and test method but you can use the TestMD class directly for more control. 

For a given testClass/testName combination, you can define as many permutations as you want. Each one is uniquely identified by its parameters and re-ran if the results change.

In this test:

```java
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
```  

we define multiple permutations and then loop through them with a resulting file that looks like [src/test/resources/com/example/ExampleJUnitTest.insertingData.accepted.md](blob/master/src/test/resources/com/example/ExampleJUnitTest.insertingData.accepted.md)         

#### Formatting Permutations As A Table ####

Depending on what is being tested, it sometimes helps readability to group permutations in a table. Adding a call to _.asTable(parameterNames)_ in the permutation definition allows you define the names of the parameters that should be used in the table.

For example:

```java
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
```  

will save the results as [src/test/resources/com/example/ExampleJUnitTest.insertingDataFormattedAsTable.accepted.md](blob/master/src/test/resources/com/example/ExampleJUnitTest.insertingDataFormattedAsTable.accepted.md). 

Functionality-wise there is no difference between table-formatted and regular-formatted accepted.md files. Chose purely on what is most readable for each test.        

## Other Uses: Not Just SQL

TestMD works well any time you are able to describe the interaction between two systems in a simple yet deterministic way. 

REST-style webservices work well with TestMD

```java
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
```

As do:

- NoSQL database interactions 
- SOAP and RPC calls
- And More
     
## Spock Example ##

TestMD is designed to work well with the Spock testing framework. Because of the improved permutation support in Spock, the tests tend to be even cleaner than JUnit.

```groovy
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
```

## Final Details ##

- Congratulations on reading all the way to the bottom of this README.
- If you have any bugs or feature enhancements, use the Github Project Issues
- Pull requests area always appreciated
- Feel free to contact me at nathan@liquibase.org or <a href="https://twitter.com/nvoxland" class="twitter-follow-button" data-show-count="false">Follow @nvoxland</a>
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script> with any additional questions or comments
- Each permutation in the accepted.md files contain a 6 digit alphanumeric "key" that is used to easily identify the permutation, even if the order is shifted.
- The accepted.md file stores results in an automatically-sorted manner to make diffs more consistent.
- You can control the formatting of parameters and results in the accepted.md file with a ValueFormat object if needed. Just use the overloaded _addParameter()_ and _addResult()_ methods.
- You can call _addNote(key, note)_ to your permutation definition to save informational notes in the accepted.md file. These notes do not affect the indentification of permutations or the decision to re-verify them.      
- Javadoc is available at [http://nvoxland.github.io/testmd/javadoc](http://nvoxland.github.io/testmd/javadoc/index.html)


  