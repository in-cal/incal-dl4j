package org.incal.dl4j

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.incal.core.util.ReflectionUtil.getCaseClassMemberNamesAndValues
import org.incal.core.util.writeStringAsStream
import org.deeplearning4j.nn.conf.{ComputationGraphConfiguration, ConvolutionMode, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers._
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.evaluation.EvaluationAveraging
import org.nd4j.evaluation.classification.{Evaluation, ROC}
import org.nd4j.evaluation.curves.{PrecisionRecallCurve, RocCurve}
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.impl.{LossBinaryXENT, LossMCXENT}
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

trait DL4JHelper {

  protected val log = LoggerFactory.getLogger("DL4J")

  private val outputEvalHeader = Seq(
    "epoch", "trainingAccuracy", "trainingMacroF1", "trainingMicroF1", "validationAccuracy", "validationMacroF1", "validationMicroF1"
  ).mkString(", ")

  private val binaryOutputEvalHeader = Seq(
    "epoch", "trainingAccuracy", "trainingF1Class0", "trainingF1Class1", "trainingAUROC", "trainingAUPR",
    "validationAccuracy", "validationF1Class0", "validationF1Class1", "validationAUROC", "validationAUPR"
  ).mkString(", ")

  private val dateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

  def run(input: TimeSeriesClassificationSpec) = {
    val featuresDir = "file:////" + input.featuresBaseDir
    val expectedOutputDir = "file:////" + input.expectedOutputBaseDir

    // training data set
    val trainingData = TimeSeriesDataSetIterator(
      input.batchSize, input.outputNum, featuresDir, expectedOutputDir, input.trainingStartIndex, input.trainingEndIndex, 1
    )

    // validation data set
    val validationData = TimeSeriesDataSetIterator(
      input.batchSize, input.outputNum, featuresDir, expectedOutputDir, input.validationStartIndex, input.validationEndIndex, 1
    )

    // build a CNN model
    log.info("Building a CNN model....")
    val config = createCNN1D(
      input.numRows, input.numColumns, input.outputNum, input.learningRate, input.kernelSize, input.poolingKernelSize, input.convolutionFeaturesNums, input.dropOut, input.lossClassWeights
    )

    // launch and report the results
    launchAndReportResults(config, trainingData, validationData, input.numEpochs, input.outputNum, input.resultsExportDir, Some(input))
  }

  def run(input: TimeSeriesClassificationWithRandomSplitSpec) = {
    val featuresDir = "file:////" + input.featuresBaseDir
    val expectedOutputDir = "file:////" + input.expectedOutputBaseDir

    // training and validation data sets
    val (trainingData, validationData) = TimeSeriesDataSetIterator(
      input.batchSize, input.outputNum, featuresDir, expectedOutputDir, input.startIndex, input.endIndex, input.trainingValidationSplitRatio , 1
    )

    // build a CNN model
    log.info("Building a CNN model....")
    val config = createCNN1D(
      input.numRows, input.numColumns, input.outputNum, input.learningRate, input.kernelSize, input.poolingKernelSize, input.convolutionFeaturesNums, input.dropOut, input.lossClassWeights
    )

    // launch and report the results
    launchAndReportResults(config, trainingData, validationData, input.numEpochs, input.outputNum, input.resultsExportDir, Some(input))
  }

  def createCNN1D(
    numRows: Int,
    numColumns: Int,
    outputNum: Int,
    learningRate: Double,
    kernelSize: Int,
    poolingKernelSize: Int,
    convolutionFeaturesNums: Seq[Int],
    dropOut: Double,
    lossClassWeights: Seq[Double] = Nil
  ): ComputationGraphConfiguration = {

    def convolution(out: Int, channels: Int) =
      new ConvolutionLayer.Builder(kernelSize, channels)
        .stride(1, 1)
        .nOut(out)
        .activation(Activation.RELU)
        .build()

    def maxPoolingType =
      new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(poolingKernelSize, 1)
        .stride(poolingKernelSize, 1)
        .build()

    // Input shape: [batch,                1,   numRows, numColumns]
    //              [batch, channels / depth,    height,      width]

    // basic learning setting
    val conf = new NeuralNetConfiguration.Builder()
      .weightInit(WeightInit.XAVIER)
    //            .updater(new Nesterovs(rate, 0.9))
      .updater(new Adam(learningRate))
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .convolutionMode(ConvolutionMode.Truncate)
      .graphBuilder()
      .addInputs("0")

    val cnnLayersNum = convolutionFeaturesNums.size

    // convolution + max pooling layers
    convolutionFeaturesNums.zipWithIndex.foreach { case (featuresNum, index) =>
      val initChannels = if (index == 0) numColumns else 1

      conf
        .layer(3 * index + 1, convolution(featuresNum, initChannels), (3 * index).toString)
        .layer(3 * index + 2, convolution(featuresNum, 1), (3 * index + 1).toString)

      if (index < cnnLayersNum - 1) {
        conf.layer(3 * index + 3, maxPoolingType, (3 * index + 2).toString)
      }
    }

    // global average pooling
    conf
      .layer(3 * cnnLayersNum, new GlobalPoolingLayer.Builder()
        .poolingType(PoolingType.AVG)
        .build(), (3 * cnnLayersNum - 1).toString)

    //      .layer(3 * cnnLayersNum + 1, new DropoutLayer.Builder(0.5)
    //        .build(), (3 * cnnLayersNum).toString)

    // final (dense) output layer + dropout
    val lossFunction = if (outputNum == 2)
      if (lossClassWeights.nonEmpty)
        new LossBinaryXENT(Nd4j.create(lossClassWeights.toArray))
      else
        new LossBinaryXENT()
    else
    if (lossClassWeights.nonEmpty)
      new LossMCXENT(Nd4j.create(lossClassWeights.toArray))
    else
      new LossMCXENT()

    val activation = if (outputNum == 2) Activation.SIGMOID else Activation.SOFTMAX

    conf
      .layer("output", new OutputLayer.Builder(lossFunction)
        .dropOut(dropOut)
        .nOut(outputNum)
        .activation(activation)
        .build(), (3 * cnnLayersNum).toString)
      .setOutputs("output")
      .setInputTypes(InputType.convolutional(numRows, numColumns, 1)) // height, width, depth
      .build()
  }

  protected def launchAndReportResults[T: TypeTag: ClassTag](
    config: ComputationGraphConfiguration,
    trainData: DataSetIterator,
    validationData: DataSetIterator,
    numEpochs: Int,
    outputNum: Int,
    exportDir: String,
    inputsToReport: Option[T] = None
  ) = {
    val startTime = new java.util.Date()
    val startTimeString = dateTimeFormat.format(startTime)

    // create a model and initialize
    val model = new ComputationGraph(config)
    model.init()

    // Input as string
    val inputParams = inputsToReport.map { input =>
      val params = getCaseClassMemberNamesAndValues(input).map { case (name, value) =>
       val valueString = if (value.isInstanceOf[Traversable[_]])
         value.asInstanceOf[Traversable[_]].mkString(", ")
        else
          value.toString

        s"$name: $valueString"
      }.toSeq.sorted
      log.info("Input: " + params.mkString(", "))
      params
    }.getOrElse(Nil)

    // Train the model
    log.info("Training the model....")

    // Aux function to evaluate the performance
    def evaluate(
      data: DataSetIterator,
      dataSetName: String
    ): (Evaluation, Option[RocCurve], Option[PrecisionRecallCurve]) =
      if (outputNum == 2) {
        val eval = new Evaluation()
        val roc = new ROC()
        model.doEvaluation(data, eval, roc)
        val rocCurve = roc.getRocCurve
        val prCurve = roc.getPrecisionRecallCurve

        log.info(s"$dataSetName accuracy: ${eval.accuracy()}")
        log.info(s"$dataSetName AUROC   : ${rocCurve.calculateAUC()}")
        log.info(s"$dataSetName AUPR    : ${prCurve.calculateAUPRC()}")

        (eval, Some(rocCurve), Some(prCurve))
      } else {
        val eval: Evaluation = model.evaluate(data)

        log.info(s"$dataSetName accuracy: ${eval.accuracy()}")

        (eval, None, None)
      }

    val trainValidationEvals = for (i <- 1 to numEpochs) yield {
      model.fit(trainData)
      log.info(s"*** Completed epoch $i ***")

      val trainingEval = evaluate(trainData, "Training  ")
      val validationEval = evaluate(validationData, "Validation")
      (trainingEval, validationEval)
    }

    log.info("*** Training finished ***")

    // Write input params to a file
    if (inputParams.nonEmpty) {
      val paramsFileName = s"CNN_DL4J-$startTimeString-input"

      writeStringAsStream(
        inputParams.mkString("\n"),
        new java.io.File(exportDir + "/" + paramsFileName)
      )
    }

    // Write selected evaluation metrics to a file
    val evalOutputFileName = s"CNN_DL4J-$startTimeString-evals.csv"

    val evalOutput = trainValidationEvals.zipWithIndex.map { case (((trainingEval, trainingRocCurve, trainingPrCurve), (validationEval, validationRocCurve, validationPrCurve)), index) =>
      val trainingAccuracy = trainingEval.accuracy

      val validationAccuracy = validationEval.accuracy

      if (outputNum == 2) {
        val trainingF1Class0 = trainingEval.f1(0)
        val trainingF1Class1 = trainingEval.f1(1)
        val trainingAUROC = trainingRocCurve.map(_.calculateAUC().toString).getOrElse("")
        val trainingAUPR = trainingPrCurve.map(_.calculateAUPRC().toString).getOrElse("")

        val validationF1Class0 = validationEval.f1(0)
        val validationF1Class1 = validationEval.f1(1)
        val validationAUROC = validationRocCurve.map(_.calculateAUC().toString).getOrElse("")
        val validationAUPR = validationPrCurve.map(_.calculateAUPRC().toString).getOrElse("")

        Seq(index + 1, trainingAccuracy, trainingF1Class0, trainingF1Class1, trainingAUROC, trainingAUPR, validationAccuracy, validationF1Class0, validationF1Class1, validationAUROC, validationAUPR).mkString(", ")
      } else {
        val trainingMacroF1 = trainingEval.f1(EvaluationAveraging.Macro)
        val trainingMicroF1 = trainingEval.f1(EvaluationAveraging.Micro)

        val validationMacroF1 = validationEval.f1(EvaluationAveraging.Macro)
        val validationMicroF1 = validationEval.f1(EvaluationAveraging.Micro)

        Seq(index + 1, trainingAccuracy, trainingMacroF1, trainingMicroF1, validationAccuracy, validationMacroF1, validationMicroF1).mkString(", ")
      }
    }.mkString("\n")

    val evalHeader = if (outputNum == 2) binaryOutputEvalHeader else outputEvalHeader

    writeStringAsStream(
      evalHeader + "\n" + evalOutput,
      new java.io.File(exportDir + "/" + evalOutputFileName)
    )

    // Write the full final evaluation stats to a file
    val lastFullEvalOutputFileName = s"CNN_DL4J-$startTimeString-lasteval"

    val ((lastTrainingEval, lastTrainingRocCurve, lastTrainingPrCurve), (lastValidationEval, lastValidationRocCurve, lastValidationPrCurve)) = trainValidationEvals.last

    def rocAsString(curve: Option[RocCurve]): String =
      curve.map(curve => "\n\nFPR: " + curve.getFpr.mkString(",") + "\n" + "TPR: " + curve.getTpr.mkString(",") + "\n\n").getOrElse("")

    def prAsString(curve: Option[PrecisionRecallCurve]): String =
      curve.map(curve => "Precision: " + curve.getPrecision.mkString(",") + "\n" + "Recall: " + curve.getRecall.mkString(",") + "\n").getOrElse("")

    writeStringAsStream(
      "Training:" +
      lastTrainingEval.stats + rocAsString(lastTrainingRocCurve) + prAsString(lastTrainingPrCurve) + "\n\n" +
      "Validation:" +
      lastValidationEval.stats + rocAsString(lastValidationRocCurve) + prAsString(lastValidationPrCurve),
      new java.io.File(exportDir + "/" + lastFullEvalOutputFileName)
    )
  }
}