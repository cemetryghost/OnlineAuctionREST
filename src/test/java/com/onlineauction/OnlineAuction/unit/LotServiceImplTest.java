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
import com.onlineauction.OnlineAuction.mapper.context.MappingContext;
import com.onlineauction.OnlineAuction.repository.BidRepository;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import com.onlineauction.OnlineAuction.service.impl.LotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotServiceImplTest {

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
    private LotServiceImpl lotServiceImpl;

    private LotDTO lotDTO;
    private Lot lot;
    private UserAccounts user;
    private Category category;

    @BeforeEach
    public void setUp() {
        lotDTO = new LotDTO();
        lotDTO.setNameLots("Test Lot");
        lotDTO.setDescriptionLots("Description");
        lotDTO.setStartPrice(BigDecimal.valueOf(100.0));
        lotDTO.setStepPrice(BigDecimal.valueOf(10.0));
        lotDTO.setClosingDate(LocalDate.now().plusDays(10));
        lotDTO.setConditionLots("New");
        lotDTO.setCategoryId(1L);

        lot = new Lot();
        lot.setId(1L);
        lot.setNameLots("Test Lot");
        lot.setDescriptionLots("Description");
        lot.setStartPrice(BigDecimal.valueOf(100.0));
        lot.setStepPrice(BigDecimal.valueOf(10.0));
        lot.setClosingDate(LocalDate.now().plusDays(10));
        lot.setConditionLots("New");
        lot.setCategoryId(new Category());
        lot.setStatusLots(StatusLot.ACTIVE_LOT);

        user = new UserAccounts();
        user.setId(1L);
        user.setLogin("seller");
        user.setRole(Role.SELLER);

        category = new Category();
        category.setId(1L);
    }

    @Test
    public void testGetAllLots() {
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getAllLots();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetLotById() {
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        LotDTO result = lotServiceImpl.getLotById(1L);

        assertNotNull(result);
        assertEquals("Test Lot", result.getNameLots());
    }

    @Test
    public void testCreateLot() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("image data".getBytes());

        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("seller");
        when(userRepository.findByLogin(anyString())).thenReturn(user);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(lotMapper.lotDTOToLot(any(LotDTO.class), any(MappingContext.class))).thenReturn(lot);
        when(lotRepository.save(any(Lot.class))).thenReturn(lot);
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        LotDTO result = lotServiceImpl.createLot(lotDTO, mockFile);

        assertNotNull(result);
        assertEquals("Test Lot", result.getNameLots());
        verify(lotRepository, times(1)).save(any(Lot.class));
    }

    @Test
    public void testUpdateLot() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("image data".getBytes());

        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(lotRepository.save(any(Lot.class))).thenReturn(lot);
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);


        LotDTO result = lotServiceImpl.updateLot(1L, lotDTO, mockFile);

        assertNotNull(result);
        assertEquals("Test Lot", result.getNameLots());
        verify(lotRepository, times(1)).save(any(Lot.class));
    }

    @Test
    public void testDeleteLot() {
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));
        doNothing().when(lotRepository).deleteById(anyLong());

        lotServiceImpl.deleteLot(1L);

        verify(lotRepository, times(1)).deleteById(anyLong());
    }

    @Test
    public void testDeleteLot_WithCurrentBuyer() {
        lot.setCurrentBuyerId(user);

        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));

        assertThrows(LotException.class, () -> lotServiceImpl.deleteLot(1L));
    }

    @Test
    public void testCheckAndUpdateLotStatusDateClosing() {
        lot.setClosingDate(LocalDate.now().minusDays(1));
        when(lotRepository.findAll()).thenReturn(List.of(lot));
        lotServiceImpl.checkAndUpdateLotStatusDateClosing();
        assertEquals(StatusLot.COMPLETED_LOT, lot.getStatusLots());
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    public void testUpdateLotStatusesDateClosing() {
        lot.setClosingDate(LocalDate.now().minusDays(1));
        when(lotRepository.findAllByClosingDateBeforeAndStatusLotsNot(any(LocalDate.class), any(StatusLot.class))).thenReturn(List.of(lot));
        lotServiceImpl.updateLotStatusesDateClosing();
        assertEquals(StatusLot.COMPLETED_LOT, lot.getStatusLots());
        verify(lotRepository, times(1)).save(lot);
    }

    @Test
    public void testSearchActiveLots() {
        when(lotRepository.findAll(any(Specification.class))).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.searchActiveLots(1L, "keyword");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetLotsByStatus() {
        when(lotRepository.findByStatusLots(any(StatusLot.class))).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getLotsByStatus(StatusLot.ACTIVE_LOT);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetLotsByCategoryId() {
        when(lotRepository.findByCategoryIdId(anyLong())).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getLotsByCategoryId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetLotsByCurrentSeller() {
        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("seller");
        when(userRepository.findByLogin(anyString())).thenReturn(user);
        when(lotRepository.findBySellerId_Id(anyLong())).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getLotsByCurrentSeller();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetCompletedLotsBySellerId() {
        when(customUserDetailsServiceImpl.getCurrentUserLogin()).thenReturn("seller");
        when(userRepository.findByLogin(anyString())).thenReturn(user);
        when(lotRepository.findBySellerId_IdAndStatusLots(anyLong(), any(StatusLot.class))).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getCompletedLotsBySellerId();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetActiveLots() {
        when(lotRepository.findByStatusLots(any(StatusLot.class))).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getActiveLots();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testGetActiveLotsByCategoryId() {
        when(lotRepository.findByCategoryIdIdAndStatusLots(anyLong(), any(StatusLot.class))).thenReturn(List.of(lot));
        when(lotMapper.lotToLotDTO(any(Lot.class))).thenReturn(lotDTO);

        List<LotDTO> result = lotServiceImpl.getActiveLotsByCategoryId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Lot", result.get(0).getNameLots());
    }

    @Test
    public void testUploadImage() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("image data".getBytes());
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));

        lotServiceImpl.uploadImage(1L, mockFile);

        verify(lotRepository, times(1)).save(any(Lot.class));
    }

    @Test
    public void testGetLotImage() {
        byte[] imageData = "image data".getBytes();
        lot.setImage(imageData);
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));

        byte[] result = lotServiceImpl.getLotImage(1L);

        assertNotNull(result);
        assertArrayEquals(imageData, result);
    }

    @Test
    public void testGetLotImage_NoImage() {
        when(lotRepository.findById(anyLong())).thenReturn(Optional.of(lot));

        assertThrows(LotException.class, () -> lotServiceImpl.getLotImage(1L));
    }

    @Test
    public void testHandleUserDeletion() {
        when(lotRepository.findByCurrentBuyerId_Id(anyLong())).thenReturn(Collections.emptyList());

        lotServiceImpl.handleUserDeletion(1L);

        verify(lotRepository, never()).save(any(Lot.class));
    }

    @Test
    public void testUpdateLotForNextHighestBid() {

        UserAccounts user = new UserAccounts();
        user.setId(1L);

        Lot lotWithBuyer = new Lot();
        lotWithBuyer.setId(1L);
        lotWithBuyer.setCurrentBuyerId(user);
        lotWithBuyer.setCurrentPrice(BigDecimal.valueOf(100.0)); // Начальная цена

        UserAccounts nextHighestBidder = new UserAccounts();
        nextHighestBidder.setId(2L);

        Bid bid = new Bid();
        bid.setBuyer(nextHighestBidder);
        bid.setBidAmount(BigDecimal.valueOf(150.0));

        // Мокаем методы
        when(lotRepository.findByCurrentBuyerId_Id(anyLong())).thenReturn(List.of(lotWithBuyer));
        when(bidRepository.findAllByLotIdOrderByBidIdDesc(anyLong())).thenReturn(List.of(bid));
        when(userRepository.findById(nextHighestBidder.getId())).thenReturn(Optional.of(nextHighestBidder));
        when(lotRepository.save(any(Lot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        lotServiceImpl.handleUserDeletion(1L);

        // Assert
        assertNotNull(lotWithBuyer.getCurrentBuyerId());
        assertEquals(2L, lotWithBuyer.getCurrentBuyerId().getId());
        assertEquals(BigDecimal.valueOf(150.0), lotWithBuyer.getCurrentPrice());
        verify(lotRepository, times(1)).save(lotWithBuyer);
    }

}

