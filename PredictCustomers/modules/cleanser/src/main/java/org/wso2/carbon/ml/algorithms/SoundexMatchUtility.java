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

import java.text.Normalizer;
import java.util.regex.Pattern;
import org.apache.commons.codec.language.Soundex;

public class SoundexMatchUtility {

    private  static Soundex soundex = new Soundex();

    public static String Convert(String input) throws Exception
    {
       return soundex.encode(deAccent(input.trim()));
    }

    public static Object Convert(Object input) throws Exception
    {
        return soundex.encode(input);
    }

    public static int Difference(String s1, String s2) throws Exception
    {
        return soundex.difference(s1, s2);
    }

    /**
     * Used to deAccent words to english letters
     */
    private static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}
