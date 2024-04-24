package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.service.BidService;
import com.onlineauction.OnlineAuction.service.LotService;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bids")
@Validated
public class BidApiController {

    private final BidService bidService;
    private final LotService lotService;

    @Autowired
    public BidApiController(BidService bidService, LotService lotService) {
        this.bidService = bidService;
        this.lotService = lotService;
    }

    @Autowired
    public CustomUserDetailsServiceImpl customUserDetailsService;

    @GetMapping
    public ResponseEntity<List<BidDTO>> getAllBids() {
        List<BidDTO> bidDTOList = bidService.getAllBids();
        return ResponseEntity.ok(bidDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidDTO> getBidById(@PathVariable Long id) {
        BidDTO bidDTO = bidService.getBidById(id);
        return bidDTO != null ? ResponseEntity.ok(bidDTO) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/increase")
    public ResponseEntity<BidDTO> increaseBid(@PathVariable Long id, @RequestParam BigDecimal newBidAmount) {
        BidDTO updatedBid = bidService.updateBid(id, newBidAmount);
        return updatedBid != null ? ResponseEntity.ok(updatedBid) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<BidDTO> placeBid(@RequestBody BidDTO bidDTO) {
        BidDTO createdBid = bidService.placeBid(bidDTO);
        return ResponseEntity.ok(createdBid);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBid(@PathVariable Long id) {
        bidService.deleteBid(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<BidDTO>> getBidsByLotId(@PathVariable Long lotId) {
        List<BidDTO> bidDTOList = bidService.getBidsByLotId(lotId);
        return ResponseEntity.ok(bidDTOList);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BidDTO>> getMyBidsWithLotDetails() {
        try {
            List<BidDTO> bidsWithLotDetails = bidService.getMyBidsWithLotDetails();
            return ResponseEntity.ok(bidsWithLotDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

