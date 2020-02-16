/********************************************************************************
 * Copyright (c) 2020 itemis AG
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Author:	A. Graf		2020-02, Initial Code
 * 
 ********************************************************************************/

package com.itemis.artop.examples;

import java.util.Collections;

import org.artop.aal.gautosar.services.splitting.handler.IVisibleResourcesProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * A simple implementation of the splitable framework's IVisibleResourcesProvider.
 * The original implementation assumes a workspace, which we don't have in standalone.
 * So this just returns all resources of the resource set.
 * 
 * @author graf
 *
 */
public class SimpleVisibleResourceProvider implements IVisibleResourcesProvider {

	@Override
	public Iterable<Resource> getVisibleResources(Resource context) {
		if (context == null || context.getResourceSet() == null) {
			return Collections.emptyList();
		}
		ResourceSet rs = context.getResourceSet();
		return rs.getResources();
	}

}
