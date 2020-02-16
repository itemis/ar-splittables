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


import org.artop.aal.autosar40.services.splitting.Autosar40SplitableService;
import org.artop.aal.gautosar.services.splitting.AmbiguityHandler;
import org.artop.aal.gautosar.services.splitting.ExceptionThrowingAmbiguityHandler;
import org.artop.aal.gautosar.services.splitting.StandaloneSplitableElementsModule;
import org.artop.aal.gautosar.services.splitting.handler.IVisibleResourcesProvider;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * The standalone setup of the Artop Splitable Services is done with DI, so we
 * need to set it up accordingly.
 * 
 * @author graf
 *
 */
public class InjectorProvider {
	public static Injector get() {
		Injector injector = Guice.createInjector(StandaloneSplitableElementsModule.create( new Autosar40SplitableService()),
				new Module() {

			@Override
			public void configure(Binder binder) {
				binder.bind(AmbiguityHandler.class).to(ExceptionThrowingAmbiguityHandler.class);
				binder.bind(IVisibleResourcesProvider.class).to(SimpleVisibleResourceProvider.class);
			}
		});
		
		return injector;
	}
}
