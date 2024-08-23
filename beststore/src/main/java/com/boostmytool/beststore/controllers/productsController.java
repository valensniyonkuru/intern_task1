package com.boostmytool.beststore.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.boostmytool.beststore.models.product;
import com.boostmytool.beststore.models.productDto;
import com.boostmytool.beststore.services.productsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class productsController {

    @Autowired
    private productsRepository repo;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {  
        List<product> products = repo.findAll();  
        model.addAttribute("products", products); 
        return "products/index";
    }

    @GetMapping("/search")
    public String searchProductById(@RequestParam int id, Model model) {
        product product = repo.findById(id).orElse(null);  
        if (product != null) {
            model.addAttribute("products", List.of(product));  
        } else {
            model.addAttribute("products", List.of());  
            model.addAttribute("searchMessage", "No product found with ID: " + id);
        }
        return "products/index";  
    }

    @GetMapping("/view")
    public String viewProduct(@RequestParam int id, Model model) {
        product product = repo.findById(id).orElse(null);
        if (product != null) {
            model.addAttribute("product", product);
            return "products/ViewProduct";
        } else {
            model.addAttribute("searchMessage", "No product found with ID: " + id);
            return "redirect:/products";  // Redirect to the product listing if the product is not found
        }
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        productDto productDto = new productDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
        @Valid @ModelAttribute productDto productDto,
        BindingResult result
    ) {
        if (productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required."));
        }
        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        // Save image file
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        product product = new product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);
        repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(
        Model model,
        @RequestParam int id
    ) {
        try {
            product product = repo.findById(id).get();
            model.addAttribute("product", product);
            productDto productDto = new productDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());
            model.addAttribute("productDto", productDto);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(
        Model model,
        @RequestParam int id,
        @Valid @ModelAttribute productDto productDto,
        BindingResult result
    ) {
        try {
            product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            // Directory where images are stored
            String uploadDir = "public/images/";

            // Check if a new image is uploaded
            if (!productDto.getImageFile().isEmpty()) {
                // Delete old image
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // Save new image file
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }

            // Update the product details
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            repo.save(product);

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            product product = repo.findById(id).get();
            // delete product image
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
            repo.delete(product);

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/products";
    }
}
