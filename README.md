# Custom OpenAPI 3.0 Provider for Springdoc

## Purpose

The default OpenAPI 3.0 specification is not compatible with REST APIs that have identical paths but different parameters. This issue is discussed in detail [here](https://github.com/springdoc/springdoc-openapi/issues/859#issuecomment-690735164).

This class extends the default Spring Web MVC provider to append query parameters (e.g., `?paramName=paramValue`) to the path for endpoints with identical paths but different parameters. This ensures proper documentation generation in OpenAPI.

Specifically, this provider modifies the active patterns for request mappings by adding the parameter conditions as query parameters to the path. If a request mapping has parameter conditions, these conditions are appended to the path to differentiate between endpoints with the same path but different parameters.

### Example

If there are three endpoints with the same path `"/api/something/{id}"` but different parameters, such as:

- `@GetMapping(path = "/api/something/{id}", params = {"foo"})`
- `@GetMapping(path = "/api/something/{id}", params = {"bar"})`
- `@GetMapping(path = "/api/something/{id}", params = {"!foo", "!bar"})`

The resulting paths would be:

- `"/api/something/{id}?foo"`
- `"/api/something/{id}?bar"`
- `"/api/something/{id}"`

This class overrides the `SpringWebMvcProvider#getActivePatterns(Object)` method to achieve this functionality.


## Get started

### Spring Boot 3 Maven Dependency
https://springdoc.org

```
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>${springdoc.version}</version>
</dependency>
```
### Spring Configuration

```
@Configuration
public class OpenApiConfig {

  @Bean
  public SpringWebProvider openApiProvider() {
    return new CustomSpringOpenApiProvider();
  }

}
```

Author: Jonathan Pepin
