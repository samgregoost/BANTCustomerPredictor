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
import org.wso2.carbon.ml.algorithms.CustomMatchingUtility;
import org.wso2.carbon.ml.algorithms.DoubleMetaphoneUtility;
import org.wso2.carbon.ml.classifiers.FortuneCompaniesUtility;
import org.wso2.carbon.ml.classifiers.TitleUtility;
import org.wso2.carbon.ml.model.Constant;
import org.wso2.carbon.ml.model.Customer;
import org.wso2.carbon.ml.validations.ValidationUtility;

import java.io.*;
import java.util.*;

public class Cleanser {
    private static final Log logger = LogFactory.getLog(Cleanser.class);

    public static String csvPath;
    public static String csvReadFile;
    public static String csvReadCustomerFile;
    public static String csvCompanySuffixFile;
    public static String csvWriteTransformed;
    public static String csvWriteNotTransformedFile;

    public static String [] columnsIncluded = { "Title", "Company", "Country", "IpAddress", "Activity date/time",
                                                "Link"};

    private static boolean enableIpValidate = false;
    private static CustomMatchingUtility customMatching;
    private static TitleUtility titleUtil = new TitleUtility();
    private static FortuneCompaniesUtility fortuneUtil;

    public static void main(String[] args) {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            ArrayList<String[]> currentCustomers;
            List<String[]> ignoreCustomers;

            customMatching = new CustomMatchingUtility();
            fortuneUtil = new FortuneCompaniesUtility(Constant.INDEX_ALGO_DOUBLE_META_PHONE);

            Cleanser.loadProperties();
            DoubleMetaphoneUtility.setMaxCodeLen(Constant.DOUBLE_META_PHONE_THRESHOLD);
            customMatching.loadCompanySuffixFromCsv(csvPath + csvCompanySuffixFile);

            long startTime = System.currentTimeMillis();

            CSVReader reader=new CSVReader(
                    new InputStreamReader(new FileInputStream(csvPath + csvReadFile), Constant.CSV_CHARACTER_FORMAT),
                    Constant.CSV_SEPERATOR, CSVReader.DEFAULT_QUOTE_CHARACTER,
                    CSVReader.DEFAULT_QUOTE_CHARACTER);

            CSVReader readerCustomers=new CSVReader(
                    new InputStreamReader(new FileInputStream(csvPath + csvReadCustomerFile),
                            Constant.CSV_CHARACTER_FORMAT), Constant.CSV_SEPERATOR, CSVReader.DEFAULT_QUOTE_CHARACTER,
                            CSVReader.DEFAULT_QUOTE_CHARACTER);

            CSVReader readerIgnore = new CSVReader(
                    new InputStreamReader(new FileInputStream(
                                            classloader.getResource(Constant.IGNORE_COMPANY_LIST).getPath()),
                                            Constant.CSV_CHARACTER_FORMAT), Constant.CSV_SEPERATOR,
                                            CSVReader.DEFAULT_QUOTE_CHARACTER, CSVReader.DEFAULT_QUOTE_CHARACTER);

            ignoreCustomers = readerIgnore.readAll();

            CSVWriter writerTransformed= new CSVWriter(new FileWriter(csvPath + csvWriteTransformed),
                    Constant.CSV_SEPERATOR, CSVWriter.NO_QUOTE_CHARACTER);

            CSVWriter writerNotTransformed = new CSVWriter(new FileWriter(csvPath + csvWriteNotTransformedFile),
                    Constant.CSV_SEPERATOR, CSVWriter.NO_QUOTE_CHARACTER);


            currentCustomers = loadCurrentCustomers(readerCustomers,
                    Constant.INDEX_ALGO_DOUBLE_META_PHONE, 0);

            cleanse(reader, writerTransformed, writerNotTransformed, Constant.INDEX_COLUMN_INPUT,
                    Constant.INDEX_COLUMN_NAME, Constant.IS_CUSTOMER_COLUMN_NAME, Constant.IS_VALID_COUNTRY_COLUMN_NAME,
                    Constant.COUNTRY_COLUMN_NAME, Constant.IP_COLUMN_NAME, Constant.TITLE_COLUMN_NAME, Constant.LINK_COLUMN_NAME,
                    Constant.JOINED_DATE_COLUMN_NAME, Constant.FORTUNE_COLUMN_NAME, currentCustomers, columnsIncluded,
                    Constant.INDEX_ALGO_DOUBLE_META_PHONE, ignoreCustomers);

            long estimatedTime = System.currentTimeMillis() - startTime;
            logger.info("Time taken : "+ estimatedTime/1000 + " seconds");

            writerTransformed.close();
            writerNotTransformed.close();
            reader.close();
        }
        catch(Exception ex) {
            logger.error("Exception occurred when cleansing" + ex.getMessage());
        }
    }

    /**
     * Load values from properties file
     * @throws IOException
     */
    public static void loadProperties() throws IOException
    {
        File file = new File(Aggregator.class.getClassLoader().getResource(Constant.PROPERTY_FILE_NAME).getPath());
        FileInputStream fileInput = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        csvPath = properties.getProperty(Constant.KEY_CSV_PATH);
        csvReadFile = properties.getProperty(Constant.KEY_READ_FILE);
        csvReadCustomerFile = properties.getProperty(Constant.KEY_READ_CUSTOMER_FILE);
        csvCompanySuffixFile = properties.getProperty(Constant.KEY_COMPANY_SUFFIX_FILE);
        csvWriteTransformed = properties.getProperty(Constant.KEY_TRANSFORMED_FILE);
        csvWriteNotTransformedFile = properties.getProperty(Constant.KEY__NON_TRANSFORMED_FILE);

    }

    /**
     * Specifies weather current value is an existing customer
     * @param currentCustomers string array of customer indexes
     * @param companyIndex current index value
     * @param companyName current name value
     * @return
     */
    private static String[] isCustomer(ArrayList<String[]> currentCustomers, String companyIndex, String companyName) {
        for (int i = 0 ; i < currentCustomers.size(); i++) {
            if (currentCustomers.get(i)[0].equals(companyIndex)) {
                return currentCustomers.get(i);
            }
            else if (companyName.toUpperCase().contains(currentCustomers.get(i)[1].toUpperCase())) {
                return currentCustomers.get(i);
            }
        }

        return null;
    }

    /**
     * Load all the current customers from specified CSV read file and convert to indexes into a string array
     * @param readerCustomers input CSV file of existing customers
     * @param indexAlgorithm indexing algorithm
     * @param indexColumn indexing column index
     * @return
     * @throws Exception
     */
    private static  ArrayList<String[]> loadCurrentCustomers(CSVReader readerCustomers,
                                                              int indexAlgorithm, int indexColumn) throws  Exception {

        ArrayList<String[]> Customers = new ArrayList<String[]>();
        String [] nextLine;

        readerCustomers.readNext();

        while ((nextLine = readerCustomers.readNext()) != null) {
            try {

                String [] rowVal = new String[3];

                //Set  algorithm Index for first column
                switch (indexAlgorithm) {
                    case Constant.INDEX_ALGO_DOUBLE_META_PHONE:
                        rowVal[0] = customMatching.Convert(nextLine[indexColumn],
                                Constant.INDEX_ALGO_DOUBLE_META_PHONE);
                        break;
                    case Constant.INDEX_ALGO_META_PHONE:
                        rowVal[0] =customMatching.Convert(nextLine[indexColumn],
                                Constant.INDEX_ALGO_META_PHONE);
                        break;
                    default:
                        rowVal[0] = customMatching.Convert(nextLine[indexColumn],
                                Constant.INDEX_ALGO_SOUNDEX);
                        break;
                }

                rowVal[1] = nextLine[indexColumn].trim();
                rowVal[2] = nextLine[1].trim();

                Customers.add(rowVal);

            }
            catch (IllegalArgumentException ex) {
                //handles algorithm encode exceptions
                logger.error("Exception occurred when indexing" + ex.getMessage());
            }
        }
        return Customers;
    }

    /**
     * Check current value is an ignored company value
     * @param ignoreCustomers list of ignored company values
     * @param currentValue value needs to be checked
     * @return
     */
    private static boolean isIgnore(List<String[]> ignoreCustomers, String currentValue) {
        for (int i = 0; i < ignoreCustomers.size(); i++) {
            if (currentValue.length() < 3 ||
                ignoreCustomers.get(i)[0].trim().toUpperCase().equals(currentValue.trim().toUpperCase())) {
                return true;
            }
        }

        return  false;
    }

    /**
     *
     * Data Cleansing will be done on specified input csv and transform into specified csv files
     *
     * @param reader input csv file
     * @param writerTransformed  output transform csv file of new customer activities
     * @param writerNotTransformed output not transformed/ignored csv file
     * @param indexColumnName indexing column name by algorithm
     * @param columnsIncluded Including column names for transformation
     * @param indexAlgorithm Specified algorithm for indexing
     * @throws IOException
     */
    private static void cleanse(CSVReader reader,CSVWriter writerTransformed,
                                CSVWriter writerNotTransformed, String indexColumnName, String indexOutputColumnName,
                                String isCutomerColumnName, String isValidCountryColumnName, String countryColumnName,
                                String ipColumnName, String titleColumnName, String linkColmunName, String joinedDateColumnName,
                                String fortuneColumnName, ArrayList<String[]> currentCustomer,
                                String [] columnsIncluded, int indexAlgorithm,  List<String[]> ignoreCustomers )
                                throws Exception {
        int totalCounter = 0;
        int transformedCounter = 0;
        int currentCustomerActionCounter = 0;
        int columnIndex ;
        int countryColumnIndex;
        int ipColumnIndex;
        int titleColumnIndex;
        int generatedColumnCount;
        int linkColumnIndex;

        int [] columnIncludedIndexes = new int[columnsIncluded.length];

        ValidationUtility validator = new ValidationUtility();

        //Writing Header row for output files
        String [] nextLine = reader.readNext();
        writerNotTransformed.writeNext(nextLine);

        //Add one more column for algorithm index to specified column array and write as header
        List<String> list = new LinkedList<String>(Arrays.asList(columnsIncluded));

        list.add(0, indexOutputColumnName);
        list.add(1, isCutomerColumnName);
        list.add(2, isValidCountryColumnName);
        list.add(3, joinedDateColumnName);
        list.add(4, fortuneColumnName);

        generatedColumnCount = list.size() - columnIncludedIndexes.length;
        writerTransformed.writeNext(list.toArray(new String[indexColumnName.length()+1]));

        //Get the column index of given column name
        columnIndex = Arrays.asList(nextLine).indexOf(indexColumnName);
        countryColumnIndex = Arrays.asList(nextLine).indexOf(countryColumnName);
        ipColumnIndex = Arrays.asList(nextLine).indexOf(ipColumnName);
        titleColumnIndex = Arrays.asList(nextLine).indexOf(titleColumnName);
        linkColumnIndex = Arrays.asList(nextLine).indexOf(linkColmunName);

        for(int i =0; i < nextLine.length; i++) {
            nextLine[i] = nextLine[i].trim();
        }

        for(int i = 0; i < columnsIncluded.length; i++) {
            columnIncludedIndexes[i] = Arrays.asList(nextLine).indexOf(columnsIncluded[i]);
        }

        while ((nextLine = reader.readNext()) != null) {

            //Check read line is number of required columns and indexing column value is not empty
            if (nextLine.length >= columnIncludedIndexes.length && !(nextLine[columnIndex].equals("")) &&
                    !isIgnore(ignoreCustomers, nextLine[columnIndex])) {

                //Initialize output with specified columns plus one more column for algorithm index
                String[] outputLine = new String[columnIncludedIndexes.length + 5];

                try {
                    //Set  algorithm Index for first column
                    switch (indexAlgorithm) {
                        case Constant.INDEX_ALGO_DOUBLE_META_PHONE :
                            outputLine[0] = customMatching.Convert(nextLine[columnIndex],
                                    Constant.INDEX_ALGO_DOUBLE_META_PHONE);
                            break;
                        case Constant.INDEX_ALGO_META_PHONE :
                            outputLine[0] = customMatching.Convert(nextLine[columnIndex],
                                    Constant.INDEX_ALGO_META_PHONE);
                            break;
                        default :
                            outputLine[0] = customMatching.Convert(nextLine[columnIndex],
                                    Constant.INDEX_ALGO_SOUNDEX);
                            break;
                    }


                    if (outputLine[0] != null) {
                        boolean isExistingCustomer;
                        String  [] result = isCustomer(currentCustomer, outputLine[0], nextLine[columnIndex]);
                        boolean isValidIp = false;

                        if (result == null) {
                            isExistingCustomer = false;
                        }
                        else {
                            isExistingCustomer = true;

                            outputLine[0] = result[0];

                        }

                        outputLine[1] = Customer.booleanToString(isExistingCustomer);

                        if(enableIpValidate) {
                            isValidIp = validator.countryByIpAddressValidation(nextLine[ipColumnIndex],
                                    nextLine[countryColumnIndex]);
                        }

                        outputLine[2] = String.valueOf(isValidIp);
                        outputLine[4] = Customer.booleanToString(fortuneUtil.isFortuneCompany(outputLine[0]));



                        //Set specified columns for output
                        for (int i = generatedColumnCount;
                             i < columnIncludedIndexes.length + generatedColumnCount; i++) {
                            //Check include index is available on readLine
                            if (nextLine.length > columnIncludedIndexes[i - generatedColumnCount]) {
                                if (titleColumnIndex == columnIncludedIndexes[i - generatedColumnCount]) {
                                    outputLine[i] = titleUtil.titleClassifier(
                                            nextLine[columnIncludedIndexes[i - generatedColumnCount]]).toString();
                                }else if((linkColumnIndex == columnIncludedIndexes[i - generatedColumnCount])){

                                    if(!(nextLine[columnIncludedIndexes[i - generatedColumnCount]]).equals("")){

                                        String actionsType = nextLine[linkColumnIndex].trim();
                                        if(actionsType.equals("")){
                                            outputLine[i] = "other";
                                        }else{
                                            if (actionsType.contains(Constant.KEY_WORD_DOWNLOADS)) {
                                                outputLine[i] = "download";
                                            }
                                            else  if (actionsType.contains(Constant.KEY_WORD_WHITE_PAPERS)) {
                                                outputLine[i] = "whitepapers";
                                            }
                                            else  if (actionsType.contains(Constant.KEY_WORD_TUTORIALS)) {
                                                outputLine[i] = "tutorials";
                                            }
                                            else  if (actionsType.contains(Constant.KEY_WORD_WORKSHOPS)) {
                                                outputLine[i] = "workshops";
                                            }
                                            else  if (actionsType.contains(Constant.KEY_WORD_CASE_STUDIES)) {
                                                outputLine[i] = "caseStudies";
                                            }
                                            else  if (actionsType.contains(Constant.KEY_WORD_PRODUCT_PAGES)) {
                                                outputLine[i] = "productPages";
                                            }
                                            else {
                                                outputLine[i] = "other";
                                            }
                                        }


                                    }else{
                                        outputLine[i] = "other";
                                    }
                                }
                                else {
                                    outputLine[i] = nextLine[columnIncludedIndexes[i - generatedColumnCount]];
                                }
                            } else {
                                outputLine[i] = "";
                            }
                        }

                        transformedCounter++;

                        if (isExistingCustomer) {
                            currentCustomerActionCounter++;
                            outputLine[3] = result[2];
                            outputLine[6] = result[1];
                        }


                        writerTransformed.writeNext(outputLine);
                    }
                    else {
                        writerNotTransformed.writeNext(nextLine);
                    }
                } catch (IllegalArgumentException ex) {
                    //handles algorithm encode exceptions
                    writerNotTransformed.writeNext(nextLine);
                }

            } else {
                writerNotTransformed.writeNext(nextLine);
            }
            totalCounter++;
        }

        logger.info(totalCounter + " rows processed.  "
                + transformedCounter + " rows transformed. "
                + (totalCounter - transformedCounter)
                + " rows not transformed. Total current customer actions "
                + currentCustomerActionCounter+ ", Other actions "
                + (transformedCounter - currentCustomerActionCounter));
    }
}
