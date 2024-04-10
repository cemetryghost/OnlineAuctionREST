package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.context.MappingContext;
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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MappingContext mappingContext;

    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Autowired
    public LotServiceImpl(LotRepository lotRepository, LotMapper lotMapper, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.lotRepository = lotRepository;
        this.lotMapper = lotMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<LotDTO> getAllLots() {
        return lotRepository.findAll().stream()
                .map(lotMapper::lotToLotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LotDTO getLotById(Long id) {
        return lotRepository.findById(id)
                .map(lotMapper::lotToLotDTO)
                .orElse(null);
    }

    @Override
    public LotDTO createLot(LotDTO lotDTO) {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts user = userRepository.findByLogin(currentUserLogin);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (!Role.SELLER.equals(user.getRole())) {
            if (lotDTO.getSellerId() == null) {
                throw new IllegalArgumentException("Seller id must be provided for non-seller users");
            }
            userRepository.findById(lotDTO.getSellerId())
                    .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + lotDTO.getSellerId()));
        } else {
            lotDTO.setSellerId(user.getId());
        }
        Lot lot = lotMapper.lotDTOToLot(lotDTO, mappingContext);
        lot.setPublicationDate(LocalDate.now());
        lot.setCategoryId(categoryRepository.findById(lotDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + lotDTO.getCategoryId())));
        if (lotDTO.getCurrentBuyerId() != null) {
            UserAccounts currentBuyer = userRepository.findById(lotDTO.getCurrentBuyerId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + lotDTO.getCurrentBuyerId()));
            lot.setCurrentBuyerId(currentBuyer);
        } else {
            lot.setCurrentBuyerId(null);
        }
        lot = lotRepository.save(lot);
        return lotMapper.lotToLotDTO(lot);
    }

    @Override
    public LotDTO updateLot(Long id, LotDTO lotDTO) {
        Lot existingLot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found with id: " + id));

        if (existingLot.getCurrentBuyerId() != null) {
            throw new IllegalStateException("Cannot update lot with a current buyer");
        }

        if (lotDTO.getNameLots() != null) existingLot.setNameLots(lotDTO.getNameLots());
        if (lotDTO.getDescriptionLots() != null) existingLot.setDescriptionLots(lotDTO.getDescriptionLots());
        if (lotDTO.getStartPrice() != null) existingLot.setStartPrice(lotDTO.getStartPrice());
        if (lotDTO.getStepPrice() != null) existingLot.setStepPrice(lotDTO.getStepPrice());
        if (lotDTO.getClosingDate() != null) existingLot.setClosingDate(lotDTO.getClosingDate());
        if (lotDTO.getConditionLots() != null) existingLot.setConditionLots(lotDTO.getConditionLots());

        if (lotDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(lotDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + lotDTO.getCategoryId()));
            existingLot.setCategoryId(category);
        }

        if (lotDTO.getCurrentBuyerId() != null) {
            UserAccounts currentBuyer = userRepository.findById(lotDTO.getCurrentBuyerId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + lotDTO.getCurrentBuyerId()));
            existingLot.setCurrentBuyerId(currentBuyer);
        }

        existingLot = lotRepository.save(existingLot);
        return lotMapper.lotToLotDTO(existingLot);
    }

    @Override
    public void deleteLot(Long id) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found with id: " + id));

        if (lot.getCurrentBuyerId() != null) {
            throw new IllegalStateException("Cannot delete lot with a current buyer");
        }
        lotRepository.deleteById(id);
    }

    public void uploadImage(Long lotId, MultipartFile file) throws IOException {
        Lot lot = lotRepository.findById(lotId).orElseThrow(() -> new RuntimeException("Lot not found"));
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
                .orElseThrow(() -> new IllegalArgumentException("Lot not found with id: " + id));

        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        existingLot.setStatusLots(newStatus);
        existingLot = lotRepository.save(existingLot);
        return lotMapper.lotToLotDTO(existingLot);
    }
}
