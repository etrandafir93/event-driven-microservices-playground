# Questions, Ideas and Things to Clarify



## Questions 

## Notes & Ideas
1. ADRs are interesting in context of AI agents for code generation. 
They can act as a form of documentation that helps agents understand 
the design decisions and constraints for generating code.


2. The `@Observed` + `@NewSpan` will result in 2 spans: 

```java
@Observed
interface ProductCatalog {
  @NewSpan
  Optional<Product> findBySku(@SpanTag(key = "sku", expression = "value") ProductSku sku);
}
```
I've added @NewSpan to the method because i want a dynamic tag,
but it doubled the spans in the trace.
Can I have a @Observed + @SpanTag somehow? but without doubling the spans?

3. Ideas for other services:
 - native compilation?
 - 