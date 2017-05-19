package hbase4s.config

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
trait HBaseConfig {
  def configuration: Configuration
}

class HBaseDefaultConfig extends HBaseConfig {

  protected val conf: Configuration = HBaseConfiguration.create()

  override def configuration: Configuration = conf

}

class HBasePropsConfig(props: Map[String, String]) extends HBaseDefaultConfig {

  props.foreach { case (k, v) => conf.set(k, v) }

}

class HBaseExternalConfig(val configuration: Configuration) extends HBaseConfig

