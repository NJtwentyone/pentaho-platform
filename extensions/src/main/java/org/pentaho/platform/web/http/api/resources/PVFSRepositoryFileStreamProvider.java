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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * POC to inject KettleVFS logic into  Scheduler writing of reports to PVFS locations.
 */
public class PVFSRepositoryFileStreamProvider extends RepositoryFileStreamProvider  {

  // providing a serialVersionUID will help prevent quartz from throwing incompatible class exceptions
  private static final long serialVersionUID  = 2123027232821453048L; // POC changed UID from //2812310908328498989L;

  private static final Log logger = LogFactory.getLog( PVFSRepositoryFileStreamProvider.class );

//  public String outputFilePath;
//  public String inputFilePath;
//  public String appendDateFormat;

  // POC keeping these variable private, investigate to make super class protected
//  private IStreamingAction streamingAction;
//  private boolean autoCreateUniqueFilename;

  // POC TODO super constructor needs to be refactored
  public PVFSRepositoryFileStreamProvider( final String inputFilePath, final String outputFilePath,
                                 final boolean autoCreateUniqueFilename ) {
    super(inputFilePath,  outputFilePath,  autoCreateUniqueFilename, null);

  }

  public PVFSRepositoryFileStreamProvider( final String inputFilePath, final String outputFilePath,
                                 final boolean autoCreateUniqueFilename, final String appendDateFormat ) {
    super( inputFilePath, outputFilePath, autoCreateUniqueFilename, appendDateFormat );
  }

  public PVFSRepositoryFileStreamProvider() {
  }


  // TODO implement with KettleVFS.java
  public OutputStream getOutputStream() throws Exception {
    String tempOutputFilePath = outputFilePath;
    String extension = RepositoryFilenameUtils.getExtension( tempOutputFilePath );
    if ( "*".equals( extension ) ) { //$NON-NLS-1$
      tempOutputFilePath = tempOutputFilePath.substring( 0, tempOutputFilePath.lastIndexOf( '.' ) );

      if ( appendDateFormat != null ) {
        try {
          LocalDateTime now = LocalDateTime.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern( appendDateFormat );
          String formattedDate = now.format( formatter );
          tempOutputFilePath += formattedDate;
        } catch ( Exception e ) {
          logger.warn( "Unable to calculate current date: " + e.getMessage() );
        }
      }

      if ( getStreamingAction() != null ) {
        String mimeType = getStreamingAction().getMimeType( null );
        if ( mimeType != null && MimeHelper.getExtension( mimeType ) != null ) {
          tempOutputFilePath += MimeHelper.getExtension( mimeType );
        }
      }
    }

    RepositoryFileOutputStream outputStream =
        new RepositoryFileOutputStream( tempOutputFilePath, autoCreateUniqueFilename(), true );
    outputStream.addListener( this );
    outputStream.forceFlush( false );
    return outputStream;
  }

  public String toString() {
    // TODO Auto-generated method stub
    return "input file = " + inputFilePath + ":" + "outputFile = " + outputFilePath;
  }
}
