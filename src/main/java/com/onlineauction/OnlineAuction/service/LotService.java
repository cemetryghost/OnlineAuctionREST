package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LotService {
    List<LotDTO> getAllLots();
    List<LotDTO> getLotsByStatus(StatusLot statusLot);
    LotDTO getLotById(Long id);
    LotDTO createLot(LotDTO lotDTO, MultipartFile file) throws IOException;
    LotDTO updateLot(Long id, LotDTO lotDTO, MultipartFile image) throws IOException;
    void deleteLot(Long id);
    void uploadImage(Long lotId, MultipartFile file) throws IOException;
    byte[] getLotImage(Long lotId);
    List<LotDTO> getLotsByCategoryId(Long categoryId);
    LotDTO updateLotStatus(Long id, StatusLot newStatus);
    List<LotDTO> getLotsBySellerId(Long sellerId);
    List<LotDTO> getLotsByCurrentSeller();
    List<LotDTO> getCompletedLotsBySellerId();
    List<LotDTO> getActiveLots();
    List<LotDTO> getActiveLotsByCategoryId(Long categoryId);
    void checkAndUpdateLotStatusDateClosing();
    void updateLotStatusesDateClosing();
    List<LotDTO> searchActiveLots(Long categoryId, String keyword);
    void handleUserDeletion(Long userId);
}
