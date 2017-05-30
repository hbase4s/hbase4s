# HBase4s

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

```"io.github.hbase4s" %% "hbase4s-core" % "0.1.1"```

Required:
- Scala 2.11

Note! This is not production ready library. It's currently under active development. 
Provided API might be changed in next release without providing backward compatibility.

Below is provided set of basic examples of establishing connection to HBase database, storing data to table, 
extracting (querying) it in different way and removing.

```scala
import io.github.hbase4s._
import io.github.hbase4s.config.HBaseDefaultConfig
import io.github.hbase4s.utils.HBaseImplicitUtils._

object Example1 {

  case class Event(index: Int, id: Long, enabled: Boolean, description: String)

  // prerequisites. Specified table with relevant family name exists
  val Table = "transactions"
  val Family = "event"

  // establish connection to HBase server, point HBaseClient to work with "transactions" table
  val client = new HBaseClient(new HBaseConnection(new HBaseDefaultConfig), Table)

  val e = Event(546, 10L, enabled = true, "some description text")
  val rowId = "unique-event-id"

  // store case class in hbase table, under provided columns family
  client.put(rowId, e)

  // retrieve data from HBase by key and transform it to instance of event class
  val eventInDb = client.get(rowId).map(_.typed[Event].asClass)

  // two version of querying data from HBase table
  // results are represented as List of Event class
  // string-based DSL
  val e1 = client.scan[String](
    "(event:description = \"some description text\") AND (event:index > int(18))"
  ).map(_.typed[Event].asClass)

  // scala static type DSL
  import io.github.hbase4s.filter._
  val e2 = client.scan[String](
    c("event", "description") === "oh-oh" & c("event", "index") > 18
  ).map(_.typed[Event].asClass)
  
  require(e1 == e2)

  // remove by key
  client.delete(rowId)
}
```

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

## Filter DSL

HBase does not support SQL-like query language. 

However, it provides Filter API, that allows `scan` tables individually (no joins) based on row/columns information. 

HBase4s provide two version of DSL: string based (whole filter is represented as string) or Scala DSL based (set of scala classes).

Supported filters in different DSLs with brief description.

Scala DSL|String DSL | Description 
----------------------------|----------------------------|----------------------------
`keys`|`"keys".f`|returns only the key component of each key-value
`firstKey` | `"first_key".f` |  returns only the first key-value from each row
`rowPrefix is "row_id_1"` | `"row_prefix == row_id_1".f` | Returns only rows that starts with specific prefix 
`columnPrefix is "col_name_a"` | `"column_prefix == col_name_a".f` |  Returns only columns that start with specific prefix
`columnPrefix in("col_a", "col_b")` | `"column_prefix == (col_a, col_b)".f` | The same as above
`columnLimit === 10` | `"column_limit == 10".f` | Returns only limited amount of columns in the table
`pageLimit === 10` | `"page_count == 10".f` | Returns limited amount of rows(pages) 
`stop on "row_id_181"` | `"stop_row == row_id_181".f` | Returns result when meet row with id as in filter (inclusive)
`columnName is "col_name_a"` | `"column_name == col_name_a".f` | Returns all columns with specified qualifier (without family name)
`columnValue is "some_value_b"` | `"column_value == some_value_b".f` | Returns all columns with specified value
`c("event", "name") === "Henry VIII"` | `"event:name == \"Henry VIII\"".f` | Returns rows that have column with specified value
`keys & pageLimit === 2` | `"keys AND (page_count == 2)".f` | And condition
```(rowPrefix is "r_a") / (columnPrefix is "c_b")``` | `"(row_prefix == r_a) OR (column_prefix == c_b)".f` | Or condition

## Custom types

Getting started section cover the case of automatical Object Relational Mapping via case classes for some standard data types.
As mentioned above HBase required values to be bytes array. 
Below is the sample of storing custom types.

```scala

import io.github.hbase4s._
import io.github.hbase4s.config.HBaseDefaultConfig
import io.github.hbase4s.utils.HBaseImplicitUtils._
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import io.github.hbase4s.filter._

object Example2 {

    // expected table "dates_table" exists with column family "event"
  val client = new HBaseClient(new HBaseConnection(new HBaseDefaultConfig), "dates_table")


    val id = "row_id_1"
    // records with just one field - date
    // create list of columns (fields) manually, providing family and name
    // value has to be array of bytes or one of supported type
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
    sdf.setTimeZone(TimeZone.getDefault)
    val date = sdf.format(new Date)
    val dateField = Field("event", "create_date", asBytes(date))
    
    client.putFields(id, List(dateField))
    
    val outDate = client.scan[String](c("event", "create_date") === date).map { wr =>
      wr.asString("event:create_date")
    }.head
    
    require(date == outDate)
    println(sdf.parse(outDate))
}
```

Basically, there are two options.

1. Transform it manually to byte array. 
The downside of this approach is: search columns won't be searchable with string based DSL.
Filter parser will not know how to transform those custom types to byte array and won't be able to match values.
Option: use scala API.

2. Transform it to one of supported data types. As shown in example above date can be easily represented and stored as String.

Another point to note while work with custom fields, there might be necessity to implement custom comparator in case of some specific comparison operations (Less, GreaterOrEq).

## Contributing

Any help appreciated. User are welcome to try, raise issue or create pull request.


## License

Source code and binaries are published under MIT License.
