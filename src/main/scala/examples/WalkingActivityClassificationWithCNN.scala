package examples

import org.incal.dl4j.{DL4JHelper, TimeSeriesClassificationSpec}

/**
  * This is an example of time-series classification using CNNs, more specifically a classification of accelerometer x-y-z walking series into 6 activities, such as walking and standing.
  * The original data set (WISDM) is available at <a href="http://www.cis.fordham.edu/wisdm/dataset.php">here</a>.
  *
  * Our setup follows <a href="https://blog.goodaudience.com/introduction-to-1d-convolutional-neural-networks-in-keras-for-time-sequences-3a7ff801a2cf">this blog post</a>,
  * which is recommended to be read first.
  */
object WalkingActivityClassificationWithCNN extends App with DL4JHelper {

  // download the DL4J-ready (normalized and segmented) data from https://in-cal.org/data/WISDM_DL4J.zip, unzip, and set the path bellow
  private val path = "/data_path/"

  // specification of a time-series classification
  val spec = TimeSeriesClassificationSpec(
    featuresBaseDir = path + "features" ,
    expectedOutputBaseDir = path + "activity",
    resultsExportDir = path,
    trainingStartIndex = 0,        // user [0-28] -> samples: [0-20218], user [29-35] -> samples: [20219-26558]
    trainingEndIndex = 20218,
    validationStartIndex = 20219,
    validationEndIndex = 26558,
    numRows = 80,                  // time series length
    numColumns = 3,                // number of features / channels
    outputNum = 6,                 // number of output classes
    batchSize = 400,               // batch size for each epoch
    numEpochs = 2,                // number of epochs
    learningRate = 0.001,
    kernelSize = 10,
    poolingKernelSize = 3,
    convolutionFeaturesNums = Seq(100, 160),
    dropOut = 0.5
  )

  // run a classification using the spec
  run(spec)
}