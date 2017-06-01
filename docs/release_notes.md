# Release notes

### 0.1.2

- ability to store and query data by `Option[T]`, where `T` is one of 8th basic types. 
- ability to introduce it's own `Encoder` to be able support custom data types.
- extended scanning DSL, added ability to set `start_row`, `stop_row` in the scanning condition.

### 0.1.1

- introduced basic wrapper operations to work with HBase: `put`, `get`, `delete`, `scan`
- added support for 8 basic types while storing and retrieving data from HBase.
- introduced query DSLs (scala static and string based) with support of most important filters in HBase. 