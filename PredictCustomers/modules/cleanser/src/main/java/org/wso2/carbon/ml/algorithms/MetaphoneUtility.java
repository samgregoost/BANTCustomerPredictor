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
import org.apache.commons.codec.language.Metaphone;

public class MetaphoneUtility {

    private static Metaphone metaphone = new Metaphone();

    public static String Convert(String input) throws EncoderException {

        return metaphone.encode(input);
    }

    public static String StringConvert(String input) throws EncoderException {

        return metaphone.metaphone(input);
    }

    public static int getMaxCodeLen() throws EncoderException {

        return metaphone.getMaxCodeLen();
    }

    public static void setMaxCodeLen(int length) throws EncoderException {

        metaphone.setMaxCodeLen(length);
    }


}