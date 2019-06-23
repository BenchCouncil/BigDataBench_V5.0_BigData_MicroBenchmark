package com.RandSample;

import org.apache.spark.{SparkConf, SparkContext}
import scala.io.Source
import org.apache.spark.util.random.RandomSampler

object RandSample {
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("RandSample")
    val sc = new SparkContext(conf)

    val textData = sc.textFile(args(0))
    textData.sample(false, args(2).toDouble, args(3).toLong).saveAsTextFile(args(1))

    sc.stop()
    }

}
