/*
 * Copyright 2000-2016 Holon TDCN.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.core.internal.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.Property.PropertyAccessException;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;

/**
 * Default {@link PropertyBox} implementation.
 * <p>
 * Property values are stored internally using a {@link HashMap}. Property value access is thread-safe.
 * </p>
 * 
 * @since 5.0.0
 * 
 * @see PropertyBox
 */
public class DefaultPropertyBox extends AbstractPropertyBox {

	/**
	 * Property-value map
	 */
	protected final Map<Property<?>, Object> propertyValues;

	/**
	 * Construct a new PropertyBox using given <code>properties</code> as property set.
	 * @param properties Property set
	 */
	@SuppressWarnings("rawtypes")
	@SafeVarargs
	public DefaultPropertyBox(Property... properties) {
		this(PropertySet.of(properties));
	}

	/**
	 * Construct a new PropertyBox using given <code>properties</code> as property set.
	 * @param <P> Actual property type
	 * @param properties Property set
	 */
	@SuppressWarnings("rawtypes")
	public <P extends Property> DefaultPropertyBox(Iterable<P> properties) {
		this((properties instanceof PropertySet) ? (PropertySet<P>) properties : PropertySet.of(properties));
	}

	/**
	 * Construct a new PropertyBox using given <code>propertySet</code> as property set.
	 * @param <P> Actual property type
	 * @param propertySet Property set
	 */
	@SuppressWarnings("rawtypes")
	public <P extends Property> DefaultPropertyBox(PropertySet<P> propertySet) {
		super(propertySet);
		this.propertyValues = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.property.AbstractPropertyBox#getPropertyValue(com.holonplatform.core.property.
	 * Property)
	 */
	@Override
	protected <T> Object getPropertyValue(Property<T> property) throws PropertyAccessException {
		synchronized (propertyValues) {
			return propertyValues.get(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.core.internal.property.AbstractPropertyBox#setPropertyValue(com.holonplatform.core.property.
	 * Property, java.lang.Object)
	 */
	@Override
	protected <T> void setPropertyValue(Property<T> property, T value) throws PropertyAccessException {
		synchronized (propertyValues) {
			if (value == null) {
				propertyValues.remove(property);
			} else {
				propertyValues.put(property, value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.core.property.PropertyBox#propertyValues()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Stream<PropertyValue<T>> propertyValues() {
		return propertyValues.entrySet().stream().map(e -> new DefaultPropertyValue(e.getKey(), e.getValue()));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DefaultPropertyBox [");
		sb.append(getPropertySet());
		sb.append("]: ");
		if (propertyValues == null || propertyValues.isEmpty()) {
			sb.append("[EMPTY]");
		} else {
			final List<String> pv = new ArrayList<>(propertyValues.size());
			propertyValues.forEach((p, v) -> pv.add(p + "=" + v));
			sb.append(pv.stream().collect(Collectors.joining(",", "[", "]")));
		}
		return sb.toString();
	}

	// Builder

	public static class PropertyBoxBuilder implements Builder {

		private final DefaultPropertyBox instance;

		/**
		 * Constructor.
		 * @param <P> Actual property type
		 * @param properties Iterable set of properties
		 */
		@SuppressWarnings("rawtypes")
		public <P extends Property> PropertyBoxBuilder(Iterable<P> properties) {
			super();
			this.instance = new DefaultPropertyBox(properties);
		}

		/**
		 * Constructor
		 * @param <P> Actual property type
		 * @param properties Set of properties
		 */
		@SuppressWarnings("rawtypes")
		@SafeVarargs
		public <P extends Property> PropertyBoxBuilder(P... properties) {
			super();
			this.instance = new DefaultPropertyBox(properties);
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.property.PropertyBox.Builder#invalidAllowed(boolean)
		 */
		@Override
		public Builder invalidAllowed(boolean invalidAllowed) {
			this.instance.setInvalidAllowed(invalidAllowed);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.property.PropertyBox.Builder#set(com.holonplatform.core.property.Property,
		 * java.lang.Object)
		 */
		@Override
		public <T> Builder set(Property<T> property, T value) {
			this.instance.setValue(property, value);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.property.PropertyBox.Builder#setIgnoreReadOnly(com.holonplatform.core.property.
		 * Property, java.lang.Object)
		 */
		@Override
		public <T> Builder setIgnoreReadOnly(Property<T> property, T value) {
			this.instance.setValue(property, value, true);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.holonplatform.core.property.PropertyBox.Builder#copyValues(com.holonplatform.core.property.PropertyBox)
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Builder copyValues(PropertyBox source) {
			ObjectUtils.argumentNotNull(source, "Source PropertyBox must be not null");
			for (Property p : source) {
				if (!p.isReadOnly() && this.instance.contains(p)) {
					this.instance.setValue(p, source.getValue(p));
				}
			}
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.property.PropertyBox.Builder#build()
		 */
		@Override
		public PropertyBox build() {
			return instance;
		}

	}

	/**
	 * Default {@link PropertyValue} implementation.
	 * @param <T> Value type
	 */
	public static class DefaultPropertyValue<T> implements PropertyValue<T> {

		private static final long serialVersionUID = -1636789481818203557L;

		/**
		 * Property
		 */
		private final Property<T> property;
		/**
		 * Property value
		 */
		private final T value;

		/**
		 * Cosntructor.
		 * @param property Property
		 * @param value Property value
		 */
		public DefaultPropertyValue(Property<T> property, T value) {
			super();
			this.property = property;
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.property.PropertyBox.PropertyValue#getProperty()
		 */
		@Override
		public Property<T> getProperty() {
			return property;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.core.property.PropertyBox.PropertyValue#getValue()
		 */
		@Override
		public T getValue() {
			return value;
		}

	}

}
