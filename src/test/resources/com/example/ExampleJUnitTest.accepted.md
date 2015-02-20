**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "Display Name" #

**NO PERMUTATIONS**

# Test: "insertingData" #

## Permutation 8ca304 (verified) ##

- **columns:** address1, address2, city
- **table:** address
- **values:** 121 Main, null, New Town

#### Results ####

- **sql:** INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');

# Test: "insertingDataFormattedAsTable" #

- **table:** address

| Permutation | Verified | columns                  | values                   | RESULTS
| :---------- | :------- | :----------------------- | :----------------------- | :------
| 8ca304      | true     | address1, address2, city | 121 Main, null, New Town | **sql**: INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');

# Test: "insertingData_simple" #

## Permutation 7d22eb (verified) ##

- **columns:** age, name
- **tableName:** test_table
- **values:** 42, Fred

#### Results ####

- **sql:** INSERT INTO test_table (age, name) VALUES (42, 'Fred');

# Test: "queryAPI" #

| Permutation | Verified | keywords           | version | RESULTS
| :---------- | :------- | :----------------- | :------ | :------
| 85a2c9      | true     | junit alternatives | 3       | **query**: /api/3/search.json?q=junit+alternatives
