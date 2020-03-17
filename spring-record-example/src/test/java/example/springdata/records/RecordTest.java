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
package example.springdata.records;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;

/**
 * @author Oliver Drotbohm
 */
class RecordTest {

	MappingContext<KeyValuePersistentEntity<?, ?>, ?> context = new KeyValueMappingContext();

	PersistentEntity<?, ?> entity = context.getRequiredPersistentEntity(Point.class);
	PersistentProperty<?> xProperty = entity.getRequiredPersistentProperty("x");
	PersistentProperty<?> yProperty = entity.getRequiredPersistentProperty("y");

	@Test
	void exposesRecordComponentAsPersistentProperty() {

		PreferredConstructor<?, ?> constructor = entity.getPersistenceConstructor();

		assertThat(constructor).isNotNull();

		Stream.of(xProperty, yProperty).forEach(it -> {

			assertThat(constructor.isConstructorParameter(it)).isTrue();
			assertThat(it.getSetter()).isNull();
			assertThat(it.getGetter()).isNotNull();
			assertThat(it.isImmutable()).isTrue();
		});
	}

	@Test
	void createsNewRecordInstanceIfPropertyIsSet() {

		var point = new Point(10, 20);

		PersistentPropertyAccessor<Point> accessor = entity.getPropertyAccessor(point);

		assertThat(accessor.getProperty(xProperty)).isEqualTo(10);

		accessor.setProperty(xProperty, 20);
		Point result = accessor.getBean();

		assertThat(result.x()).isEqualTo(20);
	}
}
