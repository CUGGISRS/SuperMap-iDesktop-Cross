/*
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 */


// $Id: AcceptAllDropFilter.java,v 1.4 2005/12/04 13:46:04 jesper Exp $
package com.supermap.desktop.ui.docking.drop;

/**
 * A {@link com.supermap.desktop.ui.docking.drop.DropFilter} that will accept drop of any
 * window.
 *
 * @author $Author: jesper $
 * @version $Revision: 1.4 $
 * @since IDW 1.4.0
 */
public class AcceptAllDropFilter implements DropFilter {
  private static final long serialVersionUID = 1;

  /**
   * The only instance of this class
   */
  public final static AcceptAllDropFilter INSTANCE = new AcceptAllDropFilter();

  private AcceptAllDropFilter() {
  }
	@Override
  public boolean acceptDrop(DropInfo dropInfo) {
    return true;
  }
}
