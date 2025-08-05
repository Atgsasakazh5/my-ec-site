package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.InventoryDto;
import com.github.Atgsasakazh5.my_ec_site.dto.SkuDto;
import com.github.Atgsasakazh5.my_ec_site.dto.SkuUpdateRequestDto;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AdminSkuControllerTest {

    @MockitoBean
    ProductService productService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("SKUの更新に成功すること")
    @WithMockUser(roles = "ADMIN")
    void updateSkuSuccess() throws Exception {
        // Arrange
        var request = new SkuUpdateRequestDto(
                1L, "S", "Red", 500, 10
        );
        var expectedResponse = new SkuDto(
                1L, "S", "Red", 500, new InventoryDto(10)
        );

        when(productService.updateSku(anyLong(), any(SkuUpdateRequestDto.class)))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/admin/skus/{skuId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.size").value(expectedResponse.size()))
                .andExpect(jsonPath("$.color").value(expectedResponse.color()))
                .andExpect(jsonPath("$.extraPrice").value(expectedResponse.extraPrice()))
                .andExpect(jsonPath("$.inventory.quantity").value(expectedResponse.inventory().quantity()));
    }

    @Test
    @DisplayName("Admin以外のユーザーがSKUを更新しようとすると403エラーが返されること")
    @WithMockUser(roles = "USER")
    void updateSku_returnForbidden_whenNotAdminUseUpdateSku() throws Exception {
        // Arrange
        var request = new SkuUpdateRequestDto(
                1L, "S", "Red", 500, 10
        );
        when(productService.updateSku(anyLong(), any(SkuUpdateRequestDto.class)))
                .thenReturn(new SkuDto(1L, "S", "Red", 500, new InventoryDto(10)));

        // Act & Assert
        mockMvc.perform(put("/api/admin/skus/{skuId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("存在しないSKUを更新しようとすると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void updateSku_returnNotFound_whenSkuIdNotFound() throws Exception {

        // Arrange
        var request = new SkuUpdateRequestDto(
                1L, "S", "Red", 500, 10
        );
        when(productService.updateSku(anyLong(), any(SkuUpdateRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("SKUは存在しません"));

        // Act & Assert
        mockMvc.perform(put("/api/admin/skus/{skuId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SKUの削除に成功すること")
    @WithMockUser(roles = "ADMIN")
    void deleteSkuSuccess() throws Exception {
        // Arrange
        var skuId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/admin/skus/{skuId}", skuId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("存在しないSKUを削除しようとすると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void deleteSku_returnNotFound_whenSkuIdNotFound() throws Exception {
        // Arrange
        var skuId = 1L;
        doThrow(new ResourceNotFoundException("SKUは存在しません"))
                .when(productService).deleteSku(skuId);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/skus/{skuId}", skuId))
                .andExpect(status().isNotFound());
    }
}