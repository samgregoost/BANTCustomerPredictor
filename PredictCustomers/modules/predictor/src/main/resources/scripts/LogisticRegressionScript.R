#
# Logistic regression script for predicting potential customers
#
#

rm(list=ls());

library(caTools);
set.seed(2014);

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

# generate the model using finalTrainingDataSet
marketingLog <- glm(Is.Customer ~ seniorTitleCount + juniorTitleCount + totalActivities + Max.between.2.activities + Time.since.100th.activity + downloads, data=finalTrainSet, family=binomial);

tempMin <- 1000;

# find which threshold gives minimum false positive value.
for (i in 1:1000 ) {
  marketingLogPredict <- predict(marketingLog, newdata=finalTestSet, type="response");
  logTable = table(finalTestSet$Is.Customer, marketingLogPredict > 0.001*i);
  print(0.001*i);
  print(logTable[2,1]);
  falsePositiveFrequancy <- logTable[2,1];

  if(tempMin > falsePositiveFrequancy) {
    ithIteration <- i;
    tempMin <- falsePositiveFrequancy;
  }

}

# print confusion matrix of selected threshold
logTable = table(finalTestSet$Is.Customer, marketingLogPredict > 0.001*ithIteration);
print(logTable);
