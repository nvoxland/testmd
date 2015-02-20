**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "inserting data" #

## Permutation 8ca304 (verified) ##

- **columns:** address1, address2, city
- **table:** address
- **values:** 121 Main, null, New Town

#### Results ####

- **sql:** INSERT INTO address (address1, address2, city) VALUES ('121 Main', NULL, 'New Town');

# Test: "inserting data formatted as a table" #

- **table:** address

| Permutation | Verified | columns                  | values           | RESULTS
| :---------- | :------- | :----------------------- | :--------------- | :------
| ee2339      | true     | address1, address2, city | null, null, null | **sql**: INSERT INTO address (address1, address2, city) VALUES (NULL, NULL, NULL);

# Test: "query API" #

| Permutation | Verified | keywords           | version | RESULTS
| :---------- | :------- | :----------------- | :------ | :------
| 85a2c9      | true     | junit alternatives | 3       | **query**: /api/3/search.json?q=junit+alternatives
