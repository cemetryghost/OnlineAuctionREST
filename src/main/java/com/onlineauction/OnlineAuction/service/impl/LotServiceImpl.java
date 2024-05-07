package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.mapper.MappingContext;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Category;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.Role;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.mapper.LotMapper;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.LotService;
import com.onlineauction.OnlineAuction.specification.LotSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LotServiceImpl implements LotService {

    private final LotRepository lotRepository;
    private final LotMapper lotMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MappingContext mappingContext;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Autowired
    public LotServiceImpl(LotRepository lotRepository, LotMapper lotMapper, CategoryRepository categoryRepository, UserRepository userRepository, MappingContext mappingContext, CustomUserDetailsServiceImpl customUserDetailsServiceImpl) {
        this.lotRepository = lotRepository;
        this.lotMapper = lotMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.mappingContext = mappingContext;
        this.customUserDetailsServiceImpl = customUserDetailsServiceImpl;
    }

//  Метод, при запуске проверяет дату закрытия лота
    @Override
    public void checkAndUpdateLotStatusDateClosing() {
        List<Lot> lots = lotRepository.findAll();
        LocalDate currentDate = LocalDate.now();
        for (Lot lot : lots) {
            if (lot.getClosingDate().isBefore(currentDate) && lot.getStatusLots() != StatusLot.COMPLETED_LOT) {
                lot.setStatusLots(StatusLot.COMPLETED_LOT);
                lotRepository.save(lot);
            }
        }
    }

//  Метод планироващика, для проверки даты закрытия при запущенном сервере
    @Override
    public void updateLotStatusesDateClosing() {
        List<Lot> lots = lotRepository.findAllByClosingDateBeforeAndStatusLotsNot(LocalDate.now().plusDays(1), StatusLot.COMPLETED_LOT);
        for (Lot lot : lots) {
            lot.setStatusLots(StatusLot.COMPLETED_LOT);
            lotRepository.save(lot);
        }
    }

    @Override
    public List<LotDTO> getAllLots() {
        return lotRepository.findAll().stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Transactional
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

    @Transactional
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
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts user = userRepository.findByLogin(currentUserLogin);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        lotDTO.setStatusLots(StatusLot.AWAITING_CONFIRMATION_LOT);

        if (!Role.SELLER.equals(user.getRole())) {
            if (lotDTO.getSellerId() == null) {
                throw new IllegalArgumentException("Идентификатор продавца должен быть предоставлен для пользователей, не являющихся продавцами");
            }
            userRepository.findById(lotDTO.getSellerId())
                    .orElseThrow(() -> new IllegalArgumentException("Продавец не найден по идентификатору: " + lotDTO.getSellerId()));
        } else {
            lotDTO.setSellerId(user.getId());
        }
        Lot lot = lotMapper.lotDTOToLot(lotDTO, mappingContext);
        lot.setPublicationDate(LocalDate.now());
        lot.setCategoryId(categoryRepository.findById(lotDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Не найдена категория с таким id " + lotDTO.getCategoryId())));
        if (lotDTO.getCurrentBuyerId() != null) {
            UserAccounts currentBuyer = userRepository.findById(lotDTO.getCurrentBuyerId())
                    .orElseThrow(() -> new IllegalArgumentException("Не найден пользователь с таким id " + lotDTO.getCurrentBuyerId()));
            lot.setCurrentBuyerId(currentBuyer);
        } else {
            lot.setCurrentBuyerId(null);
        }

        if (image != null && !image.isEmpty()) {
            byte[] imageBytes = image.getBytes();
            lot.setImage(imageBytes);
        }
        Lot savedLot = lotRepository.save(lot);
        return lotMapper.lotToLotDTO(savedLot);
    }

    @Override
    public LotDTO updateLot(Long id, LotDTO lotDTO, MultipartFile image) throws IOException {
        Lot existingLot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Лот с такми id не найден: " + id));

        if (existingLot.getCurrentBuyerId() != null) {
            throw new IllegalStateException("Обновления лота запрещено, так как есть текущий покупатель");
        }

        if (lotDTO.getNameLots() != null) existingLot.setNameLots(lotDTO.getNameLots());
        if (lotDTO.getDescriptionLots() != null) existingLot.setDescriptionLots(lotDTO.getDescriptionLots());
        if (lotDTO.getStartPrice() != null) existingLot.setStartPrice(lotDTO.getStartPrice());
        if (lotDTO.getStepPrice() != null) existingLot.setStepPrice(lotDTO.getStepPrice());
        if (lotDTO.getClosingDate() != null) existingLot.setClosingDate(lotDTO.getClosingDate());
        if (lotDTO.getConditionLots() != null) existingLot.setConditionLots(lotDTO.getConditionLots());

        if (lotDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(lotDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Категория с таким id не найдена" + lotDTO.getCategoryId()));
            existingLot.setCategoryId(category);
        }

        if (image != null && !image.isEmpty()) {
            byte[] imageBytes = image.getBytes();
            existingLot.setImage(imageBytes);
        }

        lotRepository.save(existingLot);
        return lotMapper.lotToLotDTO(existingLot);
    }

    @Override
    public void deleteLot(Long id) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Лот с такми id не найден: " + id));

        if (lot.getCurrentBuyerId() != null) {
            throw new IllegalStateException("Удаление лота запрещено, так как есть текущий покупатель");
        }
        lotRepository.deleteById(id);
    }

    public void uploadImage(Long lotId, MultipartFile file) throws IOException {
        Lot lot = lotRepository.findById(lotId).orElseThrow(() -> new RuntimeException("Лот не найден"));
        byte[] imageBytes = file.getBytes();
        lot.setImage(imageBytes);
        lotRepository.save(lot);
    }

    @Transactional
    @Override
    public List<LotDTO> getLotsByCategoryId(Long categoryId) {
        return lotRepository.findByCategoryIdId(categoryId).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LotDTO updateLotStatus(Long id, StatusLot newStatus) {
        Lot existingLot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Лот с таким id не найден: " + id));

        if (newStatus == null) {
            throw new IllegalArgumentException("Новый статус не может быть пустым");
        }
        existingLot.setStatusLots(newStatus);
        existingLot = lotRepository.save(existingLot);
        return lotMapper.lotToLotDTO(existingLot);
    }

    @Transactional
    @Override
    public List<LotDTO> getLotsBySellerId(Long sellerId) {
        return lotRepository.findBySellerIdId(sellerId).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<LotDTO> getLotsByCurrentSeller() {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts seller = userRepository.findByLogin(currentUserLogin);
        if (seller == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        List<Lot> lots = lotRepository.findBySellerId_Id(seller.getId());
        return lots.stream().map(lotMapper::lotToLotDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<LotDTO> getCompletedLotsBySellerId() {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts seller = userRepository.findByLogin(currentUserLogin);
        if (seller == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        List<Lot> completedLots = lotRepository.findBySellerId_IdAndStatusLots(seller.getId(), StatusLot.COMPLETED_LOT);
        return completedLots.stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<LotDTO> getActiveLots() {
        return lotRepository.findByStatusLots(StatusLot.ACTIVE_LOT).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<LotDTO> getActiveLotsByCategoryId(Long categoryId) {
        return lotRepository.findByCategoryIdIdAndStatusLots(categoryId, StatusLot.ACTIVE_LOT).stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

}
