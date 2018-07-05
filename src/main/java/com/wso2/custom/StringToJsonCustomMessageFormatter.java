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
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.json.JsonUtil;

import java.io.OutputStream;
import java.net.URL;

/**
 * A custom message formatter is written to read {@link OutputStream} of the axis2 transport and convert string data to
 * JSON. The purpose is to use in a specific requirement which cannot be addressed by default message formatter for
 * JSON
 */
public class StringToJsonCustomMessageFormatter implements MessageFormatter {

    private static final Log logger = LogFactory.getLog(StringToJsonCustomMessageFormatter.class.getName());

    @Override
    public byte[] getBytes(MessageContext messageContext, OMOutputFormat omOutputFormat) throws AxisFault {
        return new byte[0];
    }

    /**
     * Removing enclosing <text></text> tags and writing the payload to output stream as a JSON.
     *
     * @param messageContext axis2 message context
     * @param omOutputFormat output format
     * @param outputStream   output stream to write the result
     * @param b
     * @throws AxisFault throws if any exception occurred while processing
     */
    @Override
    public void writeTo(MessageContext messageContext, OMOutputFormat omOutputFormat, OutputStream outputStream,
                        boolean b) throws AxisFault {
        if (messageContext == null || outputStream == null) {
            return;
        }
        // get the JSON payload enclosed with <text></text>
        OMElement element = messageContext.getEnvelope().getBody().getFirstElement();
        // get only the JSON payload removing enclosing tags
        String jsonString = element.getText();
        if (!jsonString.isEmpty()) {
            // create a JSON from the String
            JsonUtil.getNewJsonPayload(messageContext, jsonString, true, true);
            JsonUtil.setContentType(messageContext);
            // writing the JSON to output stream
            JsonUtil.writeAsJson(messageContext, outputStream);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Empty JSON payload received");
            }
            return;
        }
    }

    @Override
    public String getContentType(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        return (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
    }

    @Override
    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat omOutputFormat, URL url) throws
            AxisFault {
        if (logger.isDebugEnabled()) {
            logger.debug("#getTargetAddress. Not implemented. #getTargetAddress()");
        }
        return url;
    }

    @Override
    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        if (logger.isDebugEnabled()) {
            logger.debug("#formatSOAPAction. Not implemented. #formatSOAPAction()");
        }
        return null;
    }
}
