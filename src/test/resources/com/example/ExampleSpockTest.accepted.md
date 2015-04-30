**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "inserting data" #

## Permutation 8ca304 (verified) ##

- **columns:** address1, address2, city
- **table:** address
- **values:** 121 Main, null, New Town

#### Results ####

- **sql:** INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');

---------------------------------------

## Permutation 99c155 (verified) ##

- **columns:** name, age
- **table:** person
- **values:** Joe, 55

#### Results ####

- **sql:** INSERT INTO person (name, age) VALUES ('Joe', 55);

---------------------------------------

## Permutation acf3c1 (verified) ##

- **columns:** age
- **table:** person
- **values:** 42

#### Results ####

- **sql:** INSERT INTO person (age) VALUES (42);

---------------------------------------

## Permutation faa682 (verified) ##

- **columns:** name
- **table:** person
- **values:** Bob

#### Results ####

- **sql:** INSERT INTO person (name) VALUES ('Bob');

# Test: "inserting data formatted as a table" #

- **table:** person

| Permutation | Verified | columns   | values  | OPERATIONS
| :---------- | :------- | :-------- | :------ | :------
| acf3c1      | true     | age       | 42      | **sql**: INSERT INTO person (age) VALUES (42);
| faa682      | true     | name      | Bob     | **sql**: INSERT INTO person (name) VALUES ('Bob');
| 99c155      | true     | name, age | Joe, 55 | **sql**: INSERT INTO person (name, age) VALUES ('Joe', 55);

---------------------------------------

- **table:** address

| Permutation | Verified | columns                  | values                   | OPERATIONS
| :---------- | :------- | :----------------------- | :----------------------- | :------
| 8ca304      | true     | address1, address2, city | 121 Main, null, New Town | **sql**: INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');
| ee2339      | true     | address1, address2, city | null, null, null         | **sql**: INSERT INTO address (address1, address2, city) VALUES (NULL, NULL, NULL);

# Test: "query API" #

| Permutation | Verified | keywords           | version | OPERATIONS
| :---------- | :------- | :----------------- | :------ | :------
| 9d364d      | true     | cars               | 4       | **query**: /api/4/search.json?q=cars
| 85a2c9      | true     | junit alternatives | 3       | **query**: /api/3/search.json?q=junit+alternatives
| 6c2035      | true     | testing examples   | 3       | **query**: /api/3/search.json?q=testing+examples
