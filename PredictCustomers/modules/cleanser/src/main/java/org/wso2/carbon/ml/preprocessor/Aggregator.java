/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.wso2.carbon.ml.preprocessor;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.model.Constant;
import org.wso2.carbon.ml.model.Customer;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

public class Aggregator {

    public static String csvPath;
    public static String csvAggregate;
    public static String csvAggregateCustomers;
    public static String csvAggregateNonCustomers;
    private static String [] headers  = {"Company Index", "Company Name", "Country 1", "Country 2", "Country 3",
                                         "Is Customer", "Joined Date", "downloads", "whitepapers", "tutorials",
                                         "workshops", "casestudies", "productpages", "other", "totalActivities",
                                         "seniorTitleCount", "juniorTitleCount","Median between two Activities",
                                         "Max between 2 activities", "Time since 100th activity","Is Fortune 500"};

    private static final Log logger = LogFactory.getLog(Cleanser.class);

    public static void main (String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        Aggregator.loadProperties();
        Cleanser.loadProperties();
        transformCsv(Cleanser.csvPath + Cleanser.csvWriteTransformed);
        long estimatedTime = System.currentTimeMillis() - startTime;
        logger.info("Time taken : "+ estimatedTime/1000 + " seconds");
    }

    /**
     * Load values from properties file
     * @throws IOException
     */
    public static void loadProperties() throws IOException {

        File file = new File(Aggregator.class.getClassLoader().getResource(Constant.PROPERTY_FILE_NAME).getPath());
        FileInputStream fileInput = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        csvPath = properties.getProperty(Constant.KEY_CSV_PATH);
        csvAggregate = properties.getProperty(Constant.KEY_CSV_AGGREGATE);
        csvAggregateCustomers = properties.getProperty(Constant.KEY_CSV_AGGREGATE_CUSTOMERS);
        csvAggregateNonCustomers = properties.getProperty(Constant.KEY_CSV_AGGREGATE_NON_CUSTOMERS);

    }

    /**
     * Aggregate given CSV File
     * @param csvFile
     * @throws IOException
     */
    public static void transformCsv (String csvFile) throws IOException {
        mapToCsv(getCsvMap(csvFile));
    }

    /**
     * Produce a hash map by given csv path
     * @param csvPath input csv
     * @return produced has map
     * @throws IOException
     */
    private static HashMap<String, Customer> getCsvMap (String csvPath) throws IOException {

        HashMap<String, Customer> csvMap = new HashMap<String, Customer>();

        CSVReader reader=new CSVReader(
                new InputStreamReader(new FileInputStream(csvPath), Constant.CSV_CHARACTER_FORMAT),
                                           Constant.CSV_SEPERATOR, CSVReader.DEFAULT_QUOTE_CHARACTER,
                                           CSVReader.DEFAULT_QUOTE_CHARACTER);

        //Read the header line
        String[] nextLine = reader.readNext();

        //Retrieve column indexes from header line
        int linkColumnIndex = Arrays.asList(nextLine).indexOf(Constant.ACTIVITY_COLUMN_NAME);
        int titleIndex  = Arrays.asList(nextLine).indexOf(Constant.TITLE_COLUMN_NAME);
        int companyNameIndex = Arrays.asList(nextLine).indexOf(Constant.COMPANY_COLUMN_NAME);
        int companyIndexColumnIndex = Arrays.asList(nextLine).indexOf(Constant.INDEX_COLUMN_INPUT_AGGREGATE);
        int isCustomerIndex = Arrays.asList(nextLine).indexOf(Constant.IS_CUSTOMER_COLUMN_NAME);
        int joinedDateIndex = Arrays.asList(nextLine).indexOf(Constant.JOINED_DATE_COLUMN_NAME);
        int activityTimeStampIndex = Arrays.asList(nextLine).indexOf(Constant.ACTIVITY_TIME_STAMP_COLUMN_NAME);
        int countryIndex = Arrays.asList(nextLine).indexOf(Constant.COUNTRY_COLUMN);
        int isFortuneIndex = Arrays.asList(nextLine).indexOf(Constant.IS_FORTUNE_COLUMN_NAME);

        try {
            while ((nextLine = reader.readNext()) != null) {

                if (nextLine.length > 0) {
                    try {
                        String companyIndex = nextLine[companyIndexColumnIndex].trim();
                        String actionsType;
                        Customer columnValues = csvMap.get(companyIndex);

                        if (columnValues == null) {
                            columnValues = new Customer();
                        }

                        if(nextLine.length > linkColumnIndex && !nextLine[linkColumnIndex].equals("")) {

                            actionsType = nextLine[linkColumnIndex].trim();

                            if (actionsType.equals("")) {
                                columnValues.setOtherActivityCount(columnValues.getOtherActivityCount() + 1);
                            } else {

                                //Check for keywords and increment counters accordingly
                                if (actionsType.contains(Constant.KEY_WORD_DOWNLOADS)) {
                                    columnValues.setDownloadActivityCount(
                                            columnValues.getDownloadActivityCount() + 1);
                                }
                                else  if (actionsType.contains(Constant.KEY_WORD_WHITE_PAPERS)) {
                                    columnValues.setWhitePaperActivityCount(
                                            columnValues.getWhitePaperActivityCount() + 1);
                                }
                                else  if (actionsType.contains(Constant.KEY_WORD_TUTORIALS)) {
                                    columnValues.setTutorialActivityCount(
                                            columnValues.getTutorialActivityCount() + 1);
                                }
                                else  if (actionsType.contains(Constant.KEY_WORD_WORKSHOPS)) {
                                    columnValues.setWorkshopActivityCount(
                                            columnValues.getWorkshopActivityCount() + 1);
                                }
                                else  if (actionsType.contains(Constant.KEY_WORD_CASE_STUDIES)) {
                                    columnValues.setCaseStudiesActivityCount(
                                            columnValues.getCaseStudiesActivityCount() + 1);
                                }
                                else  if (actionsType.contains(Constant.KEY_WORD_PRODUCT_PAGES)) {
                                    columnValues.setProductPagesActivityCount(
                                            columnValues.getProductPagesActivityCount() + 1);
                                }
                                else {
                                    columnValues.setOtherActivityCount(columnValues.getOtherActivityCount() + 1);
                                }

                                if (!companyIndex.equals(Constant.INDEX_COLUMN_INPUT_AGGREGATE)) {
                                    csvMap.put(companyIndex, columnValues);
                                }
                            }
                        }
                        else  {
                            columnValues.setOtherActivityCount(columnValues.getOtherActivityCount() + 1);
                        }

                        //Check title and increment counters accordingly
                        if( Integer.parseInt(nextLine[titleIndex]) == Constant.TITLE_SENIOR) {
                            columnValues.setSeniorTitleCount(columnValues.getSeniorTitleCount() + 1);
                        }
                        else if( Integer.parseInt(nextLine[titleIndex]) == Constant.TITLE_JUNIOR) {
                            columnValues.setJuniorTitleCount(columnValues.getJuniorTitleCount() + 1);
                        }

                        columnValues.setCompanyName(nextLine[companyNameIndex]);
                        columnValues.setIsCustomer(Customer.stringToBoolean(nextLine[isCustomerIndex]));
                        columnValues.setJoinedDate(nextLine[joinedDateIndex]);
                        columnValues.setIsFortune(Customer.stringToBoolean(nextLine[isFortuneIndex]));

                        SimpleDateFormat activityDateFormat = new SimpleDateFormat(Constant.DATE_FORMAT_ACTIVITY);
                        SimpleDateFormat joinedDateFormat = new SimpleDateFormat(Constant.DATE_FORMAT_JOINED_DATE);

                        //Check Joined Date
                        try{

                            if (!nextLine[activityTimeStampIndex].equals("")) {

                                //Retrieve date value from string with given date format
                                Date activityTimeStamp = activityDateFormat.parse(nextLine[activityTimeStampIndex]
                                                                                   .trim());

                                //If not a customer just add the existing value
                                if (!columnValues.getIsCustomer())
                                {
                                    columnValues.addActivityTimeStamp(activityTimeStamp);
                                }
                                 //Check weather skipping after join activity enables for existing customers
                                 else if (columnValues.getIsCustomer() && !Constant.SKIP_AFTER_JOIN_ACTIVITIES) {
                                     columnValues.addActivityTimeStamp(activityTimeStamp);
                                 }
                                 //If skipping is enabled but join date is after activity date for existing customers
                                 else if (columnValues.getIsCustomer() && !nextLine[joinedDateIndex].equals("") &&
                                         joinedDateFormat.parse(nextLine[joinedDateIndex]).after(activityTimeStamp)) {
                                     columnValues.addActivityTimeStamp(activityTimeStamp);
                                 }
                            }


                        columnValues.addCountry(nextLine[countryIndex]);
                        }
                        catch (ParseException ex) {
                            logger.debug("Exception occurred when parsing date time" + ex.getMessage());
                        }
                    }
                    catch (Exception ex) {
                        logger.error("Exception occurred when aggregating" + ex.getMessage());
                    }
                }
            }
        }
        catch (Exception ex) {
            logger.error("Exception occurred when creating HashMap" + ex.getMessage());
        }
        return csvMap;
    }

    /**
     * Create CSV from the given hash map
     * @param csvMap Hash map which needs to be converted to a CSV
     */
    private static void mapToCsv (HashMap<String, Customer> csvMap) {
        try {

            int totalCustomerCount = 0;
            int existingCustomerCount = 0;

            CSVWriter writerAggregate = new CSVWriter(new FileWriter(csvPath + csvAggregate),
                                                      Constant.CSV_SEPERATOR, CSVWriter.NO_QUOTE_CHARACTER);

            CSVWriter writerAggregateCustomers = new CSVWriter(new FileWriter(csvPath + csvAggregateCustomers),
                                                               Constant.CSV_SEPERATOR, CSVWriter.NO_QUOTE_CHARACTER);

            CSVWriter writerAggregateNonCustomers = new CSVWriter(new FileWriter(csvPath + csvAggregateNonCustomers),
                                                                  Constant.CSV_SEPERATOR, CSVWriter.NO_QUOTE_CHARACTER);

            //Write headers to CSV
            writerAggregate.writeNext(headers);
            writerAggregateCustomers.writeNext(headers);
            writerAggregateNonCustomers.writeNext(headers);

            for (String company : csvMap.keySet()) {
                Customer columnValues = csvMap.get(company);

                if (!company.equals("")) {

                    String[] outputLine = new String[headers.length];

                    outputLine[0] = company;
                    outputLine[1] = columnValues.getCompanyName();
                    String [] countries = columnValues.getTopCountries(3);

                     for (int i =0; i < countries.length; i++) {
                        outputLine[2 + i] = countries[i];
                     }

                    if(Constant.USE_NUMERIC_FOR_BOOLEAN) {
                        outputLine[5] = Customer.booleanToString(columnValues.getIsCustomer());
                    }
                    else {
                        outputLine[5] = String.valueOf(columnValues.getIsCustomer());
                    }

                    outputLine[6] = columnValues.getJoinedDate();
                    outputLine[7] = String.valueOf(columnValues.getDownloadActivityCount());
                    outputLine[8] = String.valueOf(columnValues.getWhitePaperActivityCount());
                    outputLine[9] = String.valueOf(columnValues.getTutorialActivityCount());
                    outputLine[10] = String.valueOf(columnValues.getWorkshopActivityCount());
                    outputLine[11] = String.valueOf(columnValues.getCaseStudiesActivityCount());
                    outputLine[12] = String.valueOf(columnValues.getProductPagesActivityCount());
                    outputLine[13] = String.valueOf(columnValues.getOtherActivityCount());
                    outputLine[14] = String.valueOf(columnValues.getDownloadActivityCount()
                                                    + columnValues.getWhitePaperActivityCount()
                                                    + columnValues.getTutorialActivityCount()
                                                    + columnValues.getWorkshopActivityCount()
                                                    + columnValues.getCaseStudiesActivityCount()
                                                    + columnValues.getProductPagesActivityCount()
                                                    + columnValues.getOtherActivityCount());
                    outputLine[15] = String.valueOf(columnValues.getSeniorTitleCount());
                    outputLine[16] = String.valueOf(columnValues.getJuniorTitleCount());
                    outputLine[17] = String.valueOf(columnValues.getMedianTimeBetweenTwoActivities());
                    outputLine[18] = String.valueOf(columnValues.getMaxTimeBetweenTwoActivities());



                    //Retrieve time from 100th activity
                    if (columnValues.getActivityTimeStamps().size() >= Constant.ACTIVITY_NUMBER) {
                        Date hundredthActivity = columnValues.getActivityTimeStamps().get(Constant.ACTIVITY_NUMBER
                                                                                            - 1);
                        Date today = new Date();
                        outputLine[19] = String.valueOf(columnValues.getDateDiff(hundredthActivity, today,
                                                                                Customer.TIME_UNIT));

                    }
                    else {
                        outputLine[19] = "0";
                    }

                    outputLine[20] = Customer.booleanToString(columnValues.getIsFortune());

                    if (columnValues.getIsCustomer()) {
                        existingCustomerCount++;
                        writerAggregateCustomers.writeNext(outputLine);
                    }
                    else {
                        writerAggregateNonCustomers.writeNext(outputLine);
                    }

                    writerAggregate.writeNext(outputLine);
                }
                totalCustomerCount++;
            }
            writerAggregate.close();
            writerAggregateCustomers.close();
            writerAggregateNonCustomers.close();

           logger.info("Total Customers : " + totalCustomerCount + " . Existing Customers : " + existingCustomerCount);

        }
        catch(Exception ex) {
            logger.error("Exception occurred when creating writing csv from HashMap" + ex.getMessage());
        }
    }


}
