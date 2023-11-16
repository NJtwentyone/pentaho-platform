/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

//POC
public class JobScheduleRequestJaxbTest {


  @Test
  public void testJaxb() throws Exception {


    //Set the various properties you want
    Map<String, Object> properties = new HashMap<>();
    properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
    properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

//    JAXBContext jaxbContext 	= JAXBContext.newInstance( JobScheduleRequest.class );
    JAXBContext jaxbContext =
      JAXBContextFactory.createContext(new Class[]  {
        JobScheduleRequest.class,    ObjectFactory.class}, properties);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();


//    //Set JSON type
//    jaxbUnmarshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
//    jaxbUnmarshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

    //Overloaded methods to unmarshal from different xml sources
    String jsonFileName = "JobScheduleRequest_update.json";
    File jsonFileJobSchedulerRequest_update = new File(getClass().getClassLoader().getResource(jsonFileName).getFile());
    System.out.println( "TYOOOOOO: " + jsonFileJobSchedulerRequest_update.getAbsolutePath());
    JobScheduleRequest jobScheduleRequestUpdate = (JobScheduleRequest) jaxbUnmarshaller.unmarshal( jsonFileJobSchedulerRequest_update );

    //asserts
    assertTrue(jobScheduleRequestUpdate.getJobParameters().size() > 0);

  }
}
