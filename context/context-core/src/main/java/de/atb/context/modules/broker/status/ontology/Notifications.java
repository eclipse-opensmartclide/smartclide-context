package pt.uninova.context.modules.broker.status.ontology;

/*-
 * #%L
 * ATB Context Extraction Core Lib
 * %%
 * Copyright (C) 2016 - 2020 ATB
 * %%
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
 * #L%
 */


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Giovanni
 */
public class Notifications {
    
    private List<Notify> lNotification = new ArrayList<>();

    public Notifications() {
    }
    
    public Notifications(List<Notify> lNotification) {
        this.lNotification = lNotification;
    }

    public List<Notify> getlNotification() {
        return lNotification;
    }

    public void setlNotification(List<Notify> lNotification) {
        this.lNotification = lNotification;
    }
    
    
}
