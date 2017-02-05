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

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.RuleType;

public class BeiderMorseUtility {

    private static BeiderMorseEncoder beiderMorse = new BeiderMorseEncoder();

    public static String Convert(String input) throws EncoderException {

        return beiderMorse.encode(input);
    }


    public static NameType getNameType() throws EncoderException {
        return beiderMorse.getNameType();
    }

    public static void setNameType(NameType nameType) throws EncoderException {

        beiderMorse.setNameType(nameType);
    }

    public static RuleType getRuleType() throws EncoderException {
        return beiderMorse.getRuleType();
    }

    public static void setRuleType(RuleType ruleType) throws EncoderException {
        beiderMorse.setRuleType(ruleType);
    }

    public static boolean isConcat() throws EncoderException {
        return beiderMorse.isConcat();
    }

    public static void setConcat(boolean concat) throws EncoderException {
        beiderMorse.setConcat(concat);
    }

    public static void setMaxPhonemes(int maxPhonemes) throws EncoderException {
        beiderMorse.setMaxPhonemes(maxPhonemes);
    }


}
