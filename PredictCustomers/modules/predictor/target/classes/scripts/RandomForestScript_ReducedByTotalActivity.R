#
# Random forest script for predicting potential customers 
# (Using reduced by total activity)
#

setwd("~/Documents/Machine Lerning/preprocessed data/reduced by total activity")

# Install and load required packages for decision trees and forests
library(rpart)
# install.packages('randomForest')
library(randomForest)

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

nonCustomerSubSetTrainSet <- subset(nonCustomerTrainSet, nonCustomerTrainSet$totalActivities  > 500)

# bind test data variables and training data variables to final test and data variables
finalTrainSet <- rbind(customerTrainSet,nonCustomerSubSetTrainSet)
finalTestSet  <- rbind(customerTestSet,nonCustomerTestSet)

# These csv files were generated to feed h2o and do the prediction using h2o instance
write.csv(finalTrainSet, file = "final_train_set_for_reduced_by_total_activity.csv", row.names = FALSE)
write.csv(finalTestSet, file = "final_test_set_for_reduced_by_total_activity.csv", row.names = FALSE)

# train  randomforest model using training data
model <- randomForest(as.factor(Is.Customer) ~ seniorTitleCount + juniorTitleCount + totalActivities + Max.between.2.activities + Time.since.100th.activity + downloads , data=finalTrainSet, importance=TRUE, ntree=2000)

# plot variances in feature variables
varImpPlot(model)

# predict the outcome of the testing data
prediction <- predict(model, finalTestSet, type = "response")

# write the prediction to a csv
submit <- data.frame(companyName = finalTestSet$Company.Name, IsCustomer = prediction)
write.csv(submit, file = "prediction_reduced.csv", row.names = FALSE)

# print confusion metrix
confMatrix <- confusionMatrix(prediction,finalTestSet$Is.Customer)
print(confMatrix)