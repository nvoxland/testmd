**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "insertingData" #

## Permutation 8ca3042 (verified) ##

- **columns:** address1, address2, city
- **table:** address
- **values:** 121 Main, null, New Town

#### Results ####

- **sql:** INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');

---------------------------------------

## Permutation 99c1556 (verified) ##

- **columns:** name, age
- **table:** person
- **values:** Joe, 55

#### Results ####

- **sql:** INSERT INTO person (name, age) VALUES ('Joe', 55);

---------------------------------------

## Permutation acf3c1a (verified) ##

- **columns:** age
- **table:** person
- **values:** 42

#### Results ####

- **sql:** INSERT INTO person (age) VALUES (42);

---------------------------------------

## Permutation faa682a (verified) ##

- **columns:** name
- **table:** person
- **values:** Bob

#### Results ####

- **sql:** INSERT INTO person (name) VALUES ('Bob');

# Test: "insertingDataFormattedAsTable" #

- **table:** person

| Permutation | Verified | columns   | values  | OPERATIONS
| :---------- | :------- | :-------- | :------ | :------
| acf3c1a     | true     | age       | 42      | **sql**: INSERT INTO person (age) VALUES (42);
| faa682a     | true     | name      | Bob     | **sql**: INSERT INTO person (name) VALUES ('Bob');
| 99c1556     | true     | name, age | Joe, 55 | **sql**: INSERT INTO person (name, age) VALUES ('Joe', 55);

---------------------------------------

- **table:** address

| Permutation | Verified | columns                  | values                   | OPERATIONS
| :---------- | :------- | :----------------------- | :----------------------- | :------
| 8ca3042     | true     | address1, address2, city | 121 Main, null, New Town | **sql**: INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');

# Test: "insertingData_simple" #

## Permutation 7d22ebb (verified) ##

- **columns:** age, name
- **tableName:** test_table
- **values:** 42, Fred

#### Results ####

- **sql:** INSERT INTO test_table (age, name) VALUES (42, 'Fred');

# Test: "queryAPI" #

| Permutation | Verified | keywords           | version | OPERATIONS
| :---------- | :------- | :----------------- | :------ | :------
| 9d364d7     | true     | cars               | 4       | **query**: /api/4/search.json?q=cars
| 85a2c92     | true     | junit alternatives | 3       | **query**: /api/3/search.json?q=junit+alternatives
| eed6284     | true     | junit alternatives | 5       | **query**: /api/5/search.json?q=junit+alternatives
| 6c20352     | true     | testing examples   | 3       | **query**: /api/3/search.json?q=testing+examples

# Test Version: "3d0cfd" #