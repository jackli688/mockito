/*
 * Copyright (c) 2018 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.moduletest;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.ModuleVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.utility.OpenedClassReader;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.creation.bytebuddy.InlineByteBuddyMockMaker;
import org.mockito.stubbing.OngoingStubbing;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

public class ModuleHandlingTest {

    @Test
    public void can_define_class_in_open_reading_module() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(false));

        Path jar = modularJar(true, true, true);
        ModuleLayer layer = layer(jar, true);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            @SuppressWarnings("unchecked")
            Callable<String> mock = (Callable<String>) mockito.getMethod("mock", Class.class).invoke(null, type);
            Object stubbing = mockito.getMethod("when", Object.class).invoke(null, mock.call());
            loader.loadClass(OngoingStubbing.class.getName()).getMethod("thenCallRealMethod").invoke(stubbing);

            assertThat(mock.getClass().getName()).startsWith("sample.MyCallable$MockitoMock$");
            assertThat(mock.call()).isEqualTo("foo");
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    @Test
    public void inline_mock_maker_can_mock_closed_modules() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(true));

        Path jar = modularJar(false, false, false);
        ModuleLayer layer = layer(jar, false);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        Class<?> mockito = loader.loadClass(Mockito.class.getName());
        @SuppressWarnings("unchecked")
        Callable<String> mock = (Callable<String>) mockito.getMethod("mock", Class.class).invoke(null, type);
        Object stubbing = mockito.getMethod("when", Object.class).invoke(null, mock.call());
        loader.loadClass(OngoingStubbing.class.getName()).getMethod("thenCallRealMethod").invoke(stubbing);

        assertThat(mock.getClass().getName()).isEqualTo("sample.MyCallable");
        assertThat(mock.call()).isEqualTo("foo");
    }

    @Test
    public void can_define_class_in_open_reading_private_module() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(false));

        Path jar = modularJar(false, true, true);
        ModuleLayer layer = layer(jar, true);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            @SuppressWarnings("unchecked")
            Callable<String> mock = (Callable<String>) mockito.getMethod("mock", Class.class).invoke(null, type);
            Object stubbing = mockito.getMethod("when", Object.class).invoke(null, mock.call());
            loader.loadClass(OngoingStubbing.class.getName()).getMethod("thenCallRealMethod").invoke(stubbing);

            assertThat(mock.getClass().getName()).startsWith("sample.MyCallable$MockitoMock$");
            assertThat(mock.call()).isEqualTo("foo");
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    @Test
    public void can_define_class_in_open_non_reading_module() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(false));

        Path jar = modularJar(true, true, true);
        ModuleLayer layer = layer(jar, false);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            @SuppressWarnings("unchecked")
            Callable<String> mock = (Callable<String>) mockito.getMethod("mock", Class.class).invoke(null, type);
            Object stubbing = mockito.getMethod("when", Object.class).invoke(null, mock.call());
            loader.loadClass(OngoingStubbing.class.getName()).getMethod("thenCallRealMethod").invoke(stubbing);

            assertThat(mock.getClass().getName()).startsWith("sample.MyCallable$MockitoMock$");
            assertThat(mock.call()).isEqualTo("foo");
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    @Test
    public void can_define_class_in_open_non_reading_non_exporting_module() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(false));

        Path jar = modularJar(true, false, true);
        ModuleLayer layer = layer(jar, false);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            @SuppressWarnings("unchecked")
            Callable<String> mock = (Callable<String>) mockito.getMethod("mock", Class.class).invoke(null, type);
            Object stubbing = mockito.getMethod("when", Object.class).invoke(null, mock.call());
            loader.loadClass(OngoingStubbing.class.getName()).getMethod("thenCallRealMethod").invoke(stubbing);

            assertThat(mock.getClass().getName()).startsWith("sample.MyCallable$MockitoMock$");
            assertThat(mock.call()).isEqualTo("foo");
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    @Test
    public void can_define_class_in_closed_module() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(false));

        Path jar = modularJar(true, true, false);
        ModuleLayer layer = layer(jar, false);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            @SuppressWarnings("unchecked")
            Callable<String> mock = (Callable<String>) mockito.getMethod("mock", Class.class).invoke(null, type);
            Object stubbing = mockito.getMethod("when", Object.class).invoke(null, mock.call());
            loader.loadClass(OngoingStubbing.class.getName()).getMethod("thenCallRealMethod").invoke(stubbing);

            boolean relocated = !Boolean.getBoolean("org.mockito.internal.noUnsafeInjection") && ClassInjector.UsingReflection.isAvailable();
            String prefix = relocated ? "sample.MyCallable$MockitoMock$" : "org.mockito.codegen.MyCallable$MockitoMock$";
            assertThat(mock.getClass().getName()).startsWith(prefix);
            assertThat(mock.call()).isEqualTo("foo");
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    @Test
    public void cannot_define_class_in_non_opened_non_exported_module() throws Exception {
        assumeThat(Plugins.getMockMaker() instanceof InlineByteBuddyMockMaker, is(false));

        Path jar = modularJar(false, false, false);
        ModuleLayer layer = layer(jar, false);

        ClassLoader loader = layer.findLoader("mockito.test");
        Class<?> type = loader.loadClass("sample.MyCallable");

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> mockito = loader.loadClass(Mockito.class.getName());
            try {
                mockito.getMethod("mock", Class.class).invoke(null, type);
                fail("Expected mocking to fail");
            } catch (InvocationTargetException e) {
                assertThat(e.getTargetException()).isInstanceOf(loader.loadClass(MockitoException.class.getName()));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    private static Path modularJar(boolean isPublic, boolean isExported, boolean isOpened) throws IOException {
        Path jar = Files.createTempFile("sample-module", ".jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("module-info.class"));
            out.write(moduleInfo(isExported, isOpened));
            out.closeEntry();
            out.putNextEntry(new JarEntry("sample/MyCallable.class"));
            out.write(type(isPublic));
            out.closeEntry();
        }
        return jar;
    }

    private static byte[] type(boolean isPublic) {
        return new ByteBuddy()
            .subclass(Callable.class)
            .name("sample.MyCallable")
            .merge(isPublic ? Visibility.PUBLIC : Visibility.PACKAGE_PRIVATE)
            .method(named("call"))
            .intercept(FixedValue.value("foo"))
            .make()
            .getBytes();
    }

    private static byte[] moduleInfo(boolean isExported, boolean isOpened) {
        ClassWriter classWriter = new ClassWriter(OpenedClassReader.ASM_API);
        classWriter.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
        ModuleVisitor mv = classWriter.visitModule("mockito.test", 0, null);
        mv.visitRequire("java.base", Opcodes.ACC_MANDATED, null);
        mv.visitPackage("sample");
        if (isExported) {
            mv.visitExport("sample", 0);
        }
        if (isOpened) {
            mv.visitOpen("sample", 0);
        }
        mv.visitEnd();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static ModuleLayer layer(Path jar, boolean canRead) throws MalformedURLException {
        Configuration configuration = Configuration.resolve(
            ModuleFinder.of(jar),
            Collections.singletonList(ModuleLayer.boot().configuration()),
            ModuleFinder.of(),
            Collections.singleton("mockito.test")
        );

        ClassLoader classLoader = new ReplicatingClassLoader(jar);
        ModuleLayer.Controller controller = ModuleLayer.defineModules(
            configuration,
            Collections.singletonList(ModuleLayer.boot()),
            module -> classLoader
        );
        if (canRead) {
            controller.addReads(
                controller.layer().findModule("mockito.test").orElseThrow(IllegalStateException::new),
                Mockito.class.getModule()
            );
        }
        return controller.layer();
    }

}
