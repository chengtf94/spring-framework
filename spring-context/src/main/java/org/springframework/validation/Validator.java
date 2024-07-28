/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.validation;

/**
 * Validator：校验接口
 *
 * @author Rod Johnson
 * @see SmartValidator
 * @see Errors
 * @see ValidationUtils
 * @see DataBinder#setValidator
 */
public interface Validator {

	/**
	 * Can this {@link Validator} {@link #validate(Object, Errors) validate} instances of the supplied {@code clazz}?
	 */
	boolean supports(Class<?> clazz);

	/**
	 * Validate the given {@code target} object which must be of a {@link Class} for which the {@link #supports(Class)} method
	 * typically has returned (or would return) {@code true}.
	 */
	void validate(Object target, Errors errors);

}
