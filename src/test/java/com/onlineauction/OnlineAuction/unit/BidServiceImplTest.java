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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidServiceImplTest {

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

    private Bid bid;
    private BidDTO bidDTO;
    private Lot lot;
    private UserAccounts user;

    @BeforeEach
    public void setUp() {
        lot = new Lot();
        lot.setId(1L);
        lot.setStatusLots(StatusLot.ACTIVE_LOT);
        lot.setStartPrice(BigDecimal.valueOf(100));
        lot.setStepPrice(BigDecimal.valueOf(10));
        lot.setCurrentPrice(BigDecimal.valueOf(110));

        user = new UserAccounts();
        user.setId(1L);

        bid = new Bid();
        bid.setId(1L);
        bid.setLot(lot);
        bid.setBuyer(user);
        bid.setBidAmount(BigDecimal.valueOf(120));

        bidDTO = new BidDTO();
        bidDTO.setId(1L);
        bidDTO.setLotId(1L);
        bidDTO.setBidAmount(BigDecimal.valueOf(120));
    }

    @Test
    public void testGetAllBids() {
        when(bidRepository.findAll()).thenReturn(Arrays.asList(bid));
        when(bidMapper.bidToBidDTO(any(Bid.class))).thenReturn(bidDTO);

        List<BidDTO> result = bidService.getAllBids();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    public void testGetBidsByLotId() {
        when(bidRepository.findByLotId(anyLong())).thenReturn(Arrays.asList(bid));
        when(bidMapper.bidToBidDTO(any(Bid.class))).thenReturn(bidDTO);

        List<BidDTO> result = bidService.getBidsByLotId(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    public void testGetBidById() {
        when(bidRepository.findById(anyLong())).thenReturn(Optional.of(bid));
        when(bidMapper.bidToBidDTO(any(Bid.class))).thenReturn(bidDTO);

        BidDTO result = bidService.getBidById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetBidById_NotFound() {
        when(bidRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertNull(bidService.getBidById(1L));
    }

    @Test
    public void testUpdateBid() {
        when(bidRepository.findById(anyLong())).thenReturn(Optional.of(bid));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);
        when(bidMapper.bidToBidDTO(any(Bid.class))).thenAnswer(invocation -> {
            Bid savedBid = invocation.getArgument(0);
            BidDTO bidDTO = new BidDTO();
            bidDTO.setId(savedBid.getId());
            bidDTO.setBidAmount(savedBid.getBidAmount());
            bidDTO.setLotId(savedBid.getLot().getId());
            return bidDTO;
        });
        when(lotService.getLotById(anyLong())).thenReturn(new LotDTO());

        BigDecimal newBidAmount = BigDecimal.valueOf(130);
        BidDTO result = bidService.updateBid(1L, newBidAmount);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(newBidAmount, result.getBidAmount());
        assertEquals(newBidAmount, lot.getCurrentPrice());
    }

    @Test
    public void testUpdateBid_LotNotActive() {
        lot.setStatusLots(StatusLot.COMPLETED_LOT);
        when(bidRepository.findById(anyLong())).thenReturn(Optional.of(bid));

        assertThrows(BidException.class, () -> bidService.updateBid(1L, BigDecimal.valueOf(130)));
    }

    @Test
    public void testDeleteBid() {
        when(bidRepository.findById(anyLong())).thenReturn(Optional.of(bid));
        doNothing().when(bidRepository).delete(any(Bid.class));

        bidService.deleteBid(1L);

        verify(bidRepository, times(1)).delete(bid);
    }

    @Test
    public void testPlaceBid() {
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));
        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("user1");
        when(userRepository.findByLogin(anyString())).thenReturn(user);
        when(bidMapper.BidDTOtoBid(any(BidDTO.class))).thenReturn(bid);
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);
        when(bidMapper.bidToBidDTO(any(Bid.class))).thenReturn(bidDTO);
        when(lotService.getLotById(anyLong())).thenReturn(new LotDTO());

        BidDTO result = bidService.placeBid(bidDTO);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(120), result.getBidAmount());
    }

    @Test
    public void testPlaceBid_LotNotActive() {
        lot.setStatusLots(StatusLot.COMPLETED_LOT);
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));

        assertThrows(BidException.class, () -> bidService.placeBid(bidDTO));
    }

    @Test
    public void testGetMyBidsWithLotDetails() {
        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("user1");
        when(userRepository.findByLogin(anyString())).thenReturn(user);
        when(bidRepository.findByBuyerId(anyLong())).thenReturn(Arrays.asList(bid));
        when(bidMapper.bidToBidDTO(any(Bid.class))).thenReturn(bidDTO);
        when(lotService.getLotById(anyLong())).thenReturn(new LotDTO());

        List<BidDTO> result = bidService.getMyBidsWithLotDetails();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
