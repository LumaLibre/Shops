package dev.lumas.shops.components.backing.factories;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.lumas.core.annotation.ReflectIgnore;
import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;

@ReflectIgnore
public class CurrencyCodec extends EnumTypeCodec<Currencies, Currency<?>> {

    public CurrencyCodec(Gson gson) {
        super(gson);
    }

    @Override
    public TypeToken<Currency<?>> type() {
        return new TypeToken<>() {};
    }

    @Override
    protected Class<Currencies> enumClass() {
        return Currencies.class;
    }

    @Override
    protected Class<?> inputTypeOf(Currencies type) {
        return type.amountType();
    }

    @Override
    protected Currency<?> construct(Currencies type, Object value) {
        return type.create(value);
    }
}