### Tasks. MVP

1. Add documentation to README regarding query language and API (put, scan, delete, get). 
Example added. Should add full API description. To do later on.


2. Extend filter support with some basic filters. 
Some filters left to fix (should it goes to below section)
I've incorrectly treat prefix in value based filters (it's related to data type). 
Issue related to #2 - I should use the same approach for both this issues.

3. Setup gitter chat.

### Tasks. Others

1. Extend filter support to cover all possible HBase filters and support all (most) options for each filter.

2. Provide Admin implementation to perform DDL operation in HBase and manage cluster.

3. Generate API doc.

4. Setup coverage with coverall.

### Ideas

1. Should I store Type information (Meta) in HBase together with Data.
It will allow extract data in respective types. 
User receives transformed data to specific type, but not `byte[]`
Also, it could simplify query language, as currently with type specific search I have to pass data type to filter.
Downside: not possible to use if data stored with different library, also more space used in DB.
