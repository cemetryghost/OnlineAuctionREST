package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.service.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lots")
public class LotApiController {

    private final LotService lotService;
    private final LotRepository lotRepository;

    @Autowired
    public LotApiController(LotService lotService, LotRepository lotRepository) {
        this.lotService = lotService;
        this.lotRepository = lotRepository;
    }

    @GetMapping
    public ResponseEntity<List<LotDTO>> getAllLots() {
        return ResponseEntity.ok(lotService.getAllLots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotDTO> getLotById(@PathVariable Long id) {
        LotDTO lotDTO = lotService.getLotById(id);
        return lotDTO != null ? ResponseEntity.ok(lotDTO) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<LotDTO> createLot(@RequestBody LotDTO lotDTO) {
        return new ResponseEntity<>(lotService.createLot(lotDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LotDTO> updateLot(@PathVariable Long id, @RequestBody LotDTO lotDTO) {
        LotDTO updatedLot = lotService.updateLot(id, lotDTO);
        return updatedLot != null ? ResponseEntity.ok(updatedLot) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        lotService.deleteLot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lotId}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long lotId, @RequestParam("image") MultipartFile image) {
        try {
            lotService.uploadImage(lotId, image);
            return ResponseEntity.ok("Image uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload image");
        }
    }

    @GetMapping("/{lotid}/image")
    public ResponseEntity<byte[]> getLotImage(@PathVariable Long lotid) {
        Lot lot = lotRepository.findById(lotid)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));

        if (lot.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Или MediaType.APPLICATION_OCTET_STREAM
        return new ResponseEntity<>(lot.getImage(), headers, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    @Transactional
    public ResponseEntity<List<LotDTO>> getLotsByCategoryId(@PathVariable Long categoryId) {
        List<LotDTO> lotDTOS = lotService.getLotsByCategoryId(categoryId);
        return ResponseEntity.ok(lotDTOS);
    }

}
