package fft

import org.apache.spark.rdd.RDD
import org.apache.spark._
import scala.collection.Iterator

import scala.io.Source

class complex(val _index:Long = 0, val _real:Double = 0, val _img:Double = 0) {
  var index:Long = _index
  var real:Double = _real
  var img:Double = _img

  def add(A:complex, B:complex) : complex = {
    this.real = A.real + B.real
    this.img = A.img + B.img
    this
  }

  def sub(A:complex, B:complex) :complex = {
    this.real = A.real - B.real
    this.img = A.img - B.img
    this
  }

  def mul(A:complex, B:complex) : complex = {
    this.real = A.real * B.real - A.img * B.img
    this.img = A.real * B.img + A.img * B.real
    this
  }

  def div(A:complex, B:complex) : complex = {
    this.real=( A.real*B.real+A.img*B.img )/( B.real*B.real+B.img*B.img);
    this.img=( A.img*B.real-A.real*B.img)/(B.real*B.real+B.img*B.img);
    this
  }

  def bitReverse(dim : Long) : complex = {
    var i : Long = 0
    var res : Long = 0
    while(i < dim) {
      if (( this.index & 1) == 1) {
        res = (res << 1) + 1
      }
      else {
        res = res << 1
      }
      this.index = this.index >>1
      i = i + 1
    }
    this.index = res
    this
  }

  override def toString() : String = {
    "("+ index.toString + "," + real.toString + "," + img.toString +")"
  }
}


object fft {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Spark FFT")
    val sc = new SparkContext(conf)

    val matrixData = sc.textFile(args(0)).zipWithIndex.map{case (line,num) => (num,line)}
    val dimension = args(2).toInt
    val size_x = scala.math.pow(2:Double,dimension.toDouble).toInt

    /*val point = matrixData.flatMapValues( value => Iterator[Array[complex]] {
        val ele = value.split(' ')
        var count:Long = -1
        val arr:Array[complex] = new Array[complex](ele.length/2)
        for(i <- (0 until ele.length) if i%2 ==0 ) {
          val first = ele(i).toDouble
          val second = ele(i+1).toDouble
          count = count+1
          val comData = new complex(count,first,second)
          arr(count.toInt) = comData
        }
        arr
      }
    ).flatMapValues(x => x)*/

    val point = matrixData.flatMapValues( value => Iterator[Array[complex]] {
      val ele = value.split(' ')
      var count:Long = -1
      val arr:Array[complex] = new Array[complex](size_x)
      for(i <- (0 until ele.length) if i%2 ==0 ) {
        val first = ele(i).toDouble
        val second = ele(i+1).toDouble
        count = count+1
        val comData = new complex(count,first,second)
        val comDataReverse = comData.bitReverse(dimension)
        arr(comDataReverse.index.toInt) = comDataReverse
      }
      for(i <- (ele.length/2 until size_x.toInt)) {
        val fillCom = new complex(i,0.0,0.0)
        val fillComReverse = fillCom.bitReverse(dimension)
        arr(fillComReverse.index.toInt) = fillComReverse
      }
      arr
    }
    ) //fill in complex element and complete bit reverse




    //for(i <-0 until size_x) { print(w(i).index,w(i).real),w(i).img}


    val fftPoint = point.flatMapValues( value => Iterator[Array[complex]] {
      //val arr:Array[complex] = new Array[complex](size_x)
      val wVec:Array[complex] = new Array[complex](size_x)
      for(i <- 0 until size_x )
      {
        wVec(i) = new complex()
        wVec(i).index = i
        wVec(i).real=Math.cos(2*Math.PI/size_x*i)
        wVec(i).img = -1* Math.sin(2*Math.PI/size_x*i)
      }
      var curPairNum:Int=0
      for (i <- 0 until dimension.toInt) {
        curPairNum = 1<<i
        for(j <- 0 until (size_x, 2*curPairNum) ) {
          for(k <- 0 until curPairNum) {
            val wMatH:complex = new complex()
            val GaddWH:complex = new complex()
            val GsubWH:complex = new complex()
            wMatH.mul(value(j+k+curPairNum), wVec(size_x*k/2/curPairNum))
            GaddWH.add(value(j+k),wMatH)
            GsubWH.sub(value(j+k),wMatH)
            value(j+k)=GaddWH
            value(j+k).index=j+k
            value(j+k+curPairNum)=GsubWH
            value(j+k+curPairNum).index=j+k+curPairNum
          }
        }
      }
      for (i <- 0 until dimension.toInt) {
        if(Math.abs(value(i).real) < 0.0001) value(i).real = 0
        if(Math.abs(value(i).img) < 0.00000000001 ) value(i).img = 0
      }
      value
    })

    val output = fftPoint.mapValues( _.toList ).saveAsTextFile(args(1))

    //print the array element of RDD(Long,Array[complex])
    //for( p <- point) { for (i <- 0 until p._2.length) {print(p._2(i).index,p._2(i).real,p._2(i).img) } }

    //print the key and value elements of RDD(Long,complex)
    //for( p <- point) {print(p._1,p._2.index,p._2.real,p._2.img)}

    //val pointReverse = point.map{ case(num:Long,com:complex) => (num,com.bitReverse(dimension)) }


    //var arr:Array[complex] = new Array[complex](dimension)

    /*for( mLine <- matrixData.getLines() ) {
      var count = 0
      val str = mLine.split(" ").toSeq

    }*/
    sc.stop()
  }

}
