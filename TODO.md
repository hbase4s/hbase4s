### Tasks. MVP

1. Add documentation to README regarding query language and API (put, scan, delete, get). Examples left.

2. BUG: search for non string fields does not work. 
Need to extend parser where you can pass a type of data.
For example event.index == int(5) or event.index == int:5  (I like first option more) 
This adds extra complexity on searching for fields with custom fields. 
We need a way to transform those types. A way to understand how to represent them. 
Should postpone a feature of extension types support.

3. Extend filter support with some basic filters. 
Some filters left to fix (should it goes to below section)
I've incorrectly treat prefix in value based filters (it's related to data type). 
Issue related to #2 - I should use the same approach for both this issues.

4. Setup travis build and gitter chat.

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
