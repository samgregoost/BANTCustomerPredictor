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

package org.wso2.carbon.ml.validations;

import au.com.bytecode.opencsv.CSVReader;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class ValidationUtility {

    private static final Log logger = LogFactory.getLog(ValidationUtility.class);
    private static InetAddressValidator ipAddressValidator = new InetAddressValidator();
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    final Map<String, String> ipAddressToCountryMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    final Map<String, String> countryMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    private static String ipAddressToCountryDBFileName = "ipAddressToCountryDB.csv";
    private static String countryCodeFileName          = "countryCodes.csv";

    private static Integer FirstColumnIndex  = 0;
    private static Integer SecondColumnIndex = 1;
    private static Integer ThirdColumnIndex  = 2;

    public ValidationUtility() {

        loadDataToMapFromCsv(ipAddressToCountryDBFileName, ipAddressToCountryMap,
                ValidationUtility.FirstColumnIndex, ValidationUtility.ThirdColumnIndex);
        loadDataToMapFromCsv(countryCodeFileName, countryMap, ValidationUtility.FirstColumnIndex,
                ValidationUtility.SecondColumnIndex);

    }

    /**
     * Return true if ip address matches with given country name, else return false.
     *
     * @param ipAddress
     * @param countryName
     * @return
     */
    public synchronized Boolean countryByIpAddressValidation(String ipAddress, String countryName) {

        LookupService countryLocation = null;
        Location ipLocation = null;
        String ipAddressRange;
        Boolean returnValue = null;

        try {
            countryLocation = new LookupService(classloader.getResource("GeoLiteCity.dat").getPath(), LookupService.GEOIP_CHECK_CACHE);
        } catch (IOException e) {
            logger.error(e);
        }

        try {
            if (ipAddressValidator.isValid(ipAddress)) {

                ipLocation = countryLocation.getLocation(ipAddress.trim());

                if (ipLocation != null) {
                    if (ipLocation.countryCode.equals(getCountryCode(countryName))) {
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                } else {

                    ipAddressRange = getIpAddressRange(ipAddress);

                    if (ipAddressToCountryMap.get(ipAddressRange).equals(getCountryCode(countryName))) {
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                }

            } else {
                returnValue = false;
            }
        } catch (NullPointerException e) {
            logger.error(e);
        }


        countryLocation.close();

        return returnValue;
    }

    /**
     * Return ip address range
     *
     * @param ipAddress
     * @return
     */
    private String getIpAddressRange(String ipAddress) {

        String[] splitAddress = ipAddress.split("\\.");
        splitAddress[splitAddress.length - 1] = "*";

        return StringUtils.join(splitAddress, ".");
    }

    /**
     * Return country code for given country name.
     *
     * @param country
     * @return
     */
    private String getCountryCode(String country) {
        String countryCode = countryMap.get(country);
        if (countryCode == null) {
            countryCode = "Not Found";
        }
        return countryCode;
    }


    /**
     * Populate key value pairs from csv to hash maps
     *
     * @param resourceName
     * @param map
     * @param keyCsvIndex
     * @param valueCsvIndex
     */
    public void loadDataToMapFromCsv(String resourceName, Map<String, String> map, Integer keyCsvIndex, Integer valueCsvIndex) {

        String[] nextLine;

        try {
            CSVReader reader = new CSVReader(
                    new InputStreamReader(new FileInputStream(classloader.getResource(resourceName).getPath()), "UTF-8"), ',',
                    CSVReader.DEFAULT_QUOTE_CHARACTER, CSVReader.DEFAULT_QUOTE_CHARACTER);

            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                map.put(nextLine[keyCsvIndex], nextLine[valueCsvIndex]);
            }

            reader.close();


        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }


    }


}
