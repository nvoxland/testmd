**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "complex test with tables" #

- **Group Param:** a
- **Param 1:** param 1 is a
- **Param 2:** param 2 is a

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | OPERATIONS
| :---------- | :------- | :------------ | :------------ | :------------ | :------------- | :------
| b44a968     | true     | tp1 is a      | tp2 is a      |               |                | **out data**: Permutation 1

---------------------------------------

- **Group Param:** b
- **Param 1:** param 1 is d
- **Param 2:** param 2 is d

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | OPERATIONS
| :---------- | :------- | :------------ | :------------ | :------------ | :------------- | :------
| 9509406     | Too dark |               |               |               | tpx1 is a      | **out data**: Permutation 6
| 1390e95     | false    |               |               |               | tpx1 is b      | **out data**: Permutation 7<br>With a second line with &#124; chars<br>And another with &#124; chars

---------------------------------------

- **Group Param:** b
- **Param 1:** param 1 is b
- **Param 2:** param 2 is b

| Permutation | Verified         | table param 1 | table param 2 | table param 3 | table param x1 | OPERATIONS
| :---------- | :--------------- | :------------ | :------------ | :------------ | :------------- | :------
| e35a789     | false            |               | tp2 is b      |               |                | **out data**: Permutation 2
| bc25949     | false            |               | tp2 is e      |               |                | **out data**: Permutation 5
| 26172b7     | Too lazy to test | tp1 is d      | tp2 is d      | tp3 is d      |                | __more info__: Some notes for this
|             |                  |               |               |               |                | __yet more info__: Even more notes for this
|             |                  |               |               |               |                | **more out data**: Permutation 4 extra data
|             |                  |               |               |               |                | **out data**: Permutation 4

---------------------------------------

- **Group Param:** a
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | OPERATIONS
| :---------- | :------- | :------------ | :------------ | :------------ | :------------- | :------
| 8fee938     | true     |               | tp2 is b      |               |                | **String data**: No notes, just one data

---------------------------------------

- **Group Param:** b
- **Param 1:** param 1 is c
- **Param 2:** param 2 is c

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | OPERATIONS
| :---------- | :------- | :------------ | :------------ | :------------ | :------------- | :------
| 1cf80ce     | true     | tp1 is c      | tp2 is c      | tp3 is c      |                | __more info__: Some notes for permutation 3
|             |          |               |               |               |                | **out data**: Permutation 3
