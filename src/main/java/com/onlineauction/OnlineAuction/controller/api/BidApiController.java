package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bids")
public class BidApiController {

    private final BidService bidService;

    @Autowired
    public BidApiController(BidService bidService) {
        this.bidService = bidService;
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<BidDTO> placeBidUp(@PathVariable Long id, @RequestParam BigDecimal newBidAmount) {
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
}

