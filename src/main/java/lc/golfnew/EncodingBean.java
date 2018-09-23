
package lc.golfnew;
/*
 * Copyright 2014 John Yeary <jyeary@bluelotussoftware.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

/**
 *
 * @author John Yeary <jyeary@bluelotussoftware.com>
 * @version 1.0
 */
@Named("encodingBean")
@SessionScoped
public class EncodingBean implements Serializable {

    private static final long serialVersionUID = -2585222706903579334L;

    private String encoding = "UTF-8";
    private final String weller = "Now is the time for all good men to come to the aid of their country";

    public EncodingBean() {
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public List<SelectItem> getItems() {
        List<SelectItem> items = new ArrayList<>();
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        Set<String> keys = charsets.keySet();
        for (String key : keys) {
            items.add(new SelectItem(key));
        }

        return items;
    }

    public String getWeller() {
        return weller;
    }

} // end class