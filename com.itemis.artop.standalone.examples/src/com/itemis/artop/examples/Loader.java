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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.artop.aal.common.resource.impl.AutosarResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ResourceFactoryImpl;

/**
 * Loading AR models standalone (i.e. not in an OSGI context)
 * 
 * @author graf
 *
 */
public class Loader {

	public Loader() {
		@SuppressWarnings("unused")
		Autosar40Package einstance = Autosar40Package.eINSTANCE;
		AutosarResourceFactoryImpl resourceFactory = new Autosar40ResourceFactoryImpl();

		// register
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", resourceFactory);
	}

	/*
	 * We return a modified resource set that works for AR models even in standalone
	 * 
	 */
	public ResourceSet provideResourceSet() {
		return new ResourceSetImpl() {
			
			/*
			 * This implementation can deal with AR URIs even in a standalone context
			 * It will simply check if the fragment (i.e. AR FQN) can be used to 
			 * resolve the object in any of the resources of the resource set.
			 */
			@Override
			public EObject getEObject(final URI uri, final boolean loadOnDemand) {
				if (uri == null) {
					return null;
				}
				
				Optional<EObject> resolvedEObject = resources.stream()
				.map((r)->r.getEObject(uri.fragment()))
				.filter(Objects::nonNull)
				.findFirst();
				
				
				return resolvedEObject.orElse(null);
			}
		};
	}

	public List<Resource> getResources(ResourceSet resourceSet, List<String> fileNames) {

		return fileNames.stream().map((fn) -> {
			URI resourceURI = URI.createFileURI(fn);
			return resourceSet.getResource(resourceURI, true);
		}).collect(Collectors.toList());
	}

	public List<Resource> getResources(ResourceSet resourceSet, String directory) throws IOException {
		List<String> fl = Files.walk(Paths.get(directory)).filter(Files::isRegularFile)
		.map((f)->f.toAbsolutePath().toString())
		.filter((f) -> f.endsWith(".arxml"))
		.collect(Collectors.toList());
		
		return getResources(resourceSet, fl);
	}

	public void loadResources(List<Resource> resources) {
		resources.stream().forEach((rs) -> {
			try {
				rs.load(Collections.EMPTY_MAP);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
