package org.incal.dl4j

case class TimeSeriesClassificationWithRandomSplitSpec(
  featuresBaseDir: String,
  expectedOutputBaseDir: String,
  resultsExportDir: String,
  startIndex: Int,
  endIndex: Int,
  trainingValidationSplitRatio: Double,
  numRows: Int,
  numColumns: Int,
  outputNum: Int,
  batchSize: Int,
  numEpochs: Int,
  learningRate: Double,
  kernelSize: Int,
  poolingKernelSize: Int,
  convolutionFeaturesNums: Seq[Int],
  dropOut: Double,
  lossClassWeights: Seq[Double]
)
