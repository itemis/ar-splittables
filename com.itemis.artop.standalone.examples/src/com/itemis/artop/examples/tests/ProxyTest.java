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

package com.itemis.artop.examples.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.Test;

import com.itemis.artop.examples.Loader;

import autosar40.autosartoplevelstructure.AUTOSAR;

/**
 * Basic test class to make sure that the proxies are loaded correctly.
 * 
 * @author graf
 *
 */
class ProxyTest {

	@Test
	void testNoProxies() throws IOException {
		Loader sut = new Loader();
		ResourceSet resourceSet = sut.provideResourceSet();
		sut.getResources(resourceSet,"resources");
		
		TreeIterator<Notifier> allContents = resourceSet.getAllContents() ;
		while(allContents.hasNext()) {
			Notifier next = allContents.next();
			if(next instanceof EObject) {
				((EObject)next).eCrossReferences()
				.forEach( (cr) -> assertFalse(cr.eIsProxy(), "Proxy in "+next +" "+cr));
			}
		}
	}

	@Test
	void testLoad() throws IOException {
		Loader sut = new Loader();
		ResourceSet resourceSet = sut.provideResourceSet();
		List<Resource> resources = sut.getResources(resourceSet,"resources");
		sut.loadResources(resources);
		TreeIterator<Notifier> allContents = resourceSet.getAllContents() ;
		List<Notifier> cl = new ArrayList<Notifier>();
		allContents.forEachRemaining(cl::add);
		assertEquals(6, cl.stream().filter(AUTOSAR.class::isInstance).count(), "Expected AUTOSARS" );
	}

	
}
