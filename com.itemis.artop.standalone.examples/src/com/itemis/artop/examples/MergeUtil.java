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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.artop.aal.gautosar.services.splitting.SplitableEObjectsProvider;
import org.artop.aal.gautosar.services.splitting.SplitableMerge;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import autosar40.genericstructure.generaltemplateclasses.identifiable.Identifiable;
import autosar40.genericstructure.generaltemplateclasses.specialdata.Sd;
import autosar40.genericstructure.generaltemplateclasses.specialdata.Sdg;
import autosar40.genericstructure.generaltemplateclasses.specialdata.SdgContents;
import autosar40.util.Autosar40Factory;
import gautosar.ggenericstructure.ginfrastructure.GARPackage;
import gautosar.ggenericstructure.ginfrastructure.GAUTOSAR;

/**
 * Utility class for the merging of split models
 * 
 * @author graf
 *
 */
public class MergeUtil {
	
	/**
	 * Helper to run the merge on an entire resource set. Just identifies the top
	 * level packages of the resource set and calls the createMergedCopy on those.
	 * 
	 * @param sm
	 * @param resourceSet
	 * @return
	 */
	public GAUTOSAR merge(SplitableMerge sm, ResourceSet resourceSet) {
		List<GARPackage> pkges = resourceSet.getResources().stream().flatMap((r)->r.getContents().stream())
		.flatMap((tl) -> tl.eContents().stream())
		.filter(GARPackage.class::isInstance)
		.map(GARPackage.class::cast)
		.collect(Collectors.toList());
		
		return sm.createMergedCopy(pkges);
	}
	
	/**
	 * Helper for inverse lookup. Builds a multimap that can be used to identify the
	 * EObjects that contributed to a given EObject in the merged model.
	 * 
	 * @param sm
	 * @param splitableEObjectsProvider
	 * @param rs
	 * @return
	 */
	public Multimap<EObject, EObject> buildMap(SplitableMerge sm,  SplitableEObjectsProvider splitableEObjectsProvider,  ResourceSet rs) {
		Multimap<EObject, EObject> multi = HashMultimap.create();
		
		TreeIterator<Notifier> treeIterator = rs.getAllContents();
		while(treeIterator.hasNext()) {
			Notifier n = treeIterator.next();
			if(n instanceof EObject) {
				EObject next = (EObject)n;
				EObject splitableFor = splitableEObjectsProvider.splitableFor(next);
				EObject dest = sm.get(splitableFor);
				multi.put(dest, next);
			}
		}
		
		return multi;
	}
	
	/**
	 * Adds an SD to a model element. Used to add annotation information in the merged 
	 * model, so that we can identify resources that contributed to a given model element
	 * even after save.
	 * 
	 * @param idf
	 * @param s
	 */
	public void addSdg(Identifiable idf, String s) {
		System.out.println("Add: "+idf+": "+s);
		if(idf.getAdminData() == null) {
			idf.setAdminData(Autosar40Factory.eINSTANCE.createAdminData());
		}
		Optional<Sdg> sdgO = idf.getAdminData().getSdgs().stream()
		.filter((sdg) -> sdg.getGid().equals("splitinfo")).findFirst();
		
		System.out.println(sdgO);
		
		Sdg splitSdg = sdgO.orElseGet(() -> {
			Sdg ls = Autosar40Factory.eINSTANCE.createSdg();
			ls.setGid("splitinfo");
			idf.getAdminData().getSdgs().add(ls);
			
			SdgContents createSdgContents = Autosar40Factory.eINSTANCE.createSdgContents();
			ls.gSetSdgContentsType(createSdgContents);
			return ls;
		});
		
		System.out.println(splitSdg);
		Sd sd = Autosar40Factory.eINSTANCE.createSd();
		sd.setGid("splitinfo");
		sd.setValue(s);
		
		splitSdg.getSdgContentsType().getSds().add(sd);
		
		
	}
	
	/**
	 * Annotates the merged model so that in the AdminData/SDG/SD we find the
	 * resource URIs of the elements that contributed to a given merged element.
	 * This allows us to identify splitables even in a saved model.
	 * 
	 * @param gar
	 * @param multi
	 */
	public void annotate(GAUTOSAR gar, Multimap<EObject, EObject> multi ) {
		List<EObject> cl = new ArrayList<EObject>();
		gar.eAllContents().forEachRemaining(cl::add);
		
		cl.stream().filter((x) -> multi.containsKey(x))
		.map((x) -> { System.out.println("C:" +x); return x; })
		.filter(Identifiable.class::isInstance)
		.map(Identifiable.class::cast)
		.forEach((k) -> {
				multi.get(k)
				.forEach((v) -> {
					addSdg(k,v.eResource().getURI().toString());
				});
		});
		
	}
}
