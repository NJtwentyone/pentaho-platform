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

package org.pentaho.platform.web.http.api.resources;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ObjectFactory; // TODO can't use generic org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory, need class configured for pentaho

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//POC
public class JobScheduleRequestJaxbTest {
  @Test
  public void testJaxbJson() throws Exception {
    // declare in System or add property file in root of resource folder
    System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

    //Set the various properties you want
    Map<String, Object> properties = new HashMap<>();
    properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
    properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);

//    JAXBContext jaxbContext 	= JAXBContext.newInstance( JobScheduleRequest.class );
    JAXBContext jaxbContext =
      JAXBContextFactory.createContext(new Class[]  {
        // NOTE: seems like you can use either class  org.pentaho.platform.web.http.api.resources.JobScheduleRequest or org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.JobScheduleRequest
        JobScheduleRequest.class,    ObjectFactory.class}, properties);
//    JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {JobScheduleRequest.class}, properties);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();


//    //Set JSON type
//    jaxbUnmarshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
//    jaxbUnmarshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

    //Overloaded methods to unmarshal from different xml sources
    String jsonFileName = "jaxb/JobScheduleRequest_update.json";
    File jsonFileJobSchedulerRequest_update = new File(getClass().getClassLoader().getResource(jsonFileName).getFile());
    StreamSource streamSource = new StreamSource(jsonFileJobSchedulerRequest_update);
    System.out.println( "TYOOOOOO: " + jsonFileJobSchedulerRequest_update.getAbsolutePath());
    //NOTE: including class to give unmarshaller a hint, otherwise will get error related to jobName not found
    JobScheduleRequest jobScheduleRequest = jaxbUnmarshaller.unmarshal( streamSource, JobScheduleRequest.class ).getValue();

    //asserts
    assertEquals( "Top Customers (report)-manual-edit-", jobScheduleRequest.getJobName() );
    assertTrue(jobScheduleRequest.getJobParameters().size() > 8);
    assertEquals("sLine", jobScheduleRequest.getJobParameters().get( 0 ).getName());
    assertEquals("string", jobScheduleRequest.getJobParameters().get( 0 ).getType());
    assertEquals("[[Product].[All Products].[Classic Cars]]", jobScheduleRequest.getJobParameters().get( 0 ).getStringValue().toString());

  }

  // NOTE: XML works
  @Test
  public void testJaxbXml() throws Exception {

    Map<String, Object> properties = new HashMap<>();
    JAXBContext jaxbContext =
      JAXBContextFactory.createContext(new Class[]  {
        // NOTE: seems like you can use either class org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.JobScheduleRequest or org.pentaho.platform.web.http.api.resources.JobScheduleRequest
        JobScheduleRequest.class,    ObjectFactory.class}, properties);
    //    JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {JobScheduleRequest.class}, properties);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

    //Overloaded methods to unmarshal from different xml sources
    String xmlFileName = "jaxb/JobScheduleRequest_create.xml";
    File fileJobSchedulerRequest_update = new File(getClass().getClassLoader().getResource(xmlFileName).getFile());
    System.out.println( "test file: " + fileJobSchedulerRequest_update.getAbsolutePath());
    JobScheduleRequest jobScheduleRequest = (JobScheduleRequest) jaxbUnmarshaller.unmarshal( fileJobSchedulerRequest_update );

    //asserts
    assertEquals( "TestJobName1", jobScheduleRequest.getJobName() );
    assertTrue(jobScheduleRequest.getJobParameters().size() > 0);
    assertEquals("ParameterNameTest1", jobScheduleRequest.getJobParameters().get( 0 ).getName());
    assertEquals("string", jobScheduleRequest.getJobParameters().get( 0 ).getType());
    assertTrue(jobScheduleRequest.getJobParameters().get( 0 ).getStringValue().contains( "false" ));

  }


}
