# HBase4s

[![Build Status](https://travis-ci.org/vglushak-vt/hbase4s.svg?branch=develop)](https://travis-ci.org/vglushak-vt/hbase4s) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/a0092dd676154718af28f83f5309efd2)](https://www.codacy.com/app/vglushak-vt/hbase4s?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=vglushak-vt/hbase4s&amp;utm_campaign=Badge_Grade)


Scala wrapper around HBase client library that provides user-friendly type-safe API and query language to work with HBase database.
  
HBase client has extended powerful API with huge amount of different sophisticated options.

There are no intention to cover all this options with hbase4s core library. 
The goal is to build framework that will support all basic functionality and 
give user opportunity easily extend it for it's particular goals.  

MVP (minimal valuable product) functionality includes:
- support all CRUD operations with basic options (`Get`, `Scan`, `Delete`, `Put`)
- user friendly query language that cover most popular HBase filters.

Note: raise an issue if some of basic functionality was missed

## Getting started

Before starting, note!
This is not production ready library. It's currently under active development. 
Provided API can be changed from version to version without providing backward compatibility.

It's recommended to get familiar with library by pulling it locally and lookgin closely into existent tests.
They cover most of relevant business cases that are implemented in library.

Below is provided set of basic examples of establishing connection to HBase database, storing data to table, 
extracting it in different way and removing.

```scala
// hbase4s.GettingStartedTest

package hbase4s

import hbase4s.config.HBaseDefaultConfig
import hbase4s.utils.HBaseImplicitUtils._

object Example {

  case class Event(index: Int, id: Long, enabled: Boolean, description: String)

  // prerequisites. Specified table with relevant family name exists
  val Table = "transactions"
  val Family = "event"

  // establish connection to HBase server, point HBaseClient to work with transactions table
  val client = new HBaseClient(new HBaseConnection(new HBaseDefaultConfig), Table)

  val e = Event(546, 10L, enabled = true, "oh-oh")
  val rowId = "oh-oh-event"

  // store case class in hbase table, under provided columns family
  client.put(rowId, e)

  // retrieve data from HBase by key and transform it to instance of event class
  val eventInDb = client.get(rowId).map(_.typed[Event].asClass)

  // two version of querying data from HBase table
  // results are represented as List of Event class
  // string-based DSL
  val e1 = client.scan[String]("(event:description = \"oh-oh\") AND (event:index > int(18))").map(x => x.typed[Event].asClass)

  // scala static type DSL
  import hbase4s.filter.FilterDsl._
  val e2 = client.scan[String](
    c("event", "description") === "oh-oh" & c("event", "index") > 18
  ).map(x => x.typed[Event].asClass)
  
  require(e1 == e2)

  // remove by key
  client.delete(rowId)
}
```

## CRUD

HBase does not support data types. All columns and row key are stored as array of `bytes`. 
Java HBase client works with the same single data structure. 
It's user responsibility, properly transform your data types to and from `bytes`.  

Primary goal of HBase4s is to provides type-safe API for CRUD operations on top of Java HBase client.

There are default implicit support for the following scala types:
```String, Boolean, Short, Int, Long, Float, Double, BigDecimal```.

This list can be extended by registering relevant extractor. Feature under development (yet).

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



