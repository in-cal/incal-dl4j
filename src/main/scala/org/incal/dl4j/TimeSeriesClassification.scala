package org.incal.dl4j

case class TimeSeriesClassificationSpec(
  featuresBaseDir: String,
  expectedOutputBaseDir: String,
  resultsExportDir: String,
  trainingStartIndex: Int,
  trainingEndIndex: Int,
  validationStartIndex: Int,
  validationEndIndex: Int,
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
  lossClassWeights: Seq[Double] = Nil
)