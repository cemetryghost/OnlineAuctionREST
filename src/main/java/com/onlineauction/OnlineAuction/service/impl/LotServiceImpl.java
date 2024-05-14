package com.onlineauction.OnlineAuction.service.impl;

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
import com.onlineauction.OnlineAuction.service.LotService;
import com.onlineauction.OnlineAuction.specification.LotSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LotServiceImpl implements LotService {

    private final LotRepository lotRepository;
    private final LotMapper lotMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MappingContext mappingContext;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;
    private final BidRepository bidRepository;

    @Autowired
    public LotServiceImpl(LotRepository lotRepository, LotMapper lotMapper, CategoryRepository categoryRepository, UserRepository userRepository, MappingContext mappingContext, CustomUserDetailsServiceImpl customUserDetailsServiceImpl, BidRepository bidRepository) {
        this.lotRepository = lotRepository;
        this.lotMapper = lotMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.mappingContext = mappingContext;
        this.customUserDetailsServiceImpl = customUserDetailsServiceImpl;
        this.bidRepository = bidRepository;
    }

    @Override
    public void checkAndUpdateLotStatusDateClosing() {
    LocalDate currentDate = LocalDate.now();
    lotRepository.findAll().stream()
            .filter(lot -> lot.getClosingDate().isBefore(currentDate) && lot.getStatusLots() != StatusLot.COMPLETED_LOT)
            .forEach(lot -> {
                lot.setStatusLots(StatusLot.COMPLETED_LOT);
                lotRepository.save(lot);
            });
    }

    @Override
    public void updateLotStatusesDateClosing() {
        lotRepository.findAllByClosingDateBeforeAndStatusLotsNot(LocalDate.now().plusDays(1), StatusLot.COMPLETED_LOT)
            .forEach(lot -> {
                lot.setStatusLots(StatusLot.COMPLETED_LOT);
                lotRepository.save(lot);
            });
    }

    @Override
    public List<LotDTO> getAllLots() {
        return lotRepository.findAll().stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LotDTO> searchActiveLots(Long categoryId, String keyword) {
        Specification<Lot> spec = Specification.where(LotSpecification.isActive());
        if (categoryId != null) {
            spec = spec.and(LotSpecification.hasCategory(categoryId));
        }
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(LotSpecification.hasKeyword(keyword));
        }
        List<Lot> lots = lotRepository.findAll(spec);
        return lots.stream().map(lotMapper::lotToLotDTO).collect(Collectors.toList());
    }

    @Override
    public List<LotDTO> getLotsByStatus(StatusLot statusLot) {
        if (statusLot != null) {
            return lotRepository.findByStatusLots(statusLot).stream()
                    .map(lotMapper::lotToLotDTO)
                    .collect(Collectors.toList());
        } else {
            return getAllLots();
        }
    }

    @Override
    public LotDTO getLotById(Long id) {
        return lotRepository.findById(id)
                .map(lotMapper::lotToLotDTO)
                .orElse(null);
    }

    @Override
    public LotDTO createLot(LotDTO lotDTO, MultipartFile image) throws IOException {
        UserAccounts user = getCurrentUser();
        validateSeller(lotDTO, user);
        Lot lot = prepareNewLot(lotDTO);
        attachImageToLot(lot, image);
        lotRepository.save(lot);
        return lotMapper.lotToLotDTO(lot);
    }

    @Override
    public LotDTO updateLot(Long id, LotDTO lotDTO, MultipartFile image) throws IOException {
        Lot existingLot = findByIdOrThrow(lotRepository, id, "Лот с таким id не найден: " + id);
        checkIfUpdateIsAllowed(existingLot);
        updateLotDetails(existingLot, lotDTO);
        attachImageToLot(existingLot, image);
        lotRepository.save(existingLot);
        return lotMapper.lotToLotDTO(existingLot);
    }

    @Override
    public void deleteLot(Long id) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new LotException("Лот с такми id не найден: " + id));

        if (lot.getCurrentBuyerId() != null) {
            throw new LotException("Удаление лота запрещено, так как есть текущий покупатель");
        }
        lotRepository.deleteById(id);
    }

    @Override
    public List<LotDTO> getLotsByCategoryId(Long categoryId) {
        return lotRepository.findByCategoryIdId(categoryId).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LotDTO updateLotStatus(Long id, StatusLot newStatus) {
        Lot existingLot = lotRepository.findById(id)
                .orElseThrow(() -> new LotException("Лот с таким id не найден: " + id));
        if (newStatus == null) {
            throw new LotException("Новый статус не может быть пустым");
        }
        existingLot.setStatusLots(newStatus);
        existingLot = lotRepository.save(existingLot);
        return lotMapper.lotToLotDTO(existingLot);
    }

    @Override
    public List<LotDTO> getLotsBySellerId(Long sellerId) {
        return lotRepository.findBySellerIdId(sellerId).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LotDTO> getLotsByCurrentSeller() {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts seller = userRepository.findByLogin(currentUserLogin);
        if (seller == null) {
            throw new LotException("Пользователь не найден");
        }
        List<Lot> lots = lotRepository.findBySellerId_Id(seller.getId());
        return lots.stream().map(lotMapper::lotToLotDTO).collect(Collectors.toList());
    }

    
    @Override
    public List<LotDTO> getCompletedLotsBySellerId() {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts seller = userRepository.findByLogin(currentUserLogin);
        if (seller == null) {
            throw new LotException("Пользователь не найден");
        }
        List<Lot> completedLots = lotRepository.findBySellerId_IdAndStatusLots(seller.getId(), StatusLot.COMPLETED_LOT);
        return completedLots.stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LotDTO> getActiveLots() {
        return lotRepository.findByStatusLots(StatusLot.ACTIVE_LOT).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LotDTO> getActiveLotsByCategoryId(Long categoryId) {
        return lotRepository.findByCategoryIdIdAndStatusLots(categoryId, StatusLot.ACTIVE_LOT).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    private void checkIfUpdateIsAllowed(Lot existingLot) {
        if (existingLot.getCurrentBuyerId() != null) {
            throw new LotException("Обновление лота запрещено, так как есть текущий покупатель");
        }
    }

    private void validateLotDTOForCreation(LotDTO lotDTO) throws LotException {
        List<String> missingFields = new ArrayList<>();
        if (lotDTO.getNameLots() == null || lotDTO.getNameLots().trim().isEmpty()) {
            missingFields.add("nameLots");
        }
        if (lotDTO.getDescriptionLots() == null || lotDTO.getDescriptionLots().trim().isEmpty()) {
            missingFields.add("descriptionLots");
        }
        if (lotDTO.getStartPrice() == null) {
            missingFields.add("startPrice");
        }
        if (lotDTO.getStepPrice() == null){
            missingFields.add("stepPrice");
        }
        if (lotDTO.getClosingDate() == null) {
            missingFields.add("closingDate");
        }
        if (lotDTO.getConditionLots() == null || lotDTO.getConditionLots().trim().isEmpty()) {
            missingFields.add("conditionLots");
        }
        if (lotDTO.getCategoryId() == null) {
            missingFields.add("categoryId");
        }
        if (!missingFields.isEmpty()) {
            throw new LotException("Данные поля не могут быть пустыми: " + String.join(", ", missingFields));
        }
    }


    private void updateLotDetails(Lot existingLot, LotDTO lotDTO) {
        if (lotDTO.getNameLots() != null && !lotDTO.getNameLots().trim().isEmpty()) {
            existingLot.setNameLots(lotDTO.getNameLots());
        }
        if (lotDTO.getDescriptionLots() != null && !lotDTO.getDescriptionLots().trim().isEmpty()) {
            existingLot.setDescriptionLots(lotDTO.getDescriptionLots());
        }
        if (lotDTO.getStartPrice() != null) {
            existingLot.setStartPrice(lotDTO.getStartPrice());
        }
        if (lotDTO.getStepPrice() != null) {
            existingLot.setStepPrice(lotDTO.getStepPrice());
        }
        if (lotDTO.getClosingDate() != null) {
            existingLot.setClosingDate(lotDTO.getClosingDate());
        }
        if (lotDTO.getConditionLots() != null && !lotDTO.getConditionLots().trim().isEmpty()) {
            existingLot.setConditionLots(lotDTO.getConditionLots());
        }
        if (lotDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(lotDTO.getCategoryId())
                    .orElseThrow(() -> new LotException("Категория с таким id не найдена: " + lotDTO.getCategoryId()));
            existingLot.setCategoryId(category);
        }
    }

    private void attachImageToLot(Lot lot, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            lot.setImage(image.getBytes());
        }
    }

    private UserAccounts getCurrentUser() {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts user = userRepository.findByLogin(currentUserLogin);
        if (user == null) {
            throw new LotException("Пользователь не найден");
        }
        return user;
    }

    private void validateSeller(LotDTO lotDTO, UserAccounts user) {
        if (!Role.SELLER.equals(user.getRole()) && lotDTO.getSellerId() == null) {
            throw new LotException("Идентификатор продавца должен быть предоставлен для пользователей, не являющихся продавцами");
        }
        if (!Role.SELLER.equals(user.getRole()) && !user.getId().equals(lotDTO.getSellerId())) {
            throw new LotException("Несоответствие идентификатора продавца");
        }
        lotDTO.setSellerId(user.getId()); // Гарантируем, что ID продавца всегда устанавливается правильно
    }

    private Lot prepareNewLot(LotDTO lotDTO) {
        validateLotDTOForCreation(lotDTO);
        Category category = findByIdOrThrow(categoryRepository, lotDTO.getCategoryId(), "Категория не найдена");
        Lot lot = lotMapper.lotDTOToLot(lotDTO, mappingContext);
        lot.setPublicationDate(LocalDate.now());
        lot.setCategoryId(category);
        lot.setStatusLots(StatusLot.AWAITING_CONFIRMATION_LOT);
        lot.setCurrentBuyerId(null);
        return lot;
    }

    private <T> T findByIdOrThrow(@NonNull JpaRepository<T, Long> repository, Long id, String errorMessage) {
        return repository.findById(id).orElseThrow(() -> new LotException(errorMessage));
    }

    public void uploadImage(Long lotId, MultipartFile file) throws IOException {
        Lot lot = lotRepository.findById(lotId).orElseThrow(() -> new LotException("Лот не найден"));
        byte[] imageBytes = file.getBytes();
        lot.setImage(imageBytes);
        lotRepository.save(lot);
    }

    @Override
    public void handleUserDeletion(Long userId) {
        List<Lot> lotsWithUserAsCurrentBuyer = lotRepository.findByCurrentBuyerId_Id(userId);
        lotsWithUserAsCurrentBuyer.forEach(lot -> updateLotForNextHighestBid(lot, userId));
    }

    private void updateLotForNextHighestBid(Lot lot, Long userId) {
        List<Bid> validBids = getValidBids(lot.getId(), userId);
        if (!validBids.isEmpty()) {
            setNextBuyer(lot, validBids.get(0));
        } else {
            clearCurrentBuyer(lot);
        }
        lotRepository.save(lot);
    }

    private List<Bid> getValidBids(Long lotId, Long userId) {
        return bidRepository.findAllByLotIdOrderByBidIdDesc(lotId).stream()
                .filter(bid -> !bid.getBuyer().getId().equals(userId))
                .collect(Collectors.toList());
    }

    private void setNextBuyer(Lot lot, Bid nextHighestBid) {
        userRepository.findById(nextHighestBid.getBuyer().getId()).ifPresent(buyer -> {
            lot.setCurrentBuyerId(buyer);
            lot.setCurrentPrice(nextHighestBid.getBidAmount());
        });
    }

    private void clearCurrentBuyer(Lot lot) {
        lot.setCurrentBuyerId(null);
        lot.setCurrentPrice(null);
    }

//    TODO: Flatpickr для лота на клиенте
}
