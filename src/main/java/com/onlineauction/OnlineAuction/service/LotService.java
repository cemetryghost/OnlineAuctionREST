package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.LotDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LotService {
    List<LotDTO> getAllLots();
    LotDTO getLotById(Long id);
    LotDTO createLot(LotDTO lotDTO);
    LotDTO updateLot(Long id, LotDTO lotDTO);
    void deleteLot(Long id);
    void uploadImage(Long lotId, MultipartFile file) throws IOException;
    List<LotDTO> getLotsByCategoryId(Long categoryId);
}
