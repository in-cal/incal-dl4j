# InCal DL4J Library [![version](https://img.shields.io/badge/version-0.1.0-green.svg)](https://ada.parkinson.lu)

This is a wrapper of [Deeplearning4J library](https://deeplearning4j.org) designed especially for time-series classification using (one-dimensional) convolutional neural networks and LSTMs.

#### Installation

All you need is **Scala 2.11**. To pull the library you need to add the following dependency to *build.sbt*

```
"org.in-cal" %% "incal-dl4j" % "0.1.0"
```

or to *pom.xml* (if you use maven)

```
<dependency>
    <groupId>org.in-cal</groupId>
    <artifactId>incal-dl4j_2.11</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Example(s)

* [Classification of walking (gait) time series](src/main/scala/examples/WalkingActivityClassificationWithCNN) - classification of WISDM data set into 6 activities/categories (e.g., walking, standing, and jogging) using CNNs 

#### Acknowledgement

Development of this library has been significantly supported by a one-year MJFF Grant (2018-2019):
*Scalable Machine Learning And Reservoir Computing Platform for Analyzing Temporal Data Sets in the Context of Parkinson’s Disease and Biomedicine*

![logo](https://in-cal.org/mjff_logo.png =50x)