/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.integration.test.componentclass;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.auraframework.def.ComponentDef;
import org.auraframework.def.ControllerDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.HelperDef;
import org.auraframework.def.LibraryDef;
import org.auraframework.def.LibraryDefRef;
import org.auraframework.def.ProviderDef;
import org.auraframework.def.RendererDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.DefinitionAccessImpl;
import org.auraframework.system.AuraContext;
import org.auraframework.impl.javascript.BaseJavascriptClass;
import org.auraframework.impl.root.component.ComponentDefImpl.Builder;
import org.auraframework.impl.root.component.JavascriptComponentClass;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class JavascriptComponentClassTest extends AuraImplTestCase {

	private Builder builder;

    @Mock
    private DefDescriptor<ComponentDef> descriptor;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Mockito.doReturn("test").when(descriptor).getNamespace();
        Mockito.doReturn("testComponent").when(descriptor).getName();
        Mockito.doReturn("markup://test:testComponent").when(descriptor).getQualifiedName();

        builder = new Builder();
        builder.setDescriptor(descriptor);
        builder.setAccess(new DefinitionAccessImpl(AuraContext.Access.GLOBAL));
    }

    @Test
    public void testGetCodeForPlainComponent() throws Exception {
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
        this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testGetMinifiedCodeWhenMinifyIsTrue() throws Exception {
        String controllerCode =
                "({\n" +
                "    funtion1: function(cmp, event, helper) {\n" +
                "        cmp.get('bla');\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<ControllerDef> controllerDescriptor = addSourceAutoCleanup(ControllerDef.class, controllerCode);
        builder.addControllerDef(definitionService.getDefinition(controllerDescriptor));
        ComponentDef componentDef = builder.build();
        BaseJavascriptClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).setMinify(true).build();

        String minifiedCode = javascriptClass.getMinifiedCode();

        this.goldFileText(minifiedCode);
    }

    /**
     * Verify minified code is not generated when minify is set as false.
     *
     * Ideally, it would be nice to be able to verify if the code get validated or not, but for now,
     * since validateCodeErrors() is private, we assume only the code which needs to be minified
     * will get validated.
     */
    @Test
    public void testGetMinifiedCodeWhenMinifyIsFalse() throws Exception {
        String controllerCode =
                "({\n" +
                "    funtion1: function(cmp, event, helper) {\n" +
                "        cmp.get('bla');\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<ControllerDef> controllerDescriptor = addSourceAutoCleanup(ControllerDef.class, controllerCode);
        builder.addControllerDef(definitionService.getDefinition(controllerDescriptor));
        ComponentDef componentDef = builder.build();
        BaseJavascriptClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).setMinify(false).build();

        String minifiedCode = javascriptClass.getMinifiedCode();

        assertNull(minifiedCode);
    }

    @Test
    public void testGetCodeForComponentWithClientController() throws Exception {
        String controllerCode =
                "({\n" +
                "    funtion1: function(cmp, event, helper) {\n" +
                "        cmp.get('bla');\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<ControllerDef> controllerDescriptor = addSourceAutoCleanup(ControllerDef.class, controllerCode);

        builder.addControllerDef(definitionService.getDefinition(controllerDescriptor));
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testGetCodeForComponentWithHelper() throws Exception {
        String helperCode =
                "({" +
                "    funtion1:function() {\n" +
                "        var a = 1;\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<HelperDef> helperDescriptor = addSourceAutoCleanup(HelperDef.class, helperCode);

        builder.addHelper(helperDescriptor.getQualifiedName());
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testGetCodeForComponentWithClientProvider() throws Exception {

        String providerCode =
                "({\n" +
                "    provide: function(cmp) {\n" +
                "        return 'foo';\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<ProviderDef> providerDescriptor = addSourceAutoCleanup(ProviderDef.class, providerCode);

    	builder.addProvider(providerDescriptor.getQualifiedName());
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testGetCodeForComponentWithClientRenderer() throws Exception {
        String rendererCode =
                "({\n" +
                "    render: function(cmp) {\n" +
                "        return this.superRender();\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<RendererDef> rendererDescriptor = addSourceAutoCleanup(RendererDef.class, rendererCode);

        builder.addRendererDef(definitionService.getDefinition(rendererDescriptor));
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testGetCodeForComponentWithClientEmptyRenderer() throws Exception {
        String rendererCode = "({ })";
        DefDescriptor<RendererDef> rendererDescriptor = addSourceAutoCleanup(RendererDef.class, rendererCode);

        builder.addRendererDef(definitionService.getDefinition(rendererDescriptor));
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCodeForComponentImportsLib() throws Exception {

        DefDescriptor<LibraryDef> libraryDescriptor = mock(DefDescriptor.class);
        when(libraryDescriptor.getDescriptorName()).thenReturn("test:testLibrary");

        // so far, we only add library's meta info to component class. Mock the ImportDefs.
        List<LibraryDefRef> importDefs = new ArrayList<>();
        LibraryDefRef mockImportDef1 = mock(LibraryDefRef.class);
        when(mockImportDef1.getProperty()).thenReturn("myLib1");
        when(mockImportDef1.getReferenceDescriptor()).thenReturn(libraryDescriptor);
        importDefs.add(mockImportDef1);

        LibraryDefRef mockImportDef2 = mock(LibraryDefRef.class);
        when(mockImportDef2.getProperty()).thenReturn("myLib2");
        when(mockImportDef2.getReferenceDescriptor()).thenReturn(libraryDescriptor);
        importDefs.add(mockImportDef2);

        builder.imports = importDefs;
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCodeForComponentExtendingOtherComponent() throws Exception {
        // mock testing component's super component def descriptor
        DefDescriptor<ComponentDef> mockParentDescriptor = mock(DefDescriptor.class);
        when(mockParentDescriptor.getQualifiedName()).thenReturn("markup://test:superComponent");

        builder.extendsDescriptor = mockParentDescriptor;
        ComponentDef componentDef = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(componentDef).build();
    	this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testGetMinifiedCodeForLockerComponentMinified() throws Exception {
        // Fake a non-internal namespace so component is put in Locker
        Mockito.doReturn("nonInternal").when(descriptor).getNamespace();

        // We need more than the base component to get minified code
        String controllerCode =
                "({\n" +
                "    funtion1: function(cmp, event, helper) {\n" +
                "        cmp.get('bla');\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<ControllerDef> controllerDescriptor = addSourceAutoCleanup(ControllerDef.class, controllerCode);
        builder.addControllerDef(definitionService.getDefinition(controllerDescriptor));
        ComponentDef def = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(def).build();
        this.goldFileText(javascriptClass.getMinifiedCode());
    }

    @Test
    public void testGetCodeForLockerComponentUnminified() throws Exception {
        // Fake a non-internal namespace so component is put in Locker
        Mockito.doReturn("nonInternal").when(descriptor).getNamespace();

        // We need more than the base component to get minified code
        String controllerCode =
                "({\n" +
                "    funtion1: function(cmp, event, helper) {\n" +
                "        cmp.get('bla');\n" +
                "    }\n" +
                "})\n";
        DefDescriptor<ControllerDef> controllerDescriptor = addSourceAutoCleanup(ControllerDef.class, controllerCode);
        builder.addControllerDef(definitionService.getDefinition(controllerDescriptor));
        ComponentDef def = builder.build();
        JavascriptComponentClass javascriptClass = new JavascriptComponentClass.Builder().setDefinition(def).build();
        this.goldFileText(javascriptClass.getCode());
    }

    @Test
    public void testBuildValidateJSCodeWhenBuiderWithTrueMinify() throws Exception {
        String helperCode =
                "({\n" +
                "    funtion1: function() {var foo={k:}}\n" +
                "})\n";
        DefDescriptor<HelperDef> helperDescriptor = this.addSourceAutoCleanup(HelperDef.class, helperCode);
        builder.addHelper(helperDescriptor.getQualifiedName());
        ComponentDef componentDef = builder.build();
        BaseJavascriptClass.Builder jsComponentClassBuilder = new JavascriptComponentClass.Builder().setDefinition(componentDef).setMinify(true);

        try{
            jsComponentClassBuilder.build();
            fail("Expecting a InvalidDefinitionException.");
        } catch (Exception e) {
            String expectedMsg = String.format("JS Processing Error: %s", descriptor.getQualifiedName());
            this.assertExceptionMessageContains(e, InvalidDefinitionException.class, expectedMsg);
        }
    }

    public void testBuildNotValidateJSCodeWhenBuiderWithFalseMinify() throws Exception {
        String helperCode =
                "({\n" +
                "    funtion1: function() {var foo={k:}}\n" +
                "})\n";
        DefDescriptor<HelperDef> helperDescriptor = this.addSourceAutoCleanup(HelperDef.class, helperCode);
        builder.addHelper(helperDescriptor.getQualifiedName());
        ComponentDef componentDef = builder.build();
        BaseJavascriptClass.Builder jsComponentClassBuilder = new JavascriptComponentClass.Builder().setDefinition(componentDef).setMinify(false);

        BaseJavascriptClass jsComponentClass = jsComponentClassBuilder.build();

        assertNotNull(jsComponentClass);
    }
}
