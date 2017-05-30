### Tasks. MVP

1. README. Basic documentation added. Review and make sure it's enough to get started.

2. README. Describe how to extend library (how to extend HBase client, parser?)
It's mandatory, as currently there are no full support for existing HBase API (especially filters coverage).

3. Make common API DSL wrappers available through package object io.github.hbase4s._ 

4. Add possibility to select columns we want to get after scan

5. Add scan options to filters (in scope of MVP start_row, stop_row)

### Tasks. Others

1. Extend filter support to cover all possible HBase filters and support all (most) options for each filter.
Complexity: NORMAL

2. Provide Admin implementation to perform DDL operation in HBase and manage cluster.
Why do I need it?
Complexity: HIGH

3. Generate API docs(to be done as soon as stabilized).

4. Add support of custom types (for filters, and case class mappings).
Complexity: HIGH

5. Performance testing. Comparison of HBase4s with HBase client java library.

6. Add support of Option as case class field (if relevant column was not found in table populate with None).
Complexity: NORMAL

7. Setup gitter chat.

8. There is inconsistency between scala DSL and string based DSL in names. 
It would be good to use the same names. 

9. Query language should include not just filter expressions, but also conditions from scan (stop, start rows, batch size etc.)

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