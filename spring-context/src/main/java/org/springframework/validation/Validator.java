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
 * Validator：校验器接口，通过编程的方式校验目标对象
 *
 * @author Rod Johnson
 * @see SmartValidator
 * @see Errors
 * @see ValidationUtils
 * @see DataBinder#setValidator
 */
public interface Validator {

	/**
	 * 目标类能否校验
	 */
	boolean supports(Class<?> clazz);

	/**
	 * 校验目标对象，并将校验失败的信息输出到Errors对象
	 */
	void validate(Object target, Errors errors);

}
