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

package org.wso2.carbon.ml.algorithms;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.model.Constant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomMatchingUtility {

    private static final Log logger = LogFactory.getLog(CustomMatchingUtility.class);
    static List<String> companySuffixList = new ArrayList<String>();

    public void setCompanySuffix(String companySuffix) {
        companySuffixList.add(companySuffix);
    }


    public CustomMatchingUtility() {
        loadCompanySuffixFromCsv(CustomMatchingUtility.class.getClassLoader()
                                 .getResource("companySuffix.csv").getPath());

        try {
            DoubleMetaphoneUtility.setMaxCodeLen(20);
        } catch (Exception e) {
            logger.error("Exception occurred  when initializing DoubleMetaphone utility" + e.getMessage());
        }

    }

    /**
     * Convert given string with predefined phonetic algorithm
     *
     * @param input
     * @param algorithmIndex
     * @return phonetic index for input string
     * @throws EncoderException
     */
    public String Convert(String input, int algorithmIndex) throws Exception {

        String encodedValue = "";

        input = removeCompanyNameSuffix(input.toLowerCase().trim());

        if (input.length() > Constant.MIN_INDEX_LENGTH) {

            switch (algorithmIndex) {
                case Constant.INDEX_ALGO_BEIDER_MORSE:
                    encodedValue = BeiderMorseUtility.Convert(input);
                    break;
                case Constant.INDEX_ALGO_DOUBLE_META_PHONE:
                    encodedValue = DoubleMetaphoneUtility.Convert(input);
                    break;
                case Constant.INDEX_ALGO_MATCH_RATING:
                    encodedValue = MatchRatingApproachUtility.Convert(input);
                    break;
                case Constant.INDEX_ALGO_META_PHONE:
                    encodedValue = MetaphoneUtility.Convert(input);
                    break;
                case Constant.INDEX_ALGO_SOUNDEX:
                    encodedValue = SoundexMatchUtility.Convert(input);
                    break;
            }

        } else {
            encodedValue = input;
        }

        return encodedValue;
    }

    /**
     * Remove company suffix from given input string.
     *
     * @param input
     * @return
     */
    private String removeCompanyNameSuffix(String input) {
        StringBuffer result = new StringBuffer();

        String[] splitedCompanyName = input.split("\\s+");

        for (int i = 0; i < splitedCompanyName.length; i++) {

            for (int j = 0; j < companySuffixList.size(); j++) {
                if (splitedCompanyName[i].toUpperCase().equals(companySuffixList.get(j).toUpperCase())) {
                    splitedCompanyName[i] = "";
                }
            }

            result.append(splitedCompanyName[i]);
            result.append(" ");
        }

        String companyWithOutSuffix = result.toString();

        return companyWithOutSuffix.trim();

    }

    /**
     * Load company names to a list from given csv file path
     *
     * @param csvFilePath
     */
    public void loadCompanySuffixFromCsv(String csvFilePath) {

        String[] nextLine;

        try {

            CSVReader reader = new CSVReader(
                    new InputStreamReader(new FileInputStream(csvFilePath), "UTF-8"), ',',
                    CSVReader.DEFAULT_QUOTE_CHARACTER, CSVReader.DEFAULT_QUOTE_CHARACTER);

            while ((nextLine = reader.readNext()) != null) {
                setCompanySuffix(nextLine[0].toString().toLowerCase());
            }

            reader.close();

        } catch (UnsupportedEncodingException e) {
            logger.error("Encoding exception" + e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("Suffix file is missing" + e.getMessage());
        } catch (IOException e) {
            logger.error("Exception occurred reading suffix file" + e.getMessage());
        }

    }

    /**
     * return company suffix array list
     * @return
     */
    public List<String> getCompanySuffixList() {
        return companySuffixList;
    }


}
