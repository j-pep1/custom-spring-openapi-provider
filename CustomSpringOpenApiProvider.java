/*
 * Copyright 2024 Jonathaan Pepin
 * 
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file or at https://opensource.org/licenses/MIT.
 */
package in.jpep.snippet;

import static java.util.function.Predicate.not;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.webmvc.core.providers.SpringWebMvcProvider;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * <b>Custom OpenAPI 3.0 Provider for Springdoc</b> <br>
 * 
 * The default OpenAPI 3.0 specification is not compatible with REST APIs that
 * have identical paths but different parameters. <br>
 * This issue is discussed in detail here:
 * https://github.com/springdoc/springdoc-openapi/issues/859#issuecomment-690735164
 * <br>
 * 
 * This class extends the default Spring Web MVC provider to append query
 * parameters (e.g., ?paramName=paramValue) to the path for endpoints with
 * identical paths but different parameters. This ensures proper documentation
 * generation in OpenAPI.
 * 
 * <p>
 * Specifically, this provider modifies the active patterns for request mappings
 * by adding the parameter conditions as query parameters to the path. If a
 * request mapping has parameter conditions, these conditions are appended to
 * the path to differentiate between endpoints with the same path but different
 * parameters.
 * </p>
 * 
 * <p>
 * Example: <br>
 * If there are two endpoints with the same path "/{containerId}" but different
 * parameters, such as:
 * <ul>
 * <li>@PutMapping(path = "/{containerId}", params = {"!enabled"})</li>
 * <li>@PutMapping(path = "/{containerId}", params = {"enabled"})</li>
 * </ul>
 * The resulting paths would be:
 * <ul>
 * <li>"/{containerId}?enabled"</li>
 * <li>"/{containerId}?enabled=false"</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This class overrides the
 * {@link SpringWebMvcProvider#getActivePatterns(Object)} method to achieve this
 * functionality.
 * </p>
 * 
 * @see SpringWebMvcProvider
 * @see RequestMappingInfo
 * @see ParamsRequestCondition
 * @see NameValueExpression
 * 
 *      Author: Jonathan Pepin
 */
public class CustomSpringOpenApiProvider extends SpringWebMvcProvider {

    @Override
    public Set<String> getActivePatterns(Object requestMappingInfo) {
	final Set<String> activePatterns = super.getActivePatterns(requestMappingInfo);
	if (requestMappingInfo instanceof RequestMappingInfo mappingInfo) {
	    final ParamsRequestCondition paramsCondition = mappingInfo.getParamsCondition();
	    if (!paramsCondition.isEmpty()) {
		return getParams(activePatterns, paramsCondition);
	    }
	}
	return activePatterns;
    }

    private static Set<String> getParams(final Set<String> patterns, final ParamsRequestCondition paramsCondition) {
	final String params = getParams(paramsCondition);
	if (!params.isEmpty()) {
	    final Set<String> patternsWithParams = new HashSet<>();
	    for (String path : patterns) {
		String delimiter = path.contains("?") ? "&" : "?";
		patternsWithParams.add(path + delimiter + params);
	    }
	    return patternsWithParams;
	}
	return patterns;
    }

    private static String getParams(final ParamsRequestCondition paramsCondition) {
	return paramsCondition.getExpressions().stream().filter(not(NameValueExpression::isNegated))
		.map(CustomSpringOpenApiProvider::paramNameValue).collect(Collectors.joining("&"));
    }

    private static String paramNameValue(NameValueExpression<String> nameValueExpr) {
	String paramName = nameValueExpr.getName();
	return Optional.ofNullable(nameValueExpr.getValue()).map(value -> paramName + "=" + value).orElse(paramName);
    }

}
