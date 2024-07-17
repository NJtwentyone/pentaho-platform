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

package org.pentaho.platform.api.repository2.unified;

import javax.xml.ws.WebFault;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
@WebFault( name = "UnifiedRepositoryException", targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class UnifiedRepositoryException extends RuntimeException {

  // TODO not sure if wsdl will register runtimeException, but have to declare variable name 'faultInfo'
  private static final long serialVersionUID = -3180298582920444104L;

  public UnifiedRepositoryException() {
    super();
  }

  public UnifiedRepositoryException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public UnifiedRepositoryException( final String message ) {
    super( message );
  }

  public UnifiedRepositoryException( final Throwable cause ) {
    super( cause );
  }

}
