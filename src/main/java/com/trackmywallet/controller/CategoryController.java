package com.trackmywallet.controller;

import com.trackmywallet.dto.ApiResponse;
import com.trackmywallet.dto.CategoryRequest;
import com.trackmywallet.dto.CategoryResponse;
import com.trackmywallet.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategoryController - REST endpoints for managing user expense categories.
 * All actions are authenticated and scoped to the user stored in the session.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthController.SESSION_USER_ID) == null) {
            throw new IllegalStateException("User session not found. Please log in.");
        }
        return (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(HttpServletRequest request) {
        Integer userId = getSessionUserId(request);
        List<CategoryResponse> list = categoryService.getCategoriesForUser(userId);
        return ResponseEntity.ok(ApiResponse.ok("Categories fetched successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable("id") Integer id,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            CategoryResponse response = categoryService.getCategoryById(userId, id);
            return ResponseEntity.ok(ApiResponse.ok("Category retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            CategoryResponse response = categoryService.createCategory(userId, categoryRequest);
            return ResponseEntity.ok(ApiResponse.ok("Category created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to create category: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable("id") Integer id,
            @Valid @RequestBody CategoryRequest categoryRequest,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            CategoryResponse response = categoryService.updateCategory(userId, id, categoryRequest);
            return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to update category: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable("id") Integer id,
            HttpServletRequest request) {
        try {
            Integer userId = getSessionUserId(request);
            categoryService.deleteCategory(userId, id);
            return ResponseEntity.ok(ApiResponse.ok("Category deleted successfully"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete category: " + e.getMessage()));
        }
    }
}
