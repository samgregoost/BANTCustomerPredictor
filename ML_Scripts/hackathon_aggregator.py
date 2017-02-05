from pyspark.ml import Pipeline
from pyspark.ml.classification import DecisionTreeClassifier
from pyspark.ml.feature import Binarizer
from pyspark.ml.feature import RFormula
from jpmml_sparkml import toPMMLBytes
from pyspark.shell import spark
from pyspark.ml.feature import VectorSlicer
from pyspark.ml.feature import VectorAssembler
from pyspark.ml.feature import Binarizer
from pyspark.ml.classification import RandomForestClassifier
from pyspark.ml.feature import ChiSqSelector
from pyspark.ml.feature import PCA
from pyspark.ml.evaluation import MulticlassClassificationEvaluator
from pyspark.ml.classification import MultilayerPerceptronClassifier
from pyspark.ml.classification import LogisticRegression
from pyspark.ml.classification import GBTClassifier




df = spark.read.csv("/home/sameera/Downloads/Tharik/csv/Aggregate.csv", header = True, inferSchema = True)


formula = RFormula(
    formula="Is_Customer ~ downloads+whitepapers+tutorials+workshops+casestudies+productpages+other+totalActivities+seniorTitleCount+juniorTitleCount+Median_between_two_Activities+Max_between_2_activities+Time_since_100th_activity+Is_Fortune_500",
    featuresCol="features",
    labelCol="label")

df2 = df.na.fill('unknown')



(trainingData, testData) = df2.randomSplit([0.7, 0.3])
classifier = RandomForestClassifier()
#classifier = LogisticRegression(maxIter=10, regParam=0.3, elasticNetParam=0.8);
layers = [14, 5, 4, 2]
# create the trainer and set its parameters
#classifier = MultilayerPerceptronClassifier(maxIter=100, layers=layers, blockSize=128, seed=1234)


pipeline = Pipeline(stages = [formula,classifier])
pipelineModel = pipeline.fit(trainingData)

predictions = pipelineModel.transform(testData)
evaluator = MulticlassClassificationEvaluator(
    labelCol="label", predictionCol="prediction", metricName='weightedRecall')
evaluator2 = MulticlassClassificationEvaluator(
    labelCol="label", predictionCol="prediction", metricName='weightedPrecision')


evaluator3 = MulticlassClassificationEvaluator(
    labelCol="label", predictionCol="prediction")


accuracy = evaluator.evaluate(predictions)
print("Test Error = %g" % (accuracy))
accuracy2 = evaluator2.evaluate(predictions)
print("Test Error = %g" % (accuracy2))
accuracy3 = evaluator3.evaluate(predictions)
print("Test Error = %g" % (accuracy3))

pmmlBytes = toPMMLBytes(spark, df, pipelineModel)
with open('test3.pmml','wb') as output:
    output.write( pmmlBytes)
#print(pmmlBytes)
