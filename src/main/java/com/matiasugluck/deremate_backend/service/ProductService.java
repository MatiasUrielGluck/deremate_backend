package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.product.ProductDTO;

import java.util.List;

public interface ProductService {
    GenericResponseDTO<ProductDTO> createProduct(ProductDTO productDTO);
    GenericResponseDTO<String> updateProduct(Long id, ProductDTO productDTO);
    GenericResponseDTO<List<ProductDTO>> getProducts();
    GenericResponseDTO<ProductDTO> getProductById(Long id);
    GenericResponseDTO<String> deleteProduct(Long id);
}
