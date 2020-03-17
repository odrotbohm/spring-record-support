/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odrotbohm.experiments.jdk14;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.springframework.beans.BeanInfoFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanInfoFactory} that creates artificial {@link PropertyDescriptor} instances whenever Spring's
 * {@link BeanUtils#getPropertyDescriptors(Class)} is used. Record components are considered like read-only properties
 * with the component's accessor method exposed as read method of the property.
 *
 * @author Oliver Drotbohm
 */
public class Jdk14BeanInfoFactory implements BeanInfoFactory {

	private static final BeanInfoFactory DELEGATE;

	static {

		DELEGATE = ClassUtils.isPresent("java.lang.Record", Jdk14BeanInfoFactory.class.getClassLoader()) //
				? RecordBeanInfoFactory.INSTANCE //
				: null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.BeanInfoFactory#getBeanInfo(java.lang.Class)
	 */
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		return DELEGATE == null ? null : DELEGATE.getBeanInfo(beanClass);
	}

	static enum RecordBeanInfoFactory implements BeanInfoFactory {

		INSTANCE;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.BeanInfoFactory#getBeanInfo(java.lang.Class)
		 */
		public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {

			if (!beanClass.isRecord()) {
				return null;
			}

			var descriptors = Arrays.stream(beanClass.getRecordComponents()) //
					.map(RecordBeanInfoFactory::toPropertyDescriptor) //
					.toArray(PropertyDescriptor[]::new);

			return new RecordBeanInfo(descriptors);
		}

		private static PropertyDescriptor toPropertyDescriptor(RecordComponent component) {

			try {
				return new PropertyDescriptor(component.getName(), component.getAccessor(), null);
			} catch (IntrospectionException e) {
				throw new RuntimeException("Invalid record definition!", e);
			}
		}

		private static class RecordBeanInfo extends SimpleBeanInfo {

			private final PropertyDescriptor[] descriptors;

			public RecordBeanInfo(PropertyDescriptor[] descriptors) {
				this.descriptors = descriptors.clone();
			}

			/*
			 * (non-Javadoc)
			 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
			 */
			@Override
			public PropertyDescriptor[] getPropertyDescriptors() {
				return descriptors;
			}
		}
	}
}
