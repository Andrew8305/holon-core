/*
 * Copyright 2016-2017 Axioma srl.
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
package com.holonplatform.core.examples;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.holonplatform.core.Path;
import com.holonplatform.core.Validator;
import com.holonplatform.core.config.ConfigProperty;
import com.holonplatform.core.property.BooleanProperty;
import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertyConfiguration;
import com.holonplatform.core.property.PropertyRenderer;
import com.holonplatform.core.property.PropertyRendererRegistry;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.property.PropertyValuePresenter;
import com.holonplatform.core.property.PropertyValuePresenterRegistry;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.property.TemporalProperty;
import com.holonplatform.core.property.VirtualProperty;
import com.holonplatform.core.temporal.TemporalType;

@SuppressWarnings("unused")
public class ExampleProperty {

	@SuppressWarnings("rawtypes")
	public void identity() {
		// tag::identity[]
		Property.Builder<String, Property<String>, ?> builder = getPropertyBuilder();

		builder.hashCodeProvider(property -> Optional.of(property.getName().hashCode())) // <1>
				.equalsHandler((property, other) -> { // <2>
					if (other instanceof Property)
						return property.getName().equals(((Property) other).getName());
					return false;
				});
		// end::identity[]
	}

	private static <T> Property.Builder<T, Property<T>, ?> getPropertyBuilder() {
		return null;
	}

	public void path() {
		// tag::path[]
		Path<String> stringPath = Path.of("pathName", String.class); // <1>

		String name = stringPath.getName(); // <2>
		boolean root = stringPath.isRootPath(); // <3>

		Path<String> hierarchicalPath = Path.of("subName", String.class).parent(stringPath); // <4>
		String fullName = hierarchicalPath.fullName(); // <5>
		// end::path[]
	}

	public void config() {
		// tag::config[]
		final ConfigProperty<Long> EXAMPLE_CFG = ConfigProperty.create("exampleConfig", Long.class);

		Property.Builder<String, Property<String>, ?> builder = getPropertyBuilder();

		builder.temporalType(TemporalType.DATE_TIME) // <1>
				.configuration("myAttribute", "myValue") // <2>
				.configuration(EXAMPLE_CFG, 7L); // <3>

		PropertyConfiguration cfg = aProperty().getConfiguration(); // <4>
		Optional<String> value1 = cfg.getParameter("myAttribute", String.class); // <5>
		Long value2 = cfg.getParameter(EXAMPLE_CFG, 0L); // <6>
		// end::config[]
	}

	private static Property<?> aProperty() {
		return null;
	}

	@SuppressWarnings("serial")
	public void converter() {
		// tag::converter[]
		PropertyValueConverter<Integer, String> converter = new PropertyValueConverter<Integer, String>() {

			@Override
			public Integer fromModel(String value, Property<Integer> property) throws PropertyConversionException {
				return (value != null) ? Integer.parseInt(value) : null; // <1>
			}

			@Override
			public String toModel(Integer value, Property<Integer> property) throws PropertyConversionException {
				return (value != null) ? String.valueOf(value) : null; // <2>
			}

			@Override
			public Class<Integer> getPropertyType() {
				return Integer.class;
			}

			@Override
			public Class<String> getModelType() {
				return String.class;
			}

		};
		// end::converter[]
	}

	public void converter2() {
		// tag::converter2[]
		Property.Builder<Integer, Property<Integer>, ?> builder = getPropertyBuilder();

		builder.converter(String.class, // <1>
				v -> (v != null) ? Integer.parseInt(v) : null, // <2>
				v -> (v != null) ? String.valueOf(v) : null); // <3>
		// end::converter2[]
	}

	public void builtinConverters() {
		// tag::bultincnv[]
		PropertyValueConverter.numericBoolean(Integer.class); // <1>
		PropertyValueConverter.localDate(); // <2>
		PropertyValueConverter.localDateTime(); // <3>
		PropertyValueConverter.enumByOrdinal(); // <4>
		PropertyValueConverter.enumByName(); // <5>

		Property.Builder<Boolean, Property<Boolean>, ?> builder = getPropertyBuilder();

		builder.converter(PropertyValueConverter.numericBoolean(Integer.class)); // <6>
		// end::bultincnv[]
	}

	public void validators() {
		// tag::validators[]
		Property.Builder<Integer, Property<Integer>, ?> builder = getPropertyBuilder();

		builder.validator(Validator.notNull()) // <1>
				.validator(Validator.lessThan(10)); // <2>
		// end::validators[]
	}

	// tag::pathproperty[]
	public final static PathProperty<Long> ID = PathProperty.create("id", Long.class) // <1>
			.configuration("test", 1) // <2>
			.validator(Validator.notNull()) // <3>
			.message("Identifier") // <4>
			.messageCode("property.id"); // <5>

	public final static PathProperty<Boolean> VALID = PathProperty.create("valid", Boolean.class) // <6>
			.converter(PropertyValueConverter.numericBoolean(Integer.class)); // <7>
	// end::pathproperty[]

	// tag::pathproperty2[]
	public final static PathProperty<String> PARENT_PROPERTY = PathProperty.create("parent", String.class);

	public final static PathProperty<String> A_PROPERTY = PathProperty.create("child", String.class)
			.parent(PARENT_PROPERTY); // <1>
	// end::pathpropert2y[]

	// tag::pathproperty3[]
	public final static StringProperty STRING_PROPERTY = StringProperty.create("name"); // <1>
	public final static NumericProperty<Integer> INTEGER_PROPERTY = NumericProperty.create("name", Integer.class); // <2>
	public final static NumericProperty<Long> LONG_PROPERTY = NumericProperty.longType("name"); // <3>
	public final static TemporalProperty<LocalDate> LDATE_PROPERTY = TemporalProperty.localDate("name"); // <4>
	public final static TemporalProperty<Date> DATE_PROPERTY = TemporalProperty.date("name")
			.temporalType(TemporalType.DATE); // <5>
	public final static BooleanProperty BOOLEAN_PROPERTY = BooleanProperty.create("name"); // <6>
	// end::pathpropert3y[]

	// tag::vrtproperty[]
	public final static VirtualProperty<Integer> ALWAYS_ONE = VirtualProperty.create(Integer.class, propertyBox -> 1); // <1>

	public final static PathProperty<String> NAME = PathProperty.create("name", String.class); // <2>
	public final static PathProperty<String> SURNAME = PathProperty.create("surname", String.class); // <3>
	public final static VirtualProperty<String> FULL_NAME = VirtualProperty.create(String.class,
			propertyBox -> propertyBox.getValue(NAME) + " " + propertyBox.getValue(SURNAME)); // <4>
	// end::vrtproperty[]

	public void propertySet() {
		// tag::propertyset[]
		final StringProperty NAME = StringProperty.create("name");
		final StringProperty SURNAME = StringProperty.create("surname");
		final NumericProperty<Integer> SEQUENCE = NumericProperty.integerType("surname");

		PropertySet<Property<?>> set = PropertySet.of(NAME, SURNAME); // <1>
		set = PropertySet.builder().add(NAME).add(SURNAME).build(); // <2>

		PropertySet<Property<?>> set2 = PropertySet.builder().add(set).add(SEQUENCE).build(); // <3>
		// end::propertyset[]
	}

	public void propertySet2() {
		// tag::propertyset2[]
		final PathProperty<String> NAME = PathProperty.create("name", String.class);
		final PathProperty<String> SURNAME = PathProperty.create("surname", String.class);

		final PropertySet<Property<?>> SET = PropertySet.of(NAME, SURNAME); // <1>

		boolean contains = SET.contains(NAME); // <2>
		SET.forEach(p -> p.toString()); // <3>
		String captions = SET.stream().map(p -> p.getMessage()).collect(Collectors.joining()); // <4>
		List<Property<?>> list = SET.asList(); // <5>
		// end::propertyset2[]
	}

	public void propertySet3() {
		// tag::propertyset3[]
		final NumericProperty<Long> ID = NumericProperty.longType("id");
		final StringProperty NAME = StringProperty.create("name");

		PropertySet<Property<?>> SET = PropertySet.builder().add(ID).add(NAME).identifier(ID).build(); // <1>

		SET = PropertySet.builderOf(ID, NAME).identifier(ID).build(); // <2>

		Set<Property<?>> ids = SET.getIdentifiers(); // <3>
		Optional<Property<?>> id = SET.getFirstIdentifier(); // <4>
		SET.identifiers().forEach(p -> p.toString()); // <5>
		// end::propertyset3[]
	}

	@SuppressWarnings("unchecked")
	public void propertyBox() {
		// tag::propertybox[]
		final PathProperty<Long> ID = PathProperty.create("id", Long.class);
		final StringProperty NAME = StringProperty.create("name");

		final PropertySet<?> PROPERTIES = PropertySet.of(ID, NAME);

		PropertyBox propertyBox = PropertyBox.create(ID, NAME); // <1>
		propertyBox = PropertyBox.create(PROPERTIES); // <2>

		propertyBox.setValue(ID, 1L); // <3>
		propertyBox.setValue(NAME, "testName"); // <4>

		propertyBox = PropertyBox.builder(PROPERTIES).set(ID, 1L).set(NAME, "testName").build(); // <5>

		Long id = propertyBox.getValue(ID); // <6>
		String name = propertyBox.getValueIfPresent(NAME).orElse("default"); // <7>

		boolean containsNotNullId = propertyBox.containsValue(ID); // <8>

		PropertyBox ids = propertyBox.cloneBox(ID); // <9>
		// end::propertybox[]
	}

	public void propertyBox2() {
		// tag::propertybox2[]
		final PathProperty<Long> ID = PathProperty.create("id", Long.class).validator(Validator.notNull()); // <1>
		final StringProperty NAME = StringProperty.create("name").validator(Validator.notBlank()); // <2>

		final PropertySet<?> PROPERTIES = PropertySet.of(ID, NAME);

		PropertyBox propertyBox = PropertyBox.create(PROPERTIES);

		propertyBox.setValue(ID, null); // <3>

		propertyBox = PropertyBox.builder(PROPERTIES).invalidAllowed(true).build(); // <4>

		propertyBox.validate(); // <5>
		// end::propertybox2[]
	}

	public void propertyBox3() {
		// tag::propertybox3[]
		final StringProperty NAME = StringProperty.create("name");
		final StringProperty SURNAME = StringProperty.create("surname");

		final VirtualProperty<String> FULL_NAME = VirtualProperty.create(String.class, propertyBox -> { // <1>
			return propertyBox.getValue(NAME) + " " + propertyBox.getValue(SURNAME);
		});

		PropertyBox propertyBox = PropertyBox.create(NAME, SURNAME, FULL_NAME); // <2>

		propertyBox.setValue(NAME, "John");
		propertyBox.setValue(SURNAME, "Doe");

		String fullName = propertyBox.getValue(FULL_NAME); // <3>
		// end::propertybox3[]
	}

	public void propertyBox4() {
		// tag::propertybox4[]
		final NumericProperty<Long> ID = NumericProperty.longType("id");
		final StringProperty NAME = StringProperty.create("name");

		final PropertySet<?> PROPERTIES = PropertySet.builderOf(ID, NAME).identifier(ID).build(); // <1>

		PropertyBox propertyBox1 = PropertyBox.builder(PROPERTIES).set(ID, 1L).set(NAME, "name1").build();
		PropertyBox propertyBox2 = PropertyBox.builder(PROPERTIES).set(ID, 1L).set(NAME, "name2").build();

		boolean isTrue = propertyBox1.equals(propertyBox2); // <2>
		// end::propertybox4[]
	}

	public void propertyBox5() {
		// tag::propertybox5[]
		final NumericProperty<Integer> ID = NumericProperty.integerType("id");
		final StringProperty NAME = StringProperty.create("name");

		PropertyBox propertyBox = PropertyBox.builder(ID, NAME)
				.hashCodeProvider(pb -> Optional.ofNullable(pb.getValue(ID))) // <1>
				.equalsHandler((pb, other) -> (other instanceof PropertyBox) // <2>
						&& ((PropertyBox) other).getValue(ID).equals(pb.getValue(ID)))
				.build();
		// end::propertybox5[]
	}

	public void presenter0() {
		// tag::presenter0[]
		final NumericProperty<Integer> ID = NumericProperty.integerType("id");

		PropertyValuePresenter<Integer> presenter = getPropertyValuePresenter();

		presenter.present(ID, 123); // <1>
		// end::presenter0[]
	}

	private static PropertyValuePresenter<Integer> getPropertyValuePresenter() {
		return null;
	}

	public void presenter() {
		// tag::presenter[]
		final PathProperty<Long> ID = PathProperty.create("id", Long.class);

		String stringValue = ID.present(1L); // <1>

		stringValue = PropertyValuePresenterRegistry.get().getPresenter(ID)
				.orElseThrow(() -> new IllegalStateException("No presenter available for given property"))
				.present(ID, 1L); // <2>
		// end::presenter[]
	}

	public void presenterRegistration() {
		// tag::presenterreg[]
		PropertyValuePresenter<LocalTime> myPresenter = (p, v) -> v.getHour() + "." + v.getMinute(); // <1>

		PropertyValuePresenterRegistry.get().register(p -> LocalTime.class.isAssignableFrom(p.getType()), myPresenter); // <2>
		// end::presenterreg[]
	}

	// tag::renderer[]
	class MyRenderingType { // <1>

		private final Class<?> propertyType;

		public MyRenderingType(Class<?> propertyType) {
			this.propertyType = propertyType;
		}

	}

	public void render() {
		PropertyRenderer<MyRenderingType, Object> myRenderer = PropertyRenderer.create(MyRenderingType.class,
				p -> new MyRenderingType(p.getType())); // <2>

		PropertyRendererRegistry.get().register(p -> true, myRenderer); // <3>

		final PathProperty<Long> ID = PathProperty.create("id", Long.class);

		MyRenderingType rendered = ID.render(MyRenderingType.class); // <4>
	}
	// end::renderer[]

}
