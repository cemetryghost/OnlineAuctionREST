package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LotService {
    List<LotDTO> getAllLots();
    LotDTO getLotById(Long id);
    LotDTO createLot(LotDTO lotDTO, MultipartFile file) throws IOException;
    LotDTO updateLot(Long id, LotDTO lotDTO, MultipartFile image) throws IOException;
    void deleteLot(Long id);
    void uploadImage(Long lotId, MultipartFile file) throws IOException;
    List<LotDTO> getLotsByCategoryId(Long categoryId);
    LotDTO updateLotStatus(Long id, StatusLot newStatus);
    List<LotDTO> getLotsBySellerId(Long sellerId);
    List<LotDTO> getLotsByCurrentSeller();
}
