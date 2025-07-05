package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.product.ProductDTO;
import com.matiasugluck.deremate_backend.dto.product.ProductListResponseDTO;
import com.matiasugluck.deremate_backend.exception.ApiError;
import com.matiasugluck.deremate_backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "${base-path-v1}/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints para gestionar productos")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Crear un nuevo producto", description = "Registra un nuevo producto en el sistema.")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping
    public ResponseEntity<GenericResponseDTO<ProductDTO>> createProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(productDTO));
    }

    @Operation(summary = "Obtener todos los productos", description = "Devuelve una lista de todos los productos registrados.")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductListResponseDTO.class)))
    @GetMapping
    public ResponseEntity<GenericResponseDTO<List<ProductDTO>>> getAllProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @Operation(summary = "Obtener un producto por ID", description = "Recupera los detalles de un producto específico usando su ID.")
    @ApiResponse(responseCode = "200", description = "Detalles del producto obtenidos exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponseDTO<ProductDTO>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Eliminar un producto por ID", description = "Elimina un producto del sistema usando su ID.")
    @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponseDTO<String>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @Operation(summary = "Actualizar un producto por ID", description = "Actualiza los datos de un producto existente usando su ID.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}")
    public ResponseEntity<GenericResponseDTO<String>> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }
}
