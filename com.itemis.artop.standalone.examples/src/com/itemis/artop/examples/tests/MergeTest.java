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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.artop.aal.gautosar.services.splitting.SplitableEObjectsProvider;
import org.artop.aal.gautosar.services.splitting.SplitableEObjectsQuery;
import org.artop.aal.gautosar.services.splitting.SplitableMerge;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Multimap;
import com.google.inject.Injector;
import com.itemis.artop.examples.InjectorProvider;
import com.itemis.artop.examples.Loader;
import com.itemis.artop.examples.MergeUtil;

import autosar40.genericstructure.generaltemplateclasses.arpackage.ARPackage;
import gautosar.ggenericstructure.ginfrastructure.GAUTOSAR;

/**
 * Test several of the features of the splitable service
 * @author graf
 *
 */
class MergeTest {

	/**
	 * Test that the merged model does not have unresolved proxies.
	 * 
	 * @throws IOException
	 */
	@Test
	void testMergeProxies() throws IOException {
		Loader sut = new Loader();
		MergeUtil mUtil = new MergeUtil();
		ResourceSet resourceSet = sut.provideResourceSet();
		sut.getResources(resourceSet,"resources");
		Injector injector = InjectorProvider.get();
		
		SplitableMerge sm = injector.getInstance(SplitableMerge.class);
		
		GAUTOSAR mergedCopy = mUtil.merge(sm,  resourceSet);
		
		TreeIterator<EObject> allContents = mergedCopy.eAllContents() ;
		while(allContents.hasNext()) {
			EObject next = allContents.next();
				(next).eCrossReferences()
				.forEach( (cr) -> assertFalse(cr.eIsProxy(), "Proxy in "+next +" "+cr));
				
				(next).eCrossReferences()
				.forEach( (cr) -> assertSame(mergedCopy.eResource(), cr.eResource(), "Not self contained in "+next +" "+cr));
		}
	}

	/**
	 * Basic check that the merged model conforms to expectations
	 * 
	 * @throws IOException
	 */
	@Test
	void testMergeCorrect() throws IOException {
		Loader sut = new Loader();
		MergeUtil mUtil = new MergeUtil();
		ResourceSet resourceSet = sut.provideResourceSet();
		sut.getResources(resourceSet,"resources");
		Injector injector = InjectorProvider.get();
		
		SplitableMerge sm = injector.getInstance(SplitableMerge.class);
		
		GAUTOSAR mergedCopy = mUtil.merge(sm,  resourceSet);
		
		TreeIterator<EObject> allContents = mergedCopy.eAllContents() ;
		List<EObject> cl = new ArrayList<EObject>();
		allContents.forEachRemaining(cl::add);
		assertEquals(1, cl.stream().filter(ARPackage.class::isInstance).count(), "Expected ARPackages" );
	}
	
	
	/**
	 * Test the merge query. Identify through the API all the model elements that would make
	 * up a splitable for a given EObject.
	 * 
	 * @throws IOException
	 */
	@Test
	void testMergeOQuery() throws IOException {
		Loader sut = new Loader();
		MergeUtil mUtil = new MergeUtil();
		ResourceSet resourceSet = sut.provideResourceSet();
		sut.getResources(resourceSet,"resources");
		Injector injector = InjectorProvider.get();
		
		SplitableEObjectsQuery objectsQuery = injector.getInstance(SplitableEObjectsQuery.class);
	
		TreeIterator<Notifier> allContents = resourceSet.getAllContents() ;
		List<Notifier> cl = new ArrayList<Notifier>();
		allContents.forEachRemaining(cl::add);
		cl.stream()
		.filter(EObject.class::isInstance)
		.map(EObject.class::cast)
		.forEach((o) ->System.out.println("q:"+o +" --> "+objectsQuery.get(o)));
	}
	
	/**
	 * Test the annotate functionality. Adds SDG to the elements of the merged model
	 * and saves the model. 
	 * @throws IOException
	 */
	@Test
	void testMergeAnnotate() throws IOException {
		Loader sut = new Loader();
		MergeUtil mUtil = new MergeUtil();
		ResourceSet resourceSet = sut.provideResourceSet();
		sut.getResources(resourceSet,"resources");
		Injector injector = InjectorProvider.get();
		
		SplitableMerge sm = injector.getInstance(SplitableMerge.class);
		SplitableEObjectsProvider splitableEObjectsProvider = injector.getInstance(SplitableEObjectsProvider.class);
		GAUTOSAR mergedCopy = mUtil.merge(sm,  resourceSet);
		
		Multimap<EObject, EObject> multimap = mUtil.buildMap(sm, splitableEObjectsProvider, resourceSet);
		
		assertNotEquals(0, multimap.size(), "Multimap is empty");
		multimap.entries().stream().forEach((x) -> System.out.println(x.getKey() +" --> " + x.getValue()));
		mUtil.annotate(mergedCopy, multimap);
		
		ResourceSet rs = new ExtendedResourceSetImpl();
		
		Resource autosar40Resource = rs.createResource(URI.createFileURI("merged.arxml"));
		
		autosar40Resource.getContents().add(mergedCopy);
		autosar40Resource.save(null);
	}
	


	
}
