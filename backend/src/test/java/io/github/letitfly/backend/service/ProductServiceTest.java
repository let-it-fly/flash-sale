package io.github.letitfly.backend.service;

import io.github.letitfly.backend.dto.ProductResponse;
import io.github.letitfly.backend.entity.Product;
import io.github.letitfly.backend.exception.ProductNotFoundException;
import io.github.letitfly.backend.mapper.ProductMapper;
import io.github.letitfly.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductsReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Product product = createProduct();
        ProductResponse response = createResponse();

        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product), pageable, 1));
        when(productMapper.toResponse(product)).thenReturn(response);

        Page<ProductResponse> result = productService.getProducts(pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository).findAll(pageable);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductByIdReturnsMappedProduct() {
        Product product = createProduct();
        ProductResponse response = createResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getProductById(1L);

        assertThat(result).isEqualTo(response);
        verify(productRepository).findById(1L);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductByIdThrowsWhenProductNotFound() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(42L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("42");
        verify(productRepository).findById(42L);
        verifyNoInteractions(productMapper);
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setDescription("Smartphone");
        product.setImageUrl("https://example.com/phone.jpg");
        product.setPrice(new BigDecimal("499.99"));
        return product;
    }

    private ProductResponse createResponse() {
        return new ProductResponse(
                1L, "Phone", "Smartphone", "https://example.com/phone.jpg", new BigDecimal("499.99"));
    }
}
