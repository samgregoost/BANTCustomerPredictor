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
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Customer {

    public static TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private String companyIndex;
    private int downloadActivityCount;
    private int whitePaperActivityCount;
    private int tutorialActivityCount;
    private int workshopActivityCount;
    private int caseStudiesActivityCount;
    private int productPagesActivityCount;
    private int otherActivityCount;
    private int seniorTitleCount;
    private int juniorTitleCount;
    private String companyName;
    private boolean isCustomer;
    private String joinedDate;
    private ArrayList<Date> activityTimeStamps;
    private ArrayList<String> countries;
    private boolean isFortune;


    public Customer(){
        activityTimeStamps = new ArrayList<Date>();
        countries = new ArrayList<String>();
    }

    public void addCountry(String countryName)
    {
        countries.add(countryName);
    }

    /**
     * To add particular activity time stamp to country activity array
     * @param timeStamp
     */
    public void addActivityTimeStamp(Date timeStamp) {
        activityTimeStamps.add(timeStamp);
        Collections.sort(activityTimeStamps);
    }

    /**
     * Provide top repeated countries
     * @param count how many countries required as out put
     * @return array of top countries
     */
    public String [] getTopCountries(int count) {
        ArrayList<Country> uniqueCountries = new ArrayList<Country>();
        String [] topCountries;

        if ( countries.size() < count) {
            count = countries.size();
        }

        topCountries = new String[count];

            if (countries.size() > 0) {

                for (int i = 0; i < countries.size(); i++) {
                    int index = this.getCountryIndex(uniqueCountries, countries.get(i));

                    if (index == -1) {
                        Country newCountry = new Country();

                        newCountry.setCountry(countries.get(i));
                        newCountry.setCount(1);

                        uniqueCountries.add(newCountry);
                    } else {
                        uniqueCountries.get(index).setCount(uniqueCountries.get(index).getCount() + 1);
                    }

                }

                Collections.sort(uniqueCountries, new Comparator<Country>() {
                    public int compare(Country c1, Country c2) {

                        if (c1.getCount() <= c2.getCount()) {
                            return c1.getCount();

                        } else {
                            return c2.getCount();
                        }
                    }
                });

                for (int i = 0; i < topCountries.length; i++) {
                    if (uniqueCountries.size() > i) {

                        topCountries[i] = uniqueCountries.get(i).getCountry();
                    } else {
                        topCountries[i] = "";
                    }
                }
            }

            return topCountries;
    }

    /**
     * Search specified country is existing in the list
     * @param uniqueCountries list of unique countries
     * @param countryName searching country name
     * @return index of the searched value if exists
     */
    private int getCountryIndex(ArrayList<Country> uniqueCountries , String countryName) {
            for (int i = 0; i < uniqueCountries.size(); i++) {

                if (uniqueCountries.get(i).getCountry().equals(countryName.trim())) {
                    return i;
                }
            }

            return -1;
    }

    /**
     * Provide median time between activities
     * @return median time
     */
    public long getMedianTimeBetweenTwoActivities() {

        long median = 0;
        long [] timeIntervals = this.constructIntervals();


            if (timeIntervals != null && timeIntervals.length > 0) {

                if (timeIntervals.length % 2 == 0) {
                    median = (timeIntervals[timeIntervals.length / 2] + timeIntervals[timeIntervals.length / 2 - 1]) / 2;
                } else {
                    median = timeIntervals[timeIntervals.length / 2];
                }
            }
        return  median;
    }

    /**
     * Provide maximum time between two acitivities
     * @return maximum time
     */
    public long getMaxTimeBetweenTwoActivities() {
            long[] timeIntervals = this.constructIntervals();

            if (timeIntervals != null && timeIntervals.length > 0) {
                //Return last element which is the highest
                return timeIntervals[timeIntervals.length - 1];
            } else {
                return 0;
            }
    }

    /**
     * Construct Intervals based on activity time stamps
     * @return Interval array
     */
    private long[] constructIntervals() {
        long[] timeIntervals = null;

            if (activityTimeStamps.size() > 0) {

                timeIntervals = new long[activityTimeStamps.size() - 1];

                for (int i = 0; i < timeIntervals.length; i++) {
                    timeIntervals[i] = getDateDiff(activityTimeStamps.get(i), activityTimeStamps.get(i + 1), Customer.TIME_UNIT);
                }
                Arrays.sort(timeIntervals);
            }
        return timeIntervals;

    }

    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        return timeUnit.convert( date2.getTime() - date1.getTime() , TimeUnit.MILLISECONDS);
    }

    /**
     * Converts true and false booleans to 1 and 0
     * @param input
     * @return
     */
    public static String booleanToString(Boolean input) {
        if (input == null) {
            return "0";
        } else if (input.booleanValue()) {
            return  "1";
        }
        else {
            return "0";
        }
    }

    /**
     * Converts 1 and 0 string to boolean
     * @param input
     * @return
     */
    public static boolean stringToBoolean(String input)
    {
        if (input.trim().equals("1")) {
            return true;
        }
        else {
            return false;
        }
    }



    public String getCompanyIndex() {

        return companyIndex;
    }

    public void setCompanyIndex(String companyIndex) {

        this.companyIndex = companyIndex;
    }

    public int getDownloadActivityCount() {

        return downloadActivityCount;
    }

    public void setDownloadActivityCount(int downloadActivityCount) {
        this.downloadActivityCount = downloadActivityCount;
    }

    public int getWhitePaperActivityCount() {

        return whitePaperActivityCount;
    }

    public void setWhitePaperActivityCount(int whitePaperActivityCount) {
        this.whitePaperActivityCount = whitePaperActivityCount;
    }

    public int getTutorialActivityCount() {
        return tutorialActivityCount;
    }

    public void setTutorialActivityCount(int tutorialActivityCount) {
        this.tutorialActivityCount = tutorialActivityCount;
    }

    public int getWorkshopActivityCount() {
        return workshopActivityCount;
    }

    public void setWorkshopActivityCount(int workshopActivityCount) {
        this.workshopActivityCount = workshopActivityCount;
    }

    public int getCaseStudiesActivityCount() {
        return caseStudiesActivityCount;
    }

    public void setCaseStudiesActivityCount(int caseStudiesActivityCount) {
        this.caseStudiesActivityCount = caseStudiesActivityCount;
    }

    public int getProductPagesActivityCount() {
        return productPagesActivityCount;
    }

    public void setProductPagesActivityCount(int productPagesActivityCount) {
        this.productPagesActivityCount = productPagesActivityCount;
    }

    public int getOtherActivityCount() {

        return otherActivityCount;
    }

    public void setOtherActivityCount(int otherActivityCount) {

        this.otherActivityCount = otherActivityCount;
    }

    public int getSeniorTitleCount() {

        return seniorTitleCount;
    }

    public void setSeniorTitleCount(int seniorTitleCount) {

        this.seniorTitleCount = seniorTitleCount;
    }

    public int getJuniorTitleCount() {

        return juniorTitleCount;
    }

    public void setJuniorTitleCount(int juniorTitleCount) {

        this.juniorTitleCount = juniorTitleCount;
    }

    public String getCompanyName() {

        return companyName;
    }

    public void setCompanyName(String companyName) {

        this.companyName = companyName;
    }

    public boolean getIsCustomer() {

        return isCustomer;
    }

    public void setIsCustomer(boolean isCustomer) {

        this.isCustomer = isCustomer;
    }

    public String getJoinedDate() {

        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {

        this.joinedDate = joinedDate;
    }

    public ArrayList<Date> getActivityTimeStamps() {

        return activityTimeStamps;
    }

    public boolean getIsFortune() {
        return isFortune;
    }

    public void setIsFortune(boolean isFortune) {
        this.isFortune = isFortune;
    }
}
