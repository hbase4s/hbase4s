# HBase4s

Scala wrapper around HBase client library that provides user-friendly type-safe API and query language to work with HBase database.
  
HBase client has extended powerful API with huge amount of different sophisticated options.

There are no intention to cover all this options with hbase4s core library. 
The goal is to build framework that will support all basic functionality and 
give user opportunity easily extend it for it's particular goals.  

MVP (minimal valuable product) functionality includes:
- support all CRUD operations with basic options (`Get`, `Scan`, `Delete`, `Put`)
- user friendly query language that cover most popular HBase filters.

Note: raise an issue if some of basic functionality was missed

## CRUD

HBase does not support data types. All columns and row key are stored as array of `bytes`. 
Java HBase client works with the same data structure. 

Primary goal of HBase4s is to provides type-safe API for CRUD operations on top of Java HBase client.

There are default implicit support for the following scala types:
```String, Boolean, Short, Int, Long, Float, Double, BigDecimal```.

This list can be extended by registering relevant extractor. // TODO: implement this feature

User can use any of specified type to store, retrieve, get or delete data from HBase. 

Relevant type transformation applied both for row key and columns values.

While data is stored to HBase, there are no need to pass types explicitly, they will be inference implicitly.
However, during retrieval there are no information, of what type is. 
So, API require user to provide type information both for row key and params. Examples below. 

One of overridden version of `put` function accepts case classes as value parameter. 
Note, all fields of case class have to be within specified above types.
    
Also, results of `scan, scanAll, get` functions can be transformed to provided case class type.     

## Filter DSL

HBase does not support SQL-like query language. 

However, it provides Filter API, that allows `scan` tables individually (no joins) based on row/columns information. 

HBase4s provide two version of DSL: string based (whole filter is represented as string) or Scala DSL based (set of scala classes).



