#
# Random forest script for predicting potential customers 
# (Using Down sampling method)
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

# split aggregate customers in 70%, 30% ratio
splitCustomers    <- splitdf(AggregateCustomers, seed=808, splitratio = 0.7)

str(splitCustomers)
lapply(splitCustomers,nrow)
lapply(splitCustomers,head)

customerTrainSet  <- splitCustomers$trainset
customerTestSet   <- splitCustomers$testset

# split aggregate non customers in 0.7,0.3 ratio
splitNonCustomers <- splitdf(AggregateNonCustomers, seed=808, splitratio = 0.7)

str(splitNonCustomers)
lapply(splitNonCustomers,nrow)
lapply(splitNonCustomers,head)

nonCustomerTrainSet <- splitNonCustomers$trainset
nonCustomerTestSet  <- splitNonCustomers$testset

# down sample since large number of in non customer data
sampleNonCustomerTrainSet <- nonCustomerTrainSet[sample(1:nrow(nonCustomerTrainSet), 1000,replace=FALSE),]

# bind test data variables and training data variables to final test and data variables
finalTrainSet <- rbind(customerTrainSet,sampleNonCustomerTrainSet)
finalTestSet  <- rbind(customerTestSet,nonCustomerTestSet)


# These csv files were generated to feed h2o and do the prediction using h2o instance
write.csv(finalTrainSet, file = "final_train_set_for_down_sample.csv", row.names = FALSE)
write.csv(finalTestSet, file = "final_test_set_for_down_sample.csv", row.names = FALSE)

# train a randomforest model
model <- randomForest(as.factor(Is.Customer) ~ seniorTitleCount + juniorTitleCount + totalActivities + Max.between.2.activities + Time.since.100th.activity + downloads , data=finalTrainSet, importance=TRUE, ntree=1500)

# plot variance in feature variables
varImpPlot(model)

#predict the outcome of the testing data
prediction <- predict(model, finalTestSet, type = "response")

submit <- data.frame(companyName = finalTestSet$Company.Name, IsCustomer = prediction)
write.csv(submit, file = "prediction_sample_model.csv", row.names = FALSE)

# print confusion metrix
confMatrix <- confusionMatrix(prediction,finalTestSet$Is.Customer)
print(confMatrix)