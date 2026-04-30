package co.edu.cesde.pps.controller;

import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDTO> getProducts() {
        return productService.findAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        try {
            ProductDTO product = productService.findById(id);
            return ResponseEntity.ok(product);
        } catch (EntityNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("An unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}

