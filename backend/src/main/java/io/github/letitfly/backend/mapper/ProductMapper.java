package io.github.letitfly.backend.mapper;

import io.github.letitfly.backend.dto.ProductResponse;
import io.github.letitfly.backend.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);
}
