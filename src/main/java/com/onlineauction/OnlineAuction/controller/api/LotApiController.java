package com.onlineauction.OnlineAuction.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.service.LotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/lots")
@Validated
public class LotApiController {

    private final LotService lotService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LotApiController(LotService lotService, ObjectMapper objectMapper) {
        this.lotService = lotService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<LotDTO>> getAllLots(@RequestParam(required = false) StatusLot statusLot) {
        List<LotDTO> lots = lotService.getLotsByStatus(statusLot);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/active/search")
    public ResponseEntity<List<LotDTO>> searchActiveLots(@RequestParam(required = false) String keyword, @RequestParam(required = false) Long categoryId) {
        List<LotDTO> lots = lotService.searchActiveLots(categoryId, keyword);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotDTO> getLotById(@PathVariable Long id) {
        LotDTO lotDTO = lotService.getLotById(id);
        return ResponseEntity.ok(lotDTO);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<LotDTO> createLot(@RequestParam("lot") @Valid String lotStr, @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        LotDTO lotDTO = objectMapper.readValue(lotStr, LotDTO.class);
        LotDTO createdLot = lotService.createLot(lotDTO, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLot);
    }

    @PutMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<LotDTO> updateLot(@PathVariable Long id, @RequestParam("lot") @Valid String lotStr, @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        LotDTO lotDTO = objectMapper.readValue(lotStr, LotDTO.class);
        LotDTO updatedLot = lotService.updateLot(id, lotDTO, image);
        return ResponseEntity.ok(updatedLot);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        lotService.deleteLot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lotId}/image")
    public ResponseEntity<String> uploadImage(@PathVariable Long lotId, @RequestParam("image") MultipartFile image) throws IOException {
        lotService.uploadImage(lotId, image);
        return ResponseEntity.ok("Изображение успешно загружено");
    }

    @GetMapping("/{lotid}/image")
    public ResponseEntity<byte[]> getLotImage(@PathVariable Long lotid) {
        byte[] image = lotService.getLotImage(lotid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<LotDTO>> getLotsByCategoryId(@PathVariable Long categoryId) {
        List<LotDTO> lotDTO = lotService.getLotsByCategoryId(categoryId);
        return ResponseEntity.ok(lotDTO);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LotDTO> updateLotStatus(@PathVariable Long id, @RequestParam StatusLot newStatus) {
        LotDTO updatedLot = lotService.updateLotStatus(id, newStatus);
        return ResponseEntity.ok(updatedLot);
    }

    @GetMapping("/my")
    public ResponseEntity<List<LotDTO>> getMyLots() {
        return ResponseEntity.ok(lotService.getLotsByCurrentSeller());
    }

    @GetMapping("/my/completed")
    public ResponseEntity<List<LotDTO>> getMyCompletedLots() {
        List<LotDTO> completedLots = lotService.getCompletedLotsBySellerId();
        return ResponseEntity.ok(completedLots);
    }

    @GetMapping("/active")
    public ResponseEntity<List<LotDTO>> getActiveLots() {
        List<LotDTO> activeLots = lotService.getActiveLots();
        return ResponseEntity.ok(activeLots);
    }

    @GetMapping("/active/category/{categoryId}")
    public ResponseEntity<List<LotDTO>> getActiveLotsByCategoryId(@PathVariable Long categoryId) {
        List<LotDTO> activeLots = lotService.getActiveLotsByCategoryId(categoryId);
        return ResponseEntity.ok(activeLots);
    }
}
