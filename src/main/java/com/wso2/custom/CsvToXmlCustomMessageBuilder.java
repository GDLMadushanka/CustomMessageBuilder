/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.custom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A custom message builder is written to read {@link InputStream} of the axis2 transport and convert CSV data to
 * plain XML. The purpose is to use in the subsequent transformation to cater to the dynamic behaviors.
 */
public class CsvToXmlCustomMessageBuilder implements Builder {

    private List<String> columns = new ArrayList<>();

    /**
     * Read the input stream and build XML payload
     *
     * @param inputStream    axis2 transport input stream
     * @param contentType    message builder content type
     * @param messageContext axis2 message context
     * @return process message payload
     * @throws AxisFault throws if any exception occurred while processing
     */
    public OMElement processDocument(InputStream inputStream, String contentType, MessageContext messageContext)
            throws AxisFault {


        Map headersMap = (Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        String accountId = (String) headersMap.get("account-id");

        String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
        BufferedReader reader;
        String line;
        int lineNumber = 0;
        OMElement rootElement;
        try {

            // Create root element
            rootElement = AXIOMUtil.stringToOM("<root/>");

            reader = new BufferedReader(new InputStreamReader(inputStream, charSetEnc));

            // Read values from the input stream
            while ((line = reader.readLine()) != null) {

                lineNumber = lineNumber + 1;

                String[] rowValues = line.split("\\|", -1);

                if (lineNumber <= 2) {
                    continue;
                }

                // Read columns from the input stream and populate list of columns names
                if (lineNumber == 3) {
                    columns.addAll(Arrays.asList(rowValues));
                    continue;
                }

                // Create a raw element
                OMElement rawElement = AXIOMUtil.stringToOM("<raw/>");
                rootElement.addChild(rawElement);

                // Read row array which represent the raw
                for (int i = 0; i < columns.size(); i++) {
                    String column = columns.get(i);
                    String value;

                    if (i < rowValues.length) {
                        value = rowValues[i];
                    } else {
                        // ?? default value
                        value = "";
                    }

                    // Create element by column name and inject raw value
                    OMElement columnElement = AXIOMUtil.stringToOM("<" + column + "/>");
                    columnElement.setText(value);
                    rawElement.addChild(columnElement);
                }

                // Create custom element accountId which is not in the CSV payload
                OMElement accountIdElement = AXIOMUtil.stringToOM("<accountId/>");
                accountIdElement.setText(accountId);
                rawElement.addChild(accountIdElement);
            }

        } catch (UnsupportedEncodingException e) {
            throw new AxisFault("Unsupported encoding: " + charSetEnc, e);
        } catch (IOException | XMLStreamException e) {
            throw new AxisFault("Error occurred while building content: ", e);
        }

        return rootElement;
    }
}
