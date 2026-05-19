package dev.lumas.shops.components.backing.factories;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.lumas.core.annotation.ReflectIgnore;
import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.Product;

@ReflectIgnore
public class ProductCodec extends EnumTypeCodec<Products, Product> {

    public ProductCodec(Gson gson) {
        super(gson);
    }

    @Override
    public TypeToken<Product> type() {
        return TypeToken.get(Product.class);
    }

    @Override
    protected Class<Products> enumClass() {
        return Products.class;
    }

    @Override
    protected Class<?> inputTypeOf(Products type) {
        return type.type();
    }

    @Override
    protected Product construct(Products type, Object value) {
        return type.create(value);
    }
}