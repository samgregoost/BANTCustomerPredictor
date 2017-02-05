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
import org.wso2.carbon.ml.model.Constant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TitleUtility {

    private static final Log logger = LogFactory.getLog(TitleUtility.class);



    static List<String> seniorTitleList = new ArrayList<String>();
    static List<String> juniorTitleList = new ArrayList<String>();
    static List<String> notReleventTitleList = new ArrayList<String>();

    public TitleUtility() {
        loadCompanySuffixFromCsv(TitleUtility.class.getClassLoader().getResource(Constant.TITLE_FILE_NAME).getPath());

    }

    /**
     * Return title category of the given title string
     * @param title
     * @return
     */
    public Integer titleClassifier(String title) {

        int returnValue = Constant.TITLE_JUNIOR;

        if (seniorTitleList.contains(title.toLowerCase())) {
            returnValue = Constant.TITLE_SENIOR;
        }

        if (juniorTitleList.contains(title.toLowerCase())) {
            returnValue = Constant.TITLE_JUNIOR;
        }

        if (notReleventTitleList.contains(title.toLowerCase())) {
            returnValue = Constant.TITLE_NOT_RELEVANT;
        }


        return returnValue;

    }

    /**
     * Populate company suffixes from given property csv to array lists
     * @param csvFilePath
     */
    public void loadCompanySuffixFromCsv(String csvFilePath) {

        String[] nextLine;

        try {

            CSVReader reader = new CSVReader(
                    new InputStreamReader(new FileInputStream(csvFilePath), "UTF-8"), ',',
                    CSVReader.DEFAULT_QUOTE_CHARACTER, CSVReader.DEFAULT_QUOTE_CHARACTER);

            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                seniorTitleList.add(nextLine[0].toString().toLowerCase());
                juniorTitleList.add(nextLine[1].toString().toLowerCase());
                notReleventTitleList.add(nextLine[2].toString().toLowerCase());
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

}
