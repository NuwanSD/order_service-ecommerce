package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductResponseDTO;
import com.ecommerce.orderservice.exception.ProductServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate,
                                 @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductResponseDTO getProductById(Long productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            return restTemplate.getForObject(url, ProductResponseDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductServiceException("Product not found with ID: " + productId);
        } catch (Exception e) {
            throw new ProductServiceException("Failed to fetch product from Product Service: " + e.getMessage());
        }
    }
}
