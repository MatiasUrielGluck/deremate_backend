package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.product.ProductDTO;
import com.matiasugluck.deremate_backend.entity.Product;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.ProductRepository;
import com.matiasugluck.deremate_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public GenericResponseDTO<ProductDTO> createProduct(ProductDTO productDTO) {
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .build();
        Product savedProduct = productRepository.save(product);
        GenericResponseDTO<ProductDTO> response = new GenericResponseDTO<>();
        response.setData(savedProduct.toDto());
        response.setMessage("Product created");
        return response;
    }

    @Override
    public GenericResponseDTO<String> updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found",
                        HttpStatus.NOT_FOUND.value()));

        product.setName(productDTO.getName() != null ? productDTO.getName() : product.getName());
        product.setDescription(productDTO.getDescription() != null ? productDTO.getDescription() : product.getDescription());
        product.setPrice(productDTO.getPrice() != null ? productDTO.getPrice() : product.getPrice());
        productRepository.save(product);
        GenericResponseDTO<String> responseDTO = new GenericResponseDTO<>();
        responseDTO.setMessage("OK");
        return responseDTO;
    }

    @Override
    public GenericResponseDTO<List<ProductDTO>> getProducts() {
        List<Product> products = productRepository.findAll();
        GenericResponseDTO<List<ProductDTO>> response = new GenericResponseDTO<>();
        response.setData(products.stream().map(Product::toDto).toList());
        return response;
    }

    @Override
    public GenericResponseDTO<ProductDTO> getProductById(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found",
                        HttpStatus.NOT_FOUND.value()));
        GenericResponseDTO<ProductDTO> response = new GenericResponseDTO<>();
        response.setData(product.toDto());
        return response;
    }

    @Override
    public GenericResponseDTO<String> deleteProduct(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found",
                        HttpStatus.NOT_FOUND.value()));
        productRepository.delete(product);
        GenericResponseDTO<String> response = new GenericResponseDTO<>();
        response.setMessage("PRODUCT DELETED");
        return response;
    }
}
