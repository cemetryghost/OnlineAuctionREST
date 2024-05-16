package com.onlineauction.OnlineAuction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineauction.OnlineAuction.OnlineAuctionApplication;
import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.service.BidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
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
public class BidApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BidService bidService;

    @Autowired
    private ObjectMapper objectMapper;

    private BidDTO bidDTO;

    @BeforeEach
    void setUp() {
        bidDTO = new BidDTO();
        bidDTO.setId(1L);
        bidDTO.setBidAmount(BigDecimal.valueOf(150.0));
        bidDTO.setLotId(1L);
    }

    @Test
    void testGetAllBids() throws Exception {
        List<BidDTO> bids = Arrays.asList(bidDTO);
        Mockito.when(bidService.getAllBids()).thenReturn(bids);

        mockMvc.perform(MockMvcRequestBuilders.get("/bids")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bidAmount").value(bidDTO.getBidAmount()));
    }

    @Test
    void testGetBidById() throws Exception {
        Mockito.when(bidService.getBidById(1L)).thenReturn(bidDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/bids/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidAmount").value(bidDTO.getBidAmount()));
    }

    @Test
    void testIncreaseBid() throws Exception {
        BigDecimal newBidAmount = BigDecimal.valueOf(200.0);
        bidDTO.setBidAmount(newBidAmount);
        Mockito.when(bidService.updateBid(Mockito.anyLong(), Mockito.any(BigDecimal.class))).thenReturn(bidDTO);

        mockMvc.perform(MockMvcRequestBuilders.put("/bids/1/increase")
                        .param("newBidAmount", newBidAmount.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidAmount").value(newBidAmount));
    }

    @Test
    void testPlaceBid() throws Exception {
        Mockito.when(bidService.placeBid(Mockito.any(BidDTO.class))).thenReturn(bidDTO);

        String bidJson = objectMapper.writeValueAsString(bidDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/bids")
                        .content(bidJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidAmount").value(bidDTO.getBidAmount()));
    }

    @Test
    void testDeleteBid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/bids/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetBidsByLotId() throws Exception {
        List<BidDTO> bids = Arrays.asList(bidDTO);
        Mockito.when(bidService.getBidsByLotId(1L)).thenReturn(bids);

        mockMvc.perform(MockMvcRequestBuilders.get("/bids/lot/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bidAmount").value(bidDTO.getBidAmount()));
    }

    @Test
    void testGetMyBidsWithLotDetails() throws Exception {
        List<BidDTO> bids = Arrays.asList(bidDTO);
        Mockito.when(bidService.getMyBidsWithLotDetails()).thenReturn(bids);

        mockMvc.perform(MockMvcRequestBuilders.get("/bids/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bidAmount").value(bidDTO.getBidAmount()));
    }
}
