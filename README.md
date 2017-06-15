# HBase4s <sup>*beta*</sup>

[![Build Status](https://travis-ci.org/hbase4s/hbase4s.svg?branch=develop)](https://travis-ci.org/hbase4s/hbase4s)
[![Coverage Status](https://coveralls.io/repos/github/hbase4s/hbase4s/badge.svg?branch=develop)](https://coveralls.io/github/hbase4s/hbase4s?branch=develop)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a0092dd676154718af28f83f5309efd2)](https://www.codacy.com/app/vglushak-vt/io.github.hbase4s?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=vglushak-vt/io.github.hbase4s&amp;utm_campaign=Badge_Grade)


Scala wrapper for Java HBase client library that provides user-friendly type-safe API and query language to work with HBase database.
  
HBase client has extended powerful API with huge amount of different sophisticated options. It won't be possible to cover all those options with hbase4s core library. 
The goal is to build framework that will support all basic functionality and 
give user opportunity easily extend it for it's particular goals.  

MVP (minimal valuable product) functionality includes:
- support all CRUD operations with basic options (`Get`, `Scan`, `Delete`, `Put`)
- user friendly query language that cover most popular HBase filters.

## Getting started

Add following library to your project:

```libraryDependencies += "io.github.hbase4s" %% "hbase4s-core" % "0.1.2"```

Required:
- Scala 2.11
- HBase 1.3.1

Note! This library is in beta phase and currently under active development. 
Provided API might be changed in next release without providing full backward compatibility.
If you urgently need some features or have feedback on existing functionality feel free to contact authors in any available channel.

Below is provided set of basic examples of establishing connectivity to HBase database, storing data to table, 
extracting (querying) it in different way and deleting.

Some necessary imports.
```scala
import io.github.hbase4s._
import io.github.hbase4s.config.HBaseDefaultConfig
import io.github.hbase4s.utils.HBaseImplicitUtils._
```

**Prerequisites**. Specified table with relevant family name must already exists in HBase (it can be created via `hbase shell`)
```scala
  case class Event(index: Int, id: Long, enabled: Boolean, description: String)

  val Table = "transactions"
  val Family = "event"
```
  
Establish **connection** to HBase server, point HBaseClient to work with "transactions" table
```scala
val client = new HBaseClient(new HBaseConnection(new HBaseDefaultConfig), Table)
```

**Store** `Event` case class in HBase table, under defined above columns family. 
```scala
  val rowId = "unique-event-id"
  client.put(rowId, Event(546, 10L, enabled = true, "some description text"))
```
As you might notice family name does not pass as parameter, by default it's taken from lowercase case class name.

**Get**. Retrieve data from HBase by key and transform it to instance of `Event` case class
```scala
  val eventInDb = client.get(rowId).map(_.typed[Event].asClass)
```

This is the most efficient way to work with HBase (querying by row id), 
but there is also extensive query language that allows user build complex queries based on numerous conditions and columns.
  
**Scan**. HBase4s provides two ways of querying data from HBase table results are represented as `List` of `Event` class

1. string-based DSL
```scala
  val e1 = client.scan[String](
    "(event:description = \"some description text\") AND (event:index > int(18))"
  ).map(_.typed[Event].asClass)
```

2. Scala static type DSL
```scala
  import io.github.hbase4s.filter._
  val e2 = client.scan[String](
    c("event", "description") === "oh-oh" & c("event", "index") > 18
  ).map(_.typed[Event].asClass)
  
  require(e1 == e2)
```

Both DSLs filters translated to native java `Scan` object and should be equally efficient.
Full set of supported querying features are described in reference guide.

**Remove** by key:
```scala
  client.delete(rowId)
```

Executable examples can be found in project [hbase-examples](https://github.com/hbase4s/hbase4s-examples)

## Background

HBase does not support data types. All columns and row keys are stored as array of `bytes`. 
Java HBase client works with the same single data structure. 
It's user responsibility, properly transform your data types to and from `bytes`.  

Primary goal of HBase4s is to provides type-safe API for CRUD operations on top of Java HBase client.

There are default implicit support for the following scala types:
```String, Boolean, Short, Int, Long, Float, Double, BigDecimal```.

User can use any of specified type to store, retrieve, get or delete data from HBase. 
Relevant type transformation applied both for row key and columns values.

This list can be extended by registering relevant extractor. Feature under development (yet).

While data is storing to HBase, there are no need to pass types explicitly, they will be inference implicitly.
However, during retrieval there are no information, of what type is. 
So, API requires user to provide type information both for row key and params. Examples below. 

One of overridden version of `put` function accepts case classes as value parameter. 
Note, all fields of such case class have to be within specified above types.
    
As shown in getting started example, results of `scan, scanAll, get` functions can also be transformed to provided case class type.     

## Reference guide

To learn more about HBase4s: 

- [ ] [Reference guide](docs/reference.md)
- [ ] [Release notes](docs/release_notes.md)
- [ ] [Future plans](docs/todo.md) (unstructured list of tasks and ideas)

## Contributing

Any help appreciated. User are welcome to try, raise issue or create pull request.

## License

Source code and binaries are published under MIT License.
