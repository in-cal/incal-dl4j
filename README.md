# InCal DL4J Library [![version](https://img.shields.io/badge/version-0.3.0-green.svg)](https://ada-discovery.github.io) [![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![Build Status](https://travis-ci.com/in-cal/incal-dl4j.svg?branch=master)](https://travis-ci.com/in-cal/incal-dl4j)

This is a wrapper of [Deeplearning4J library](https://deeplearning4j.org) designed especially for time-series classification and prediction using (one-dimensional) convolutional neural networks and LSTMs.

#### Example(s)

* [Classification of walking (gait) time series](src/main/scala/examples/WalkingActivityClassificationWithCNN.scala) - classification of WISDM data set into 6 activities/categories (e.g., walking, standing, and jogging) using CNNs

#### Installation

All you need is **Scala 2.11**. To pull the library you have to add the following dependency to *build.sbt*

```
"org.in-cal" %% "incal-dl4j" % "0.3.0"
```

or to *pom.xml* (if you use maven)

```
<dependency>
    <groupId>org.in-cal</groupId>
    <artifactId>incal-dl4j_2.11</artifactId>
    <version>0.3.0</version>
</dependency>
```

#### Acknowledgement

Development of this library has been significantly supported by a one-year MJFF Grant (2018-2019):
*Scalable Machine Learning And Reservoir Computing Platform for Analyzing Temporal Data Sets in the Context of Parkinson’s Disease and Biomedicine*

<a href="https://www.michaeljfox.org"><img src="https://peterbanda.net/mjff_logo.png" width="700"></a>
