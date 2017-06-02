
# Reference guide

Below is description of some important features of HBase4s

## Scan DSL

HBase does not support SQL-like query language. 

However, it provides Filter API, that allows `scan` tables individually (no joins) based on row/columns information. 

HBase4s provide two version of DSL: string based (whole filter is represented as string) or Scala DSL based (set of scala classes).

Developed DSL contains of two parts: `query_expression ! query_options`

Available query options are:
- `start_row == <row_id>` - start scanning from particular row id.
- `stop_row == <row_id>` - stop scanning when reach particular row id.

Supported filters in both Scala and string based DSLs with brief description.

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
```(rowPrefix is "r_a") | (columnPrefix is "c_b")``` | `"(row_prefix == r_a) OR (column_prefix == c_b)".f` | Or condition

## Custom types (user defined types)

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

### UDT and case classes
 
There is a possibility to define and register it's own ``Encoder`` that will transform type to/from byte array.
Following steps need to be performed to achieve it:
 1. Implement trait `Encoder[T]` for your type `T`
 2. Register your implementation in global registry: `EncoderRegistry.add(T, new Encoder[T] { ... } )`
 3. Define case class with the field of type `T` and read/store it to HBase.
 
### Option support

As the part of custom types feature it was introduced support of `Option[T]` scala class.
Case classes that have `Option` as the field can be stored into HBase table or read(get, query) from it.

To query field with `Option` type user has to specify that value is `Option[T]`, where `T` is one of defined above in the manual type supported by default

For ex. following filter: `"event:description == option_str(text)".f` will look for rows with column `description` of type `Option[String]` (obviously stored into HBase as byte array) with specified value. 

Other supported Option variances: 
```option_str, option_int, option_long, option_short, option_float, option_double, option_bool, option_bigdecimal, none```
