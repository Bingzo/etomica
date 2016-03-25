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

import org.apache.batik.i18n.LocalizableSupport;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * This class manages the message for the bridge module.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: Messages.java,v 1.2 2014/12/05 21:15:22 andrew Exp $
 */
public class Messages {

    /**
     * This class does not need to be instantiated.
     */
    protected Messages() { }

    /**
     * The error messages bundle class name.
     */
    protected static final String RESOURCES =
        "etomica.virial.cluster2.ui.rasterizer.resources.Messages";

    /**
     * The localizable support for the error messages.
     */
    protected static LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES, Messages.class.getClassLoader());

    /**
     * Implements {@link org.apache.batik.i18n.Localizable#setLocale(Locale)}.
     */
    public static void setLocale(Locale l) {
        localizableSupport.setLocale(l);
    }

    /**
     * Implements {@link org.apache.batik.i18n.Localizable#getLocale()}.
     */
    public static Locale getLocale() {
        return localizableSupport.getLocale();
    }

    /**
     * Implements {@link
     * org.apache.batik.i18n.Localizable#formatMessage(String,Object[])}.
     */
    public static String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        return localizableSupport.formatMessage(key, args);
    }

    public static String get(String key)
        throws MissingResourceException {
        return formatMessage(key, null);
    }

    public static String get(String key, String def){
        String value = def;
        try{
            value  = get(key);
        }catch(MissingResourceException e){
        }

        return value;
    }
}
