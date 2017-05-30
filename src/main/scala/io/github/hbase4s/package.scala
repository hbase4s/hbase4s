package io.github

import io.github.hbase4s.config.HBaseConfig

/**
  * Created by Volodymyr.Glushak on 30/05/2017.
  */
package object hbase4s {

  def hBaseClient(conf: HBaseConfig, table: String) = new HBaseClient(new HBaseConnection(conf), table)

}
