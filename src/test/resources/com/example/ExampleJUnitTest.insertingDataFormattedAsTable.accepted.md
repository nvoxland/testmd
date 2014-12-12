# Test: com.example.ExampleJUnitTest "insertingDataFormattedAsTable" #

NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY

---------------------------------------

- **table:** person

| Permutation | Verified | columns   | values  | RESULTS
| :---------- | :------- | :-------- | :------ | :------
| acf3c1      | true     | age       | 42      | **sql**: INSERT INTO person (age) VALUES (42);
| faa682      | true     | name      | Bob     | **sql**: INSERT INTO person (name) VALUES ('Bob');
| 99c155      | true     | name, age | Joe, 55 | **sql**: INSERT INTO person (name, age) VALUES ('Joe', 55);


---------------------------------------

- **table:** address

| Permutation | Verified | columns                  | values                   | RESULTS
| :---------- | :------- | :----------------------- | :----------------------- | :------
| 8ca304      | true     | address1, address2, city | 121 Main, null, New Town | **sql**: INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');


---------------------------------------

