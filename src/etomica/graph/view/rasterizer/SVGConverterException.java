/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package etomica.graph.view.rasterizer;

/**
 * Describes an error condition in <tt>SVGConverter</tt>
 *
 * @author <a href="mailto:vincent.hardy@sun.com">Vincent Hardy</a>
 * @version $Id: SVGConverterException.java,v 1.2 2014/12/05 21:15:22 andrew Exp $
 */
public class SVGConverterException extends Exception {
    /**
     * Error code
     */
    protected String errorCode;

    /**
     * Additional information about the error condition
     */
    protected Object[] errorInfo;

    /**
     * Defines whether or not this is a fatal error condition
     */
    protected boolean isFatal;

    public SVGConverterException(String errorCode){
        this(errorCode, null, false);
    }

    public SVGConverterException(String errorCode, 
                                  Object[] errorInfo){
        this(errorCode, errorInfo, false);
    }

    public SVGConverterException(String errorCode,
                                  Object[] errorInfo,
                                  boolean isFatal){
        this.errorCode = errorCode;
        this.errorInfo = errorInfo;
        this.isFatal = isFatal;
    }

    public SVGConverterException(String errorCode,
                                  boolean isFatal){
        this(errorCode, null, isFatal);
    }

    public boolean isFatal(){
        return isFatal;
    }

    public String getMessage(){
        return Messages.formatMessage(errorCode, errorInfo);
    }

    public String getErrorCode(){
        return errorCode;
    }
}
