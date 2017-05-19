package hbase4s.utils

import org.apache.hadoop.hbase.HBaseTestingUtility

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
object HBaseTesting {

  val hBaseServer: HBaseTestingUtility = {
    val u = new HBaseTestingUtility()
    u.getConfiguration.setBoolean("fs.hdfs.impl.disable.cache", true)
    u.startMiniCluster
    val clientPortProp = "hbase.zookeeper.property.clientPort"
    System.setProperty(clientPortProp, u.getConfiguration.get(clientPortProp))
    u
  }
}