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

package org.wso2.carbon.ml.model;

public class Constant {

    //Cleanser Constants
    public static final String INDEX_COLUMN_NAME = "Index";
    public static final String INDEX_COLUMN_INPUT = "Company";
    public static final String IS_CUSTOMER_COLUMN_NAME = "Is Customer";
    public static final String IS_VALID_COUNTRY_COLUMN_NAME = "Is valid Country";
    public static final String JOINED_DATE_COLUMN_NAME = "Joined Date";
    public static final String COUNTRY_COLUMN_NAME = "Country";
    public static final String IP_COLUMN_NAME ="IpAddress";
    public static final String TITLE_COLUMN_NAME ="Title";
    public static final String LINK_COLUMN_NAME ="Link";
    public static final String FORTUNE_COLUMN_NAME = "Is Fortune 500";
    public static final int DOUBLE_META_PHONE_THRESHOLD = 10;
    public static final char CSV_SEPERATOR = ',';
    public static final String CSV_CHARACTER_FORMAT = "UTF-8";
    public static final String PROPERTY_FILE_NAME = "config.properties";
    public static final String KEY_CSV_PATH = "csvPath";
    public static final String KEY_READ_FILE = "csvReadFile";
    public static final String KEY_READ_CUSTOMER_FILE = "csvReadCustomerFile";
    public static final String KEY_COMPANY_SUFFIX_FILE = "csvCompanySuffixFile";
    public static final String KEY_TRANSFORMED_FILE = "csvWriteTransformed";
    public static final String KEY__NON_TRANSFORMED_FILE = "csvWriteNotTransformedFile";
    public static final String IGNORE_COMPANY_LIST = "ignoreCompanies.csv";

    //Aggregator Constants
    public static final String INDEX_COLUMN_INPUT_AGGREGATE = "Index";
    public static final String ACTIVITY_COLUMN_NAME = "Link";
    public static final String COMPANY_COLUMN_NAME = "Company";
    public static final String IS_FORTUNE_COLUMN_NAME = "Is Fortune 500";
    public static final String ACTIVITY_TIME_STAMP_COLUMN_NAME = "Activity date/time";
    public static final String COUNTRY_COLUMN = "Country";
    public static final String DATE_FORMAT_ACTIVITY = "MMM dd yyyy hh:mma";
    public static final String DATE_FORMAT_JOINED_DATE = "MM/DD/YYYY";
    public static final String KEY_WORD_DOWNLOADS = "downloads";
    public static final String KEY_WORD_WHITE_PAPERS = "whitepapers";
    public static final String KEY_WORD_TUTORIALS = "tutorials";
    public static final String KEY_WORD_WORKSHOPS = "workshops";
    public static final String KEY_WORD_CASE_STUDIES = "casestudies";
    public static final String KEY_WORD_PRODUCT_PAGES = "productpages";
    public static final int ACTIVITY_NUMBER = 100;
    public static final boolean SKIP_AFTER_JOIN_ACTIVITIES = false;
    public static final boolean USE_NUMERIC_FOR_BOOLEAN = true;
    public static final String KEY_CSV_AGGREGATE = "csvAggregate";
    public static final String KEY_CSV_AGGREGATE_CUSTOMERS = "csvAggregateCustomers";
    public static final String KEY_CSV_AGGREGATE_NON_CUSTOMERS = "csvAggregateNonCustomers";

    //CustomMatchingUtility Constants
    public static final int INDEX_ALGO_SOUNDEX = 1;
    public static final int INDEX_ALGO_META_PHONE = 2;
    public static final int INDEX_ALGO_DOUBLE_META_PHONE = 3;
    public static final int INDEX_ALGO_BEIDER_MORSE = 4;
    public static final int INDEX_ALGO_MATCH_RATING = 5;


    public static final int MIN_INDEX_LENGTH = 6;

    //FortuneCompaniesUtility Constants
    public static final String PROPERTY_FORTUNE_FILE_NAME = "fortune500.csv";

    //TitleUtility Constants
    public static final int TITLE_NOT_RELEVANT = 0;
    public static final int TITLE_JUNIOR = 1;
    public static final int TITLE_SENIOR = 2;
    public static final String TITLE_FILE_NAME = "titleClassifier.csv";
}
