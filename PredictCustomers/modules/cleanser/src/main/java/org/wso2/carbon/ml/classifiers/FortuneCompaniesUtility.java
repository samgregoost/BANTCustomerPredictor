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

package org.wso2.carbon.ml.classifiers;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.algorithms.CustomMatchingUtility;
import org.wso2.carbon.ml.model.Constant;
import org.wso2.carbon.ml.preprocessor.Cleanser;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FortuneCompaniesUtility {

    private static final Log logger = LogFactory.getLog(TitleUtility.class);
    Map<String,String[]> Companies = new HashMap<String,String[]>();
    CustomMatchingUtility customMatching;

    public FortuneCompaniesUtility(int indexAlgorithm) {
        String [] nextLine;
        int indexColumn = 0;

        customMatching = new CustomMatchingUtility();

        try {

            CSVReader reader = new CSVReader(
                    new InputStreamReader(new FileInputStream(FortuneCompaniesUtility.class
                                                                .getClassLoader().getResource(
                                                                Constant.PROPERTY_FORTUNE_FILE_NAME).getPath()),
                                                                Constant.CSV_CHARACTER_FORMAT), Constant.CSV_SEPERATOR,
                                                                CSVReader.DEFAULT_QUOTE_CHARACTER,
                                                                CSVReader.DEFAULT_QUOTE_CHARACTER);

            while ((nextLine = reader.readNext()) != null) {
                try {

                    String[] rowVal = new String[1];
                    rowVal[indexColumn] = nextLine[indexColumn].trim();

                    //Set  algorithm Index for first column
                    switch (indexAlgorithm) {
                        case Constant.INDEX_ALGO_DOUBLE_META_PHONE:
                            Companies.put(customMatching.Convert(nextLine[indexColumn],
                                    Constant.INDEX_ALGO_DOUBLE_META_PHONE), rowVal);
                            break;
                        case Constant.INDEX_ALGO_META_PHONE:
                            Companies.put(customMatching.Convert(nextLine[indexColumn],
                                    Constant.INDEX_ALGO_META_PHONE), rowVal);
                            break;
                        default:
                            Companies.put(customMatching.Convert(nextLine[indexColumn],
                                    Constant.INDEX_ALGO_SOUNDEX), rowVal);
                            break;
                    }
                } catch (IllegalArgumentException ex) {
                    logger.error("Encoding exception occurred" + ex.getMessage());
                }
            }
        }
        catch (Exception ex)
        {
            logger.error("Exception occurred when loading fortune companies" + ex.getMessage());
        }
    }

    public boolean isFortuneCompany(String companyName) {
        String [] companyValues = Companies.get(companyName);

        if (companyValues != null) {
            return true;
        }
        else {
            return false;
        }
    }

}
