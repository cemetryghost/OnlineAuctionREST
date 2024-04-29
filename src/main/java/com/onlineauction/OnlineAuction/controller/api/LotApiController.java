package com.onlineauction.OnlineAuction.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.service.LotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private final LotService lotService;
    private final LotRepository lotRepository;

    @Autowired
    public LotApiController(LotService lotService, LotRepository lotRepository) {
        this.lotService = lotService;
        this.lotRepository = lotRepository;
    }

    @GetMapping
    public ResponseEntity<List<LotDTO>> getAllLots(@RequestParam(required = false) StatusLot statusLot) {
        List<LotDTO> lots = lotService.getLotsByStatus(statusLot);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/active/search")
    public ResponseEntity<List<LotDTO>> searchActiveLots(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {

        List<LotDTO> lots = lotService.searchActiveLots(categoryId, keyword);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotDTO> getLotById(@PathVariable Long id) {
        LotDTO lotDTO = lotService.getLotById(id);
        return lotDTO != null ? ResponseEntity.ok(lotDTO) : ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<LotDTO> createLot(
            @RequestParam("lot") String lotStr,
            @RequestParam(value = "static/image", required = false) MultipartFile image) {

        try {
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            LotDTO lotDTO = objectMapper.readValue(lotStr, LotDTO.class);
            LotDTO createdLot = lotService.createLot(lotDTO, image);
            return new ResponseEntity<>(createdLot, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<LotDTO> updateLot(
            @PathVariable Long id,
            @RequestParam("lot") String lotStr,
            @RequestParam(value = "static/image", required = false) MultipartFile image) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            LotDTO lotDTO = objectMapper.readValue(lotStr, LotDTO.class);
            LotDTO updatedLot = lotService.updateLot(id, lotDTO, image);
            return ResponseEntity.ok(updatedLot);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        lotService.deleteLot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lotId}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long lotId, @RequestParam("static/image") MultipartFile image) {
        try {
            lotService.uploadImage(lotId, image);
            return ResponseEntity.ok("Изображение успешно загружено");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка загрузки изображения");
        }
    }

    @GetMapping("/{lotid}/image")
    public ResponseEntity<byte[]> getLotImage(@PathVariable Long lotid) {
        Lot lot = lotRepository.findById(lotid)
                .orElseThrow(() -> new IllegalArgumentException("Лот не найден"));

        if (lot.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(lot.getImage(), headers, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<LotDTO>> getLotsByCategoryId(@PathVariable Long categoryId) {
        List<LotDTO> lotDTO = lotService.getLotsByCategoryId(categoryId);
        return ResponseEntity.ok(lotDTO);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LotDTO> updateLotStatus(@PathVariable Long id, @RequestParam StatusLot newStatus) {
        LotDTO updatedLot = lotService.updateLotStatus(id, newStatus);
        return updatedLot != null ? ResponseEntity.ok(updatedLot) : ResponseEntity.notFound().build();
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
