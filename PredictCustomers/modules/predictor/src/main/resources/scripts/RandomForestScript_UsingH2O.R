#
# Predict Potential Customers by random forest using a h2o instance. 
# Main reason for using h2o is because it provides probability of being a customer or non customer for each prediction
# Therefore we can mannually change probability threshold and find the best fit
#

setwd("~/Documents/Machine Lerning/preprocessed data/down sample data")

# Install and load required packages for decision trees and forests
library(rpart)
# install.packages('randomForest')
library(randomForest)
library(party)

# Install required packages for confussion metrix
library(caret)
# install.packages('e1071')

# read required csv files from disk
Aggregate             <- read.csv("~/Documents/Machine Lerning/preprocessed data/Aggregate.csv")
AggregateCustomers    <- read.csv("~/Documents/Machine Lerning/preprocessed data/AggregateCustomers.csv")
AggregateNonCustomers <- read.csv("~/Documents/Machine Lerning/preprocessed data/AggregateNonCustomers.csv")

# splitdf function to divide a given data frame into given ratio
splitdf <- function(dataframe, seed=NULL, splitratio) {
  if (!is.null(seed)) set.seed(seed)
  index <- 1:nrow(dataframe)
  trainindex <- sample(index, trunc(length(index)*splitratio))
  trainset <- dataframe[trainindex, ]
  testset <- dataframe[-trainindex, ]
  list(trainset=trainset,testset=testset)
}

# split aggregate customers in 0.7, 0.3 ratio
splitCustomers    <- splitdf(AggregateCustomers, seed=808, splitratio = 0.7)

str(splitCustomers)
lapply(splitCustomers,nrow)
lapply(splitCustomers,head)

customerTrainSet  <- splitCustomers$trainset
customerTestSet   <- splitCustomers$testset

# split aggregate non customers in 0.7, 0.3 ratio
splitNonCustomers <- splitdf(AggregateNonCustomers, seed=808, splitratio = 0.7)

str(splitNonCustomers)
lapply(splitNonCustomers,nrow)
lapply(splitNonCustomers,head)

nonCustomerTrainSet <- splitNonCustomers$trainset
nonCustomerTestSet  <- splitNonCustomers$testset

# uncomment following line and replace it with nonCustomerSubSetTrainSet variable to use down sample method when choosing training set.
# sampleNonCustomerTrainSet <- nonCustomerTrainSet[sample(1:nrow(nonCustomerTrainSet), 1000,replace=FALSE),]

# select compnaies with total activities above 500
nonCustomerSubSetTrainSet <- subset(nonCustomerTrainSet, nonCustomerTrainSet$totalActivities  > 500)

# bind test data variables and training data variables to final test and data variables
finalTrainSet <- rbind(customerTrainSet,nonCustomerSubSetTrainSet)
finalTestSet  <- rbind(customerTestSet,nonCustomerTestSet)

# load h2o library for r
library(h2o)

# initialize h2o
localH2O = h2o.init()

# convert training and test data frames to h2o prased data
finalTrainSet.hex <- as.h2o(localH2O, finalTrainSet, key="finalTrainSet.hex")
finalTestSet.hex <- as.h2o(localH2O, finalTestSet, key="finalTestSet.hex")

# train random forest model using h2o
model.randomForest <- h2o.randomForest(y = "Is.Customer", x = c(14,15,16,17,18,19,20), data = finalTrainSet.hex, ntree = 1500, depth = 100)

# predict test model and save prediction to a praised data variable
prediction.hex <- h2o.predict(model.randomForest,finalTestSet.hex)

# convert h2o praised data prediction to a data frame
prediction.data.frame <- as.data.frame(prediction.hex)

numberOfRows <- nrow(prediction.data.frame)
threshold.interval    <- 0.01 # 0.13 0.011

threshold.based.prdiction <- matrix(, nrow = numberOfRows, ncol = 1)
prediction.value <- 0
tempMin <- 1000 # set this to higher value initially

numberOfIterations <- 100

for (j in 1:numberOfIterations ) {
  
  threshold <- threshold.interval*j
  
  if (j == numberOfIterations) {
    break  
  }
  
  for (i in 1:numberOfRows ) {
    
    if( prediction.data.frame[i,3] > threshold ) {
      prediction.value <- 1;
    } else {
      prediction.value <- 0;
    }
    
    threshold.based.prdiction[i,1 ] <- prediction.value
  }
  
  # confusion metrix
  confMatrix <- confusionMatrix(threshold.based.prdiction,finalTestSet$Is.Customer)
  
  confMatrixTable            <- confMatrix["table"]
  confMatrixTable.data.frame <- data.frame(matrix(unlist(confMatrixTable), nrow=4, byrow=T))
  
  print(confMatrixTable.data.frame[3,1])
  
  if(tempMin > confMatrixTable.data.frame[3,1]) {
    minimum.ithIteration <- j;
    minimum.confMatrix <- confMatrixTable;
    tempMin <- confMatrixTable.data.frame[3,1];
  }
  
}

# print minimum threshold value and confusion metrix
print(minimum.ithIteration)
print(threshold.interval*minimum.ithIteration)
print(minimum.confMatrix)




