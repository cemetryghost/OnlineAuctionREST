package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Bid;
import com.onlineauction.OnlineAuction.entity.Category;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.exception.LotException;
import com.onlineauction.OnlineAuction.mapper.LotMapper;
import com.onlineauction.OnlineAuction.mapper.MappingContext;
import com.onlineauction.OnlineAuction.repository.BidRepository;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import com.onlineauction.OnlineAuction.service.impl.LotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LotServiceImplTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private LotMapper lotMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MappingContext mappingContext;

    @Mock
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private LotServiceImpl lotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckAndUpdateLotStatusDateClosing() {
        Lot lot = new Lot();
        lot.setClosingDate(LocalDate.now().minusDays(1));
        lot.setStatusLots(StatusLot.ACTIVE_LOT);

        when(lotRepository.findAll()).thenReturn(List.of(lot));

        lotService.checkAndUpdateLotStatusDateClosing();

        assertEquals(StatusLot.COMPLETED_LOT, lot.getStatusLots());
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    void testGetAllLots() {
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getAllLots();

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findAll();
    }

    @Test
    void testGetLotsByCategoryId() {
        Long categoryId = 1L;
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findByCategoryIdId(categoryId)).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getLotsByCategoryId(categoryId);

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findByCategoryIdId(categoryId);
    }

    @Test
    void testGetLotsBySellerId() {
        Long sellerId = 1L;
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findBySellerIdId(sellerId)).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getLotsBySellerId(sellerId);

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findBySellerIdId(sellerId);
    }

    @Test
    void testGetLotsByCurrentSeller() {
        String currentUserLogin = "seller";
        UserAccounts seller = new UserAccounts();
        seller.setId(1L);
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn(currentUserLogin);
        when(userRepository.findByLogin(currentUserLogin)).thenReturn(seller);
        when(lotRepository.findBySellerId_Id(seller.getId())).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getLotsByCurrentSeller();

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findBySellerId_Id(seller.getId());
    }

    @Test
    void testGetCompletedLotsBySellerId() {
        String currentUserLogin = "seller";
        UserAccounts seller = new UserAccounts();
        seller.setId(1L);
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn(currentUserLogin);
        when(userRepository.findByLogin(currentUserLogin)).thenReturn(seller);
        when(lotRepository.findBySellerId_IdAndStatusLots(seller.getId(), StatusLot.COMPLETED_LOT)).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getCompletedLotsBySellerId();

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findBySellerId_IdAndStatusLots(seller.getId(), StatusLot.COMPLETED_LOT);
    }

    @Test
    void testGetActiveLots() {
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findByStatusLots(StatusLot.ACTIVE_LOT)).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getActiveLots();

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findByStatusLots(StatusLot.ACTIVE_LOT);
    }

    @Test
    void testGetActiveLotsByCategoryId() {
        Long categoryId = 1L;
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findByCategoryIdIdAndStatusLots(categoryId, StatusLot.ACTIVE_LOT)).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getActiveLotsByCategoryId(categoryId);

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findByCategoryIdIdAndStatusLots(categoryId, StatusLot.ACTIVE_LOT);
    }

    @Test
    void testGetLotById_Success() {
        Long lotId = 1L;
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        LotDTO result = lotService.getLotById(lotId);

        assertNotNull(result);
    }

    @Test
    void testGetLotById_NotFound() {
        Long lotId = 1L;

        when(lotRepository.findById(lotId)).thenReturn(Optional.empty());

        LotDTO result = lotService.getLotById(lotId);

        assertNull(result);
    }

    @Test
    void testCreateLot_Success() throws IOException {
        UserAccounts user = new UserAccounts();
        user.setRole(Role.SELLER);
        user.setId(1L);
        LotDTO lotDTO = new LotDTO();
        lotDTO.setNameLots("Test Lot");
        lotDTO.setDescriptionLots("Description");
        lotDTO.setStartPrice(BigDecimal.ZERO);
        lotDTO.setStepPrice(BigDecimal.ONE);
        lotDTO.setClosingDate(LocalDate.now());
        lotDTO.setConditionLots("Condition");
        lotDTO.setCategoryId(1L);
        MultipartFile image = new MockMultipartFile("image", new byte[]{1, 2, 3});
        Category category = new Category();
        category.setId(1L);
        Lot lot = new Lot();

        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("seller");
        when(userRepository.findByLogin("seller")).thenReturn(user);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(lotMapper.lotDTOToLot(lotDTO, mappingContext)).thenReturn(lot);
        when(lotRepository.save(any(Lot.class))).thenReturn(lot);
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        LotDTO result = lotService.createLot(lotDTO, image);

        assertNotNull(result);
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    void testUpdateLot_Success() throws IOException {
        Long lotId = 1L;
        Lot existingLot = new Lot();
        existingLot.setId(lotId);
        LotDTO lotDTO = new LotDTO();
        lotDTO.setNameLots("Updated Lot");
        MultipartFile image = new MockMultipartFile("image", new byte[]{1, 2, 3});

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(existingLot));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(new Category()));
        when(lotRepository.save(existingLot)).thenReturn(existingLot);
        when(lotMapper.lotToLotDTO(existingLot)).thenReturn(lotDTO);

        LotDTO result = lotService.updateLot(lotId, lotDTO, image);

        assertNotNull(result);
        verify(lotRepository, times(1)).save(existingLot);
    }

    @Test
    void testDeleteLot_Success() {
        Long lotId = 1L;
        Lot lot = mock(Lot.class);

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(lot.getCurrentBuyerId()).thenReturn(null);

        lotService.deleteLot(lotId);

        verify(lotRepository, times(1)).deleteById(lotId);
    }

    @Test
    void testDeleteLot_ThrowsException_WithBuyer() {
        Long lotId = 1L;
        Lot lot = mock(Lot.class);
        UserAccounts buyer = new UserAccounts();
        buyer.setId(1L);

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(lot.getCurrentBuyerId()).thenReturn(buyer);

        LotException exception = assertThrows(LotException.class, () -> lotService.deleteLot(lotId));

        assertEquals("Удаление лота запрещено, так как есть текущий покупатель", exception.getMessage());
    }

    @Test
    void testUpdateLotStatus_Success() {
        Long lotId = 1L;
        StatusLot newStatus = StatusLot.ACTIVE_LOT;
        Lot existingLot = new Lot();
        existingLot.setId(lotId);

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(existingLot));
        when(lotRepository.save(existingLot)).thenReturn(existingLot);
        when(lotMapper.lotToLotDTO(existingLot)).thenReturn(new LotDTO());

        LotDTO result = lotService.updateLotStatus(lotId, newStatus);

        assertNotNull(result);
        assertEquals(newStatus, existingLot.getStatusLots());
    }

    @Test
    void testUpdateLotStatusesDateClosing() {
        Lot lot = new Lot();
        lot.setClosingDate(LocalDate.now().minusDays(1));
        lot.setStatusLots(StatusLot.ACTIVE_LOT);

        when(lotRepository.findAllByClosingDateBeforeAndStatusLotsNot(any(), eq(StatusLot.COMPLETED_LOT))).thenReturn(List.of(lot));

        lotService.updateLotStatusesDateClosing();

        assertEquals(StatusLot.COMPLETED_LOT, lot.getStatusLots());
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    void testSearchActiveLots() {
        Long categoryId = 1L;
        String keyword = "test";
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findAll(any(Specification.class))).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.searchActiveLots(categoryId, keyword);

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testUploadImage() throws IOException {
        Long lotId = 1L;
        Lot lot = new Lot();
        MultipartFile image = new MockMultipartFile("image", new byte[]{1, 2, 3});

        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));

        lotService.uploadImage(lotId, image);

        verify(lotRepository, times(1)).save(lot);
        assertArrayEquals(image.getBytes(), lot.getImage());
    }

    @Test
    void testGetLotsByStatus() {
        StatusLot status = StatusLot.ACTIVE_LOT;
        Lot lot = new Lot();
        LotDTO lotDTO = new LotDTO();

        when(lotRepository.findByStatusLots(status)).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(lot)).thenReturn(lotDTO);

        List<LotDTO> result = lotService.getLotsByStatus(status);

        assertEquals(1, result.size());
        verify(lotRepository, times(1)).findByStatusLots(status);
    }

    @Test
    void testHandleUserDeletion_WithValidBids() {
        Long userId = 1L;
        Lot lot = new Lot();
        lot.setId(1L);
        List<Lot> lotsWithUserAsCurrentBuyer = List.of(lot);
        Bid bid = new Bid();

        bid.setBidAmount(BigDecimal.valueOf(100.0));
        UserAccounts nextBuyer = new UserAccounts();
        nextBuyer.setId(2L);
        bid.setBuyer(nextBuyer);

        when(lotRepository.findByCurrentBuyerId_Id(userId)).thenReturn(lotsWithUserAsCurrentBuyer);
        when(bidRepository.findAllByLotIdOrderByBidIdDesc(lot.getId())).thenReturn(List.of(bid));
        when(userRepository.findById(nextBuyer.getId())).thenReturn(Optional.of(nextBuyer));

        lotService.handleUserDeletion(userId);

        verify(lotRepository, times(1)).save(lot);
        assertEquals(nextBuyer, lot.getCurrentBuyerId());
        assertEquals(bid.getBidAmount(), lot.getCurrentPrice());
    }

    @Test
    void testHandleUserDeletion_WithoutValidBids() {
        Long userId = 1L;
        Lot lot = new Lot();
        lot.setId(1L);
        List<Lot> lotsWithUserAsCurrentBuyer = List.of(lot);

        when(lotRepository.findByCurrentBuyerId_Id(userId)).thenReturn(lotsWithUserAsCurrentBuyer);
        when(bidRepository.findAllByLotIdOrderByBidIdDesc(lot.getId())).thenReturn(List.of());

        lotService.handleUserDeletion(userId);

        verify(lotRepository, times(1)).save(lot);
        assertNull(lot.getCurrentBuyerId());
        assertNull(lot.getCurrentPrice());
    }
}

