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
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.action.kettle.DIServerConfig;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.OutputStream;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * POC to inject KettleVFS logic into  Scheduler writing of reports to PVFS locations.
 */
public class PVFSRepositoryFileStreamProvider extends RepositoryFileStreamProvider  {

  // providing a serialVersionUID will help prevent quartz from throwing incompatible class exceptions
  private static final long serialVersionUID  = 2123027232821453048L; // POC changed UID from //2812310908328498989L;

  private static final Log logger = LogFactory.getLog( PVFSRepositoryFileStreamProvider.class );

  IMetaStore saveMetaStore;

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
    // kept code snippet above similar to RepositoryFileStreamProvider#getOutputStream to get filename
    String filename = tempOutputFilePath.substring( tempOutputFilePath.lastIndexOf( RepositoryFile.SEPARATOR ) + 1 );
    String fullPVFSFilePath = concat( getPVFSOutputDirectory() , filename);

    /**
     * Milestone: able to write a file on AWS S3
     *
     * TODO look into how the default providers are created for PurRepositoryMetaStore
     * */

    /**
     * TODO After call to setXmlMetaStore(),
     * the PUC application will stop working, PUC expects the IMetaStore to Be PurRepositoryMetaStore.java.
     * Current singleton instance of ConnectionManager is not thread safe.
     *
     * On server start up, the KTR server//pentaho-server/pentaho-solutions/system/pentaho-geo/dataimport/import_us_postalcode.ktr
     * is run with XmlMetaStore supplied to ConnectionManager
     *
     * Then after that, PurRepositoryMetaStore is supplied to ConnectionManager via provided PurRepository#connect
     * initaited from Trans#fireTransFinishedListeners
     */
    //setXmlMetaStore(); first iteration on how to set ConnectionManager's metastore Connection Details
    addXmlMetaStoreToPurRepositoryMetaStore();

    OutputStream pvfsOutputStream = KettleVFS.getOutputStream( fullPVFSFilePath, false);

    return  pvfsOutputStream;
    /**
     * Commenting out RepositoryFileOutputStream, opening two stream will cause issue.
     * TODO investigate do we need to implement a VFS version of RepositoryFileOutputStream and implement:
     * - RepositoryFileOutputStream#addListener
     * RepositoryFileOutputStream#forceFlush
     */

//    RepositoryFileOutputStream outputStream =
//        new RepositoryFileOutputStream( tempOutputFilePath, autoCreateUniqueFilename(), true );
//    outputStream.addListener( this );
//    outputStream.forceFlush( false );
//
//    return usePVFS() ? pvfsOutputStream : outputStream;
  }

  public String getPVFSOutputDirectory() {
    return System.getenv().getOrDefault( "POC_PVFS_OUTPUT_DIR", null );
  }

  public String concat(String replacementPath, String filename ){
    return replacementPath + "/" + filename;
  }

  public void setXmlMetaStore() {
    /**
     * Get XMl metastore and override shared instance of ConnectionManager's metastore.
     * Luckily, all the logic that needs ConnectionManger's metastore calls ConnectionManager#setMetastoreSupplier
     * with the appropriate Metastore ie
     *  UpdateAuditData uses XmlMetastore
     *  some export logic uses PurRepositoryMetaStore
     */
    ConnectionManager.getInstance().setMetastoreSupplier( () -> {
      try {
        return MetaStoreConst.openLocalPentahoMetaStore(); // POC should be XmlMetaStore ie  ~/.pentaho/metastore/pentaho/
      } catch ( MetaStoreException e ) {
        throw new IllegalStateException( "could not creat XmlMetaStore object", e);
      }
    });
  }

  public void addXmlMetaStoreToPurRepositoryMetaStore() {
    /**
     * NOTE:
     * Basically an import all VFS connections from XMl metastore into PurRepository
     *
     * so could use ConnectionManager#copy( IMetaStore sourceMetaStore, IMetaStore destinationMetaStore ).
     * ConnectionManager#copy needs to ConnectionManager#copy( IMetaStore sourceMetaStore ),
     * so no need to #getPurRepositoryMetaStore(),
     */
    try {
      IMetaStore xmlMetastore = MetaStoreConst.openLocalPentahoMetaStore(); // configurable via property 'PENTAHO_METASTORE_FOLDER'
      IMetaStore pucMetastore = getPurRepositoryMetaStore();
      /*
       TODO check each run doesn't add same connection details over,
        verify underlying ConnectionDetails#equals is correctly overridden and implemented
       */
      ConnectionManager.getInstance().copy( xmlMetastore, pucMetastore  );
    } catch ( MetaStoreException e ) {
      throw new IllegalStateException( "could not copy XmlMetaStore object", e );
    }
  }

  public IMetaStore getPurRepositoryMetaStore() throws MetaStoreException {
    try {
      // TODO look at cleaner way to get metatstore or ConnectionManager.metaStoreSupplier.get();
      DIServerConfig sc  = new DIServerConfig( null, null );
      return sc.getMetaStore().getActiveMetaStore();
    } catch ( KettleException | MetaStoreException e ) {
      throw new MetaStoreException( "could not getPurRepositoryMetaStore", e );
    }
  }

  public String toString() {
    // TODO Auto-generated method stub
    return "input file = " + inputFilePath + ":" + "outputFile = " + outputFilePath;
  }
}
