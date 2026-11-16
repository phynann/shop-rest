package edu.itc.shop_rest.api.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import edu.itc.shop_rest.domain.Money;

@Provider
public class MoneyConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> raw, Type generic, Annotation[] ann) {
        if (raw != Money.class) {
            return null;
        }

        return new ParamConverter<T>() {
            @Override
            public T fromString(String value) {
                if (value == null) {
                    return null;
                }
                try {
                    return raw.cast(Money.parse(value));
                } catch (IllegalArgumentException e) {
                    // Throw a WebApplicationException yourself: otherwise Jersey turns a bad
                    // @QueryParam into 404 instead of 400.
                    throw new BadRequestException(e.getMessage(), e);
                }
            }

            @Override
            public String toString(T value) {
                return value == null ? null : value.toString();
            }
        };
    }
}