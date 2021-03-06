/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.beanutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for BeanHelper.
 *
 * @since 1.3
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons
 * Configuration team</a>
 * @version $Id$
 */
public class TestBeanHelper
{
    /** Constant for the test value of the string property. */
    private static final String TEST_STRING = "testString";

    /** Constant for the test value of the numeric property. */
    private static final int TEST_INT = 42;

    /** Constant for the name of the test bean factory. */
    private static final String TEST_FACTORY = "testFactory";

    /** The test bean helper instance. */
    private BeanHelper helper;

    @Before
    public void setUp() throws Exception
    {
        helper = new BeanHelper(new TestBeanFactory());
    }

    /**
     * Tests whether the correct default bean factory is set.
     */
    @Test
    public void testDefaultBeanFactory()
    {
        helper = new BeanHelper();
        assertSame("Wrong default bean factory", DefaultBeanFactory.INSTANCE,
                helper.getDefaultBeanFactory());
    }

    /**
     * Tests whether a specific default bean factory can be set when
     * constructing an instance.
     */
    @Test
    public void testInitWithBeanFactory()
    {
        BeanFactory factory = EasyMock.createMock(BeanFactory.class);
        EasyMock.replay(factory);
        helper = new BeanHelper(factory);
        assertSame("Wrong default bean factory", factory,
                helper.getDefaultBeanFactory());
    }

    /**
     * Tests the default instance of BeanHelper.
     */
    @Test
    public void testDefaultInstance()
    {
        assertSame("Wrong factory for default instance",
                DefaultBeanFactory.INSTANCE,
                BeanHelper.INSTANCE.getDefaultBeanFactory());
    }

    /**
     * Tests that a newly created instance does not have any bean factories
     * registered.
     */
    @Test
    public void testRegisteredFactoriesEmptyForNewInstance()
    {
        assertTrue("List of registered factories is not empty", helper
                .registeredFactoryNames().isEmpty());
    }

    /**
     * Tests registering a new bean factory.
     */
    @Test
    public void testRegisterBeanFactory()
    {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        assertEquals("Wrong number of registered factories", 1, helper
                .registeredFactoryNames().size());
        assertTrue("Test factory is not contained", helper
                .registeredFactoryNames().contains(TEST_FACTORY));
    }

    /**
     * Tries to register a null factory. This should cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterBeanFactoryNull()
    {
        helper.registerBeanFactory(TEST_FACTORY, null);
    }

    /**
     * Tries to register a bean factory with a null name. This should cause an
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterBeanFactoryNullName()
    {
        helper.registerBeanFactory(null, new TestBeanFactory());
    }

    /**
     * Tests to deregister a bean factory.
     */
    @Test
    public void testDeregisterBeanFactory()
    {
        BeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        assertSame("Could not deregister factory", factory, helper
                .deregisterBeanFactory(TEST_FACTORY));
        assertTrue("List of factories is not empty", helper
                .registeredFactoryNames().isEmpty());
    }

    /**
     * Tests deregisterBeanFactory() for a non-existing factory name.
     */
    @Test
    public void testDeregisterBeanFactoryNonExisting()
    {
        assertNull("deregistering non existing factory",
                helper.deregisterBeanFactory(TEST_FACTORY));
    }

    /**
     * Tests deregisterBeanFactory() for a null factory name.
     */
    @Test
    public void testDeregisterBeanFactoryNull() {
        assertNull("deregistering null factory",
                helper.deregisterBeanFactory(null));
    }

    /**
     * Tests initializing a bean.
     */
    @Test
    public void testInitBean()
    {
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        BeanCreationTestBean bean = new BeanCreationTestBean();
        helper.initBean(bean, data);
        checkBean(bean);
    }

    /**
     * Tests initializing a bean when the bean declaration does not contain any
     * data.
     */
    @Test
    public void testInitBeanWithNoData()
    {
        BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        BeanCreationTestBean bean = new BeanCreationTestBean();
        helper.initBean(bean, data);
        assertNull("Wrong string property", bean.getStringValue());
        assertEquals("Wrong int property", 0, bean.getIntValue());
        assertNull("Buddy was set", bean.getBuddy());
    }

    /**
     * Tries to initialize a bean with a bean declaration that contains an
     * invalid property value. This should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testInitBeanWithInvalidProperty()
    {
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.getBeanProperties().put("nonExistingProperty", Boolean.TRUE);
        helper.initBean(new BeanCreationTestBean(), data);
    }

    /**
     * Tests creating a bean. All necessary information is stored in the bean
     * declaration.
     */
    @Test
    public void testCreateBean()
    {
        TestBeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) helper.createBean(data, null));
        assertNull("A parameter was passed", factory.parameter);
        assertEquals("Factory not called", 1, factory.getCreateBeanCount());
    }

    /**
     * Tests whether a bean with a property of type collection can be created.
     */
    @Test
    public void testCreateBeanWithListChildBean()
    {
        TestBeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclarationWithListChild();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBeanWithListChild.class.getName());
        checkBean((BeanCreationTestBeanWithListChild) helper.createBean(data, null));
        assertNull("A parameter was passed", factory.parameter);
        assertEquals("Factory not called", 1, factory.getCreateBeanCount());
    }

    /**
     * Tests creating a bean when no bean declaration is provided. This should
     * cause an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateBeanWithNullDeclaration()
    {
        helper.createBean(null);
    }

    /**
     * Tests creating a bean. The bean's class is specified as the default class
     * argument.
     */
    @Test
    public void testCreateBeanWithDefaultClass()
    {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((BeanCreationTestBean) helper.createBean(data, BeanCreationTestBean.class));
    }

    /**
     * Tests creating a bean when the bean's class is specified as the default
     * class of the bean factory.
     */
    @Test
    public void testCreateBeanWithFactoryDefaultClass()
    {
        TestBeanFactory factory = new TestBeanFactory();
        factory.supportsDefaultClass = true;
        helper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        checkBean((BeanCreationTestBean) helper.createBean(data, null));
        assertEquals("Factory not called", 1, factory.getCreateBeanCount());
    }

    /**
     * Tries to create a bean if no class is provided. This should cause an
     * exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithNoClass()
    {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        helper.createBean(data, null);
    }

    /**
     * Tries to create a bean with a non existing class. This should cause an
     * exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithInvalidClass()
    {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName("non.existing.ClassName");
        helper.createBean(data, null);
    }

    /**
     * Tests creating a bean using the default bean factory.
     */
    @Test
    public void testCreateBeanWithDefaultFactory()
    {
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) helper.createBean(data, null));
        TestBeanFactory factory = (TestBeanFactory) helper.getDefaultBeanFactory();
        assertTrue("Factory not called", factory.getCreateBeanCount() > 0);
    }

    /**
     * Tests creating a bean using a non registered factory.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithUnknownFactory()
    {
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        helper.createBean(data, null);
    }

    /**
     * Tests creating a bean when the factory throws an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testCreateBeanWithException()
    {
        helper.registerBeanFactory(TEST_FACTORY, new TestBeanFactory());
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(getClass().getName());
        helper.createBean(data, null);
    }

    /**
     * Tests if a parameter is correctly passed to the bean factory.
     */
    @Test
    public void testCreateBeanWithParameter()
    {
        Object param = new Integer(42);
        TestBeanFactory factory = new TestBeanFactory();
        helper.registerBeanFactory(TEST_FACTORY, factory);
        BeanDeclarationTestImpl data = setUpBeanDeclaration();
        data.setBeanFactoryName(TEST_FACTORY);
        data.setBeanClassName(BeanCreationTestBean.class.getName());
        checkBean((BeanCreationTestBean) helper.createBean(data, null, param));
        assertSame("Wrong parameter", param, factory.parameter);
    }

    /**
     * Returns an initialized bean declaration.
     *
     * @return the bean declaration
     */
    private BeanDeclarationTestImpl setUpBeanDeclaration()
    {
        BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("stringValue", TEST_STRING);
        properties.put("intValue", String.valueOf(TEST_INT));
        data.setBeanProperties(properties);
        BeanDeclarationTestImpl buddyData = new BeanDeclarationTestImpl();
        Map<String, Object> properties2 = new HashMap<String, Object>();
        properties2.put("stringValue", "Another test string");
        properties2.put("intValue", new Integer(100));
        buddyData.setBeanProperties(properties2);
        buddyData.setBeanClassName(BeanCreationTestBean.class.getName());

        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("buddy", buddyData);
        data.setNestedBeanDeclarations(nested);
        return data;
    }

    /**
     * Same as setUpBeanDeclaration, but returns a nested array of beans
     * as a single property. Tests multi-value (Collection<BeanDeclaration>)
     * children construction.
     *
     * @return The bean declaration with a list child bean proerty
     */
    private BeanDeclarationTestImpl setUpBeanDeclarationWithListChild()
    {
        BeanDeclarationTestImpl data = new BeanDeclarationTestImpl();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("stringValue", TEST_STRING);
        properties.put("intValue", String.valueOf(TEST_INT));
        data.setBeanProperties(properties);

        List<BeanDeclaration> childData = new ArrayList<BeanDeclaration>();
        childData.add(createChildBean("child1"));
        childData.add(createChildBean("child2"));
        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("children", childData);
        data.setNestedBeanDeclarations(nested);
        return data;
    }

    /**
     * Create a simple bean declaration that has no children for testing
     * of nested children bean declarations.
     *
     * @param name A name prefix that can be used to disambiguate the children
     * @return A simple declaration
     */
    private BeanDeclarationTestImpl createChildBean(String name)
    {
        BeanDeclarationTestImpl childBean = new BeanDeclarationTestImpl();
        Map<String, Object> properties2 = new HashMap<String, Object>();
        properties2.put("stringValue", name + " Another test string");
        properties2.put("intValue", new Integer(100));
        childBean.setBeanProperties(properties2);
        childBean.setBeanClassName(BeanCreationTestBean.class.getName());

        return childBean;
    }

    /**
     * Tests if the bean was correctly initialized from the data of the test
     * bean declaration.
     *
     * @param bean the bean to be checked
     */
    private void checkBean(BeanCreationTestBean bean)
    {
        assertEquals("Wrong string property", TEST_STRING, bean
                .getStringValue());
        assertEquals("Wrong int property", TEST_INT, bean.getIntValue());
        BeanCreationTestBean buddy = bean.getBuddy();
        assertNotNull("Buddy was not set", buddy);
        assertEquals("Wrong string property in buddy", "Another test string",
                buddy.getStringValue());
        assertEquals("Wrong int property in buddy", 100, buddy.getIntValue());
    }

    /**
     * Tests if the bean was correctly initialized from the data of the test
     * bean declaration.
     *
     * @param bean the bean to be checked
     */
    private void checkBean(BeanCreationTestBeanWithListChild bean)
    {
        assertEquals("Wrong string property", TEST_STRING, bean
                .getStringValue());
        assertEquals("Wrong int property", TEST_INT, bean.getIntValue());
        List<BeanCreationTestBean> children = bean.getChildren();
        assertNotNull("Children were not set", children);
        assertEquals("Wrong number of children created", children.size(), 2);
        assertNotNull("First child was set as null", children.get(0));
        assertNotNull("Second child was set as null", children.get(1));
    }

    /**
     * An implementation of the BeanFactory interface used for testing. This
     * implementation is really simple: If the BeanCreationTestBean class is provided, a new
     * instance will be created. Otherwise an exception is thrown.
     */
    private class TestBeanFactory implements BeanFactory
    {
        Object parameter;

        boolean supportsDefaultClass;

        /** A counter for the created instances. */
        private int createBeanCount;

        public Object createBean(BeanCreationContext bcc) throws Exception
        {
            createBeanCount++;
            parameter = bcc.getParameter();
            if (BeanCreationTestBean.class.equals(bcc.getBeanClass()))
            {
                BeanCreationTestBean bean = new BeanCreationTestBean();
                helper.initBean(bean, bcc.getBeanDeclaration());
                return bean;
            }
            else if (BeanCreationTestBeanWithListChild.class.equals(bcc
                    .getBeanClass()))
            {
                BeanCreationTestBeanWithListChild bean =
                        new BeanCreationTestBeanWithListChild();
                helper.initBean(bean, bcc.getBeanDeclaration());
                return bean;
            }
            else
            {
                throw new IllegalArgumentException("Unsupported class: "
                        + bcc.getBeanClass());
            }
        }

        /**
         * Returns the default class, but only if the supportsDefaultClass flag
         * is set.
         */
        public Class<?> getDefaultBeanClass()
        {
            return supportsDefaultClass ? BeanCreationTestBean.class : null;
        }

        /**
         * Returns the number of beans created via this factory.
         *
         * @return the number of created beans
         */
        public int getCreateBeanCount()
        {
            return createBeanCount;
        }
    }
}
