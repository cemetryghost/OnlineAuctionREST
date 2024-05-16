package com.onlineauction.OnlineAuction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineauction.OnlineAuction.OnlineAuctionApplication;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.service.LotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OnlineAuctionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LotApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotService lotService;

    @Autowired
    private ObjectMapper objectMapper;

    private LotDTO lotDTO;

    @BeforeEach
    void setUp() {
        lotDTO = new LotDTO();
        lotDTO.setId(1L);
        lotDTO.setNameLots("Test Lot");
        lotDTO.setDescriptionLots("Test Description");
        lotDTO.setStartPrice(BigDecimal.valueOf(100.0));
        lotDTO.setStepPrice(BigDecimal.valueOf(10.0));
        lotDTO.setConditionLots("New");
        lotDTO.setStatusLots(StatusLot.AWAITING_CONFIRMATION_LOT);
        lotDTO.setCategoryId(1L);
    }

    @Test
    void testGetAllLots() throws Exception {
        List<LotDTO> lots = Arrays.asList(lotDTO);
        Mockito.when(lotService.getLotsByStatus(null)).thenReturn(lots);

        mockMvc.perform(MockMvcRequestBuilders.get("/lots")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nameLots").value(lotDTO.getNameLots()));
    }

    @Test
    void testGetLotById() throws Exception {
        Mockito.when(lotService.getLotById(1L)).thenReturn(lotDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/lots/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameLots").value(lotDTO.getNameLots()));
    }

    @Test
    void testCreateLot() throws Exception {
        Mockito.when(lotService.createLot(Mockito.any(LotDTO.class), Mockito.any())).thenReturn(lotDTO);

        String lotJson = objectMapper.writeValueAsString(lotDTO);
        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());

        MockMultipartHttpServletRequestBuilder builder = (MockMultipartHttpServletRequestBuilder) MockMvcRequestBuilders.multipart("/lots")
                .file(imageFile)
                .param("lot", lotJson)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nameLots").value(lotDTO.getNameLots()));
    }

    @Test
    void testUpdateLot() throws Exception {
        Mockito.when(lotService.updateLot(Mockito.anyLong(), Mockito.any(LotDTO.class), Mockito.any())).thenReturn(lotDTO);

        String lotJson = objectMapper.writeValueAsString(lotDTO);
        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());

        MockMultipartHttpServletRequestBuilder builder = (MockMultipartHttpServletRequestBuilder) MockMvcRequestBuilders.multipart("/lots/1")
                .file(imageFile)
                .param("lot", lotJson)
                .contentType(MediaType.MULTIPART_FORM_DATA);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mockMvc.perform(builder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameLots").value(lotDTO.getNameLots()));
    }

    @Test
    void testDeleteLot() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/lots/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testUploadImage() throws Exception {
        Mockito.doNothing().when(lotService).uploadImage(Mockito.anyLong(), Mockito.any());

        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/lots/1/image")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Изображение успешно загружено"));
    }
}
