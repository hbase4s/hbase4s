### Urgent tasks

1. Documentation

- Describe how to extend library (how to extend HBase client, parser?)
It's mandatory, as currently there are no full support for existing HBase API (especially filters coverage).

- It would be good to add clear description 

- Need few tests for different exception scenarios 
(nonexistent field in search, attempt to store unsupported type etc.)

2. Coding

- Do not hard code family name with case class name, give opportunity to user set non default family name.

- Make common API DSL wrappers available through package object `io.github.hbase4s._` 

- Add possibility to select columns we want to get after scan (theoretically it's high demand feature as it can improve performance, can't it)

### Other tasks

1. Extend filter support to cover all possible HBase filters and support all (most) options for each filter.
Complexity: NORMAL
- add support for different features like `scan.set*`
- query by regexp (column value, row value etc.) 

2. Provide Admin implementation to perform DDL operation in HBase and manage cluster.
Why do I need it?
Complexity: HIGH

3. Generate API docs (to be done as soon as library stabilized).

4. Performance testing. Comparison of HBase4s with HBase client java library. Basic test present.

5. Setup gitter chat.

6. There is inconsistency between scala DSL and string based DSL in names.  It would be good to use the same names. 

### Ideas

1. Should I store Type information (Meta) in HBase together with Data.
It will allow extract data in respective types. 
User receives transformed data to specific type, but not `byte[]`
Also, it could simplify query language, as currently with type specific search I have to pass data type to filter.
Downside: not possible to use if data stored with different library, also more space used in DB.

2. Implement salting option of row key on put to prevent hotspotting.
 Adding two random letters before each row-key will guarantee evenly-distributed data across the cluster
 (two letters - around 1000 combinations - nodes).
 Put method has to return generated key. Salt should be a hash function of existing row key.
 Salt mechanism can be applied inside each method (get,delete,put etc.), so user will never no about it.
 Salt mechanism should be enabled/disable on HBaseClient level (and applied automatically for all methods if enabled).
 Problem - won't be able to search for "startWith"???

3. HBase documentation recommends to have short family name and the same applied for column names,
because all this data is stored quite in HBase for each record.

4. For simplicity, should be possibility to refer columns by name without family name be added?
This can be done for ex. in WrappedResults where we have all columns extracted and can analyze if there are different family names there.

5. Example of API for hbase: https://github.com/nerdammer/spark-hbase-connector

6. Performance optimization: cache byte[] values for column families and qualifiers.
There are couple of options here. One is to cache everything, without letting user know about it happening.
Cache can/should be applied for families names, qualifiers. 

7. Encoders can be alternatively implemented as implicit classes. (as Json Writters that pass as implicit params).
 Default list of those implicit classes for basic classes might be available from some global package object.
 
8. More on Options: if column not found - write None into Option field. 
If trying to write None - do not create column at all. Too specific??? Can be problems with support by other libs.
 
9. Should I include HBase version into hbase4s version? For. ex 0.1.2_1.3.1 (where left part is hbase4s, right is hbase client lib version) 