package com.onlineauction.OnlineAuction.unit;


import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Bid;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.exception.BidException;
import com.onlineauction.OnlineAuction.mapper.BidMapper;
import com.onlineauction.OnlineAuction.repository.BidRepository;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.LotService;
import com.onlineauction.OnlineAuction.service.impl.BidServiceImpl;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BidServiceImplTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BidMapper bidMapper;

    @Mock
    private LotService lotService;

    @Mock
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @InjectMocks
    private BidServiceImpl bidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBids() {
        Bid bid = new Bid();
        BidDTO bidDTO = new BidDTO();

        when(bidRepository.findAll()).thenReturn(List.of(bid));
        when(bidMapper.bidToBidDTO(bid)).thenReturn(bidDTO);

        List<BidDTO> result = bidService.getAllBids();

        assertEquals(1, result.size());
        verify(bidRepository, times(1)).findAll();
    }

    @Test
    void testGetBidsByLotId() {
        Long lotId = 1L;
        Bid bid = new Bid();
        BidDTO bidDTO = new BidDTO();

        when(bidRepository.findByLotId(lotId)).thenReturn(List.of(bid));
        when(bidMapper.bidToBidDTO(bid)).thenReturn(bidDTO);

        List<BidDTO> result = bidService.getBidsByLotId(lotId);

        assertEquals(1, result.size());
        verify(bidRepository, times(1)).findByLotId(lotId);
    }

    @Test
    void testGetBidById_Success() {
        Long bidId = 1L;
        Bid bid = new Bid();
        BidDTO bidDTO = new BidDTO();

        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        when(bidMapper.bidToBidDTO(bid)).thenReturn(bidDTO);

        BidDTO result = bidService.getBidById(bidId);

        assertNotNull(result);
        verify(bidRepository, times(1)).findById(bidId);
    }

    @Test
    void testGetBidById_NotFound() {
        Long bidId = 1L;

        when(bidRepository.findById(bidId)).thenReturn(Optional.empty());

        BidDTO result = bidService.getBidById(bidId);

        assertNull(result);
    }

    @Test
    void testUpdateBid_Success() {
        Long bidId = 1L;
        BigDecimal newBidAmount = BigDecimal.valueOf(200);
        Bid existingBid = new Bid();
        Lot lot = new Lot();
        lot.setStatusLots(StatusLot.ACTIVE_LOT);
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setStepPrice(BigDecimal.valueOf(10));
        existingBid.setLot(lot);
        BidDTO bidDTO = new BidDTO();

        when(bidRepository.findById(bidId)).thenReturn(Optional.of(existingBid));
        when(bidRepository.save(existingBid)).thenReturn(existingBid);
        when(bidMapper.bidToBidDTO(existingBid)).thenReturn(bidDTO);
        when(lotService.getLotById(lot.getId())).thenReturn(new LotDTO());

        BidDTO result = bidService.updateBid(bidId, newBidAmount);

        assertNotNull(result);
        assertEquals(newBidAmount, existingBid.getBidAmount());
        assertEquals(newBidAmount, lot.getCurrentPrice());
        verify(bidRepository, times(1)).save(existingBid);
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    void testUpdateBid_Failure_InvalidAmount() {
        Long bidId = 1L;
        BigDecimal newBidAmount = BigDecimal.valueOf(105);
        Bid existingBid = new Bid();
        Lot lot = new Lot();
        lot.setStatusLots(StatusLot.ACTIVE_LOT);
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setStepPrice(BigDecimal.valueOf(10));
        existingBid.setLot(lot);

        when(bidRepository.findById(bidId)).thenReturn(Optional.of(existingBid));

        BidException exception = assertThrows(BidException.class, () -> bidService.updateBid(bidId, newBidAmount));

        assertEquals("Новая сумма ставки должна быть больше или равна текущей сумме ставки плюс цена шага", exception.getMessage());
    }

    @Test
    void testDeleteBid_Success() {
        Long bidId = 1L;
        Bid existingBid = new Bid();

        when(bidRepository.findById(bidId)).thenReturn(Optional.of(existingBid));

        bidService.deleteBid(bidId);

        verify(bidRepository, times(1)).delete(existingBid);
    }

    @Test
    void testPlaceBid_Success() {
        Long lotId = 1L;
        Lot lot = new Lot();
        lot.setId(lotId);
        lot.setStatusLots(StatusLot.ACTIVE_LOT);
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setStepPrice(BigDecimal.valueOf(10));
        UserAccounts buyer = new UserAccounts();
        buyer.setId(1L);
        BidDTO bidDTO = new BidDTO();
        bidDTO.setLotId(lotId);
        bidDTO.setBidAmount(BigDecimal.valueOf(110));
        Bid bid = new Bid();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("buyer");
        when(userRepository.findByLogin("buyer")).thenReturn(buyer);
        when(bidMapper.BidDTOtoBid(bidDTO)).thenReturn(bid);
        when(bidRepository.save(bid)).thenReturn(bid);
        when(bidMapper.bidToBidDTO(bid)).thenReturn(bidDTO);
        when(lotService.getLotById(lotId)).thenReturn(new LotDTO());

        BidDTO result = bidService.placeBid(bidDTO);

        assertNotNull(result);
        verify(bidRepository, times(1)).save(bid);
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    void testPlaceBid_Failure_InvalidAmount() {
        Long lotId = 1L;
        Lot lot = new Lot();
        lot.setId(lotId);
        lot.setStatusLots(StatusLot.ACTIVE_LOT);
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setStepPrice(BigDecimal.valueOf(10));
        UserAccounts buyer = new UserAccounts();
        buyer.setId(1L);
        BidDTO bidDTO = new BidDTO();
        bidDTO.setLotId(lotId);
        bidDTO.setBidAmount(BigDecimal.valueOf(105));
        Bid bid = new Bid();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("buyer");
        when(userRepository.findByLogin("buyer")).thenReturn(buyer);
        when(bidMapper.BidDTOtoBid(bidDTO)).thenReturn(bid);

        BidException exception = assertThrows(BidException.class, () -> bidService.placeBid(bidDTO));

        assertEquals("Новая сумма ставки должна быть больше или равна текущей сумме ставки плюс цена шага", exception.getMessage());
    }

    @Test
    void testGetMyBidsWithLotDetails() {
        UserAccounts buyer = new UserAccounts();
        buyer.setId(1L);
        Bid bid = new Bid();
        bid.setBuyer(buyer);
        Lot lot = new Lot();
        lot.setId(1L);
        bid.setLot(lot);
        BidDTO bidDTO = new BidDTO();
        LotDTO lotDTO = new LotDTO();

        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("buyer");
        when(userRepository.findByLogin("buyer")).thenReturn(buyer);
        when(bidRepository.findByBuyerId(buyer.getId())).thenReturn(List.of(bid));
        when(bidMapper.bidToBidDTO(bid)).thenReturn(bidDTO);
        when(lotService.getLotById(lot.getId())).thenReturn(lotDTO);

        List<BidDTO> result = bidService.getMyBidsWithLotDetails();

        assertEquals(1, result.size());
        verify(bidRepository, times(1)).findByBuyerId(buyer.getId());
    }
}
