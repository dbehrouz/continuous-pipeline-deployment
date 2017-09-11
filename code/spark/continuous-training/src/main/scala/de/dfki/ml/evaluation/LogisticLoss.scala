package de.dfki.ml.evaluation

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
  * @author behrouz
  */
object LogisticLoss {

  def logisticLoss(rdd: RDD[(Double, Double)]): Double = {
    val res = rdd
      .map(i => (logisticLoss(i._1, i._2), 1))
      .reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    res._1 / res._2
  }

  def logisticLoss(prediction: Double, label: Double): Double = {
    if (prediction == 0)
      prediction
    else {
      val eps = 1E-20
      -1 * (label * math.log(prediction + eps) + (1 - label) * math.log(1 - prediction))
    }
  }

  val RESULT_PATH = "data/criteo-full/temp-results"

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Logistic Regression")
    val masterURL = conf.get("spark.master", "local[*]")
    conf.setMaster(masterURL)
    val sc = new SparkContext(conf)

    val data = sc.textFile(s"$RESULT_PATH/optimizer=sgd/updater=l2-adam/iter=500/step-size=0.01-1/reg=0.0").map(parse)
    data.take(100).map(r => (r, logisticLoss(r._1, r._2))).foreach(println)
    val loss = logisticLoss(data)
    println(s"loss = $loss")
  }

  def parse(line: String): (Double, Double) = {
    val predict :: label :: other = line.split(",").map(s => s.replace("(", "").replace(")", "").toDouble).toList
    (predict, label)
  }
}
