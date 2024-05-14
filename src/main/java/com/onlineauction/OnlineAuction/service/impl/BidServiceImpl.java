package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.dto.LotDTO;
import com.onlineauction.OnlineAuction.entity.Bid;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.enums.StatusLot;
import com.onlineauction.OnlineAuction.exception.BidException;
import com.onlineauction.OnlineAuction.mapper.BidMapper;
import com.onlineauction.OnlineAuction.repository.BidRepository;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.BidService;
import com.onlineauction.OnlineAuction.service.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final LotService lotService;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Autowired
    public BidServiceImpl(BidRepository bidRepository, LotRepository lotRepository,
                          UserRepository userRepository, BidMapper bidMapper, LotService lotService, CustomUserDetailsServiceImpl customUserDetailsServiceImpl) {
        this.bidRepository = bidRepository;
        this.lotRepository = lotRepository;
        this.userRepository = userRepository;
        this.bidMapper = bidMapper;
        this.lotService = lotService;
        this.customUserDetailsServiceImpl = customUserDetailsServiceImpl;
    }
    
    @Override
    public List<BidDTO> getAllBids() {
        return mapBidListToDTOList(bidRepository.findAll());
    }

    @Override
    public List<BidDTO> getBidsByLotId(Long lotId) {
        return mapBidListToDTOList(bidRepository.findByLotId(lotId));
    }
    
    @Override
    public BidDTO getBidById(Long id) {
        return bidRepository.findById(id)
                .map(bidMapper::bidToBidDTO)
                .orElse(null);
    }

    @Override
    public BidDTO updateBid(Long id, BigDecimal newBidAmount) {
        Bid existingBid = getBidByIdOrElseThrow(id);

        Lot lot = existingBid.getLot();
        checkLotStatus(lot);
        validateBidForUpdate(lot, newBidAmount);

        existingBid.setBidAmount(newBidAmount);
        existingBid = bidRepository.save(existingBid);
        lot.setCurrentPrice(newBidAmount);
        lotRepository.save(lot);

        return bidMapper.bidToBidDTO(existingBid);
    }

    @Override
    public void deleteBid(Long id) {
        Bid existingBid = getBidByIdOrElseThrow(id);
        bidRepository.delete(existingBid);
    }

    @Override
    public BidDTO placeBid(BidDTO bidDTO) {
        Lot lot = getLotByIdOrElseThrow(bidDTO.getLotId());
        checkLotStatus(lot);
        BigDecimal minimumBidAmount = calculateMinimumBidAmount(lot);

        validateBidForPlacement(bidDTO, minimumBidAmount);

        UserAccounts buyer = getCurrentUserOrThrow();
        Bid bid = bidMapper.BidDTOtoBid(bidDTO);
        bid.setLot(lot);
        bid.setBuyer(buyer);

        bid = bidRepository.save(bid);

        updateLotAfterBidPlacement(lot, bidDTO.getBidAmount(), buyer);

        return bidMapper.bidToBidDTO(bid);
    }

    @Override
    public List<BidDTO> getMyBidsWithLotDetails() {
        UserAccounts buyer = getCurrentUserOrThrow();
        List<Bid> myBids = bidRepository.findByBuyerId(buyer.getId());
        return mapBidListToDTOListWithLotDetails(myBids);
    }

    private List<BidDTO> mapBidListToDTOList(List<Bid> bids) {
        return bids.stream()
                .map(bidMapper::bidToBidDTO)
                .collect(Collectors.toList());
    }

    private Bid getBidByIdOrElseThrow(Long id) {
        return bidRepository.findById(id)
                .orElseThrow(() -> new BidException("Ставка с таким id не найдена: " + id));
    }

    private Lot getLotByIdOrElseThrow(Long lotId) {
        return lotRepository.findById(lotId)
                .orElseThrow(() -> new BidException("Лот с таким id не найден: " + lotId));
    }

    private BigDecimal calculateMinimumBidAmount(Lot lot) {
        return lot.getCurrentPrice() != null ? lot.getCurrentPrice().add(lot.getStepPrice()) : lot.getStartPrice().add(lot.getStepPrice());
    }

    private void validateBidForUpdate(Lot lot, BigDecimal newBidAmount) {
        BigDecimal newBidTotalAmount = lot.getCurrentPrice().add(lot.getStepPrice());
        if (newBidAmount.compareTo(newBidTotalAmount) < 0) {
            throw new BidException("Новая сумма ставки должна быть больше или равна текущей сумме ставки плюс цена шага");
        }
    }

    private void checkLotStatus(Lot lot) {
        if (!lot.getStatusLots().equals(StatusLot.ACTIVE_LOT)) {
            throw new BidException("Ставку можно повышать или размещать только на активные лоты");
        }
    }

    private void validateBidForPlacement(BidDTO bidDTO, BigDecimal minimumBidAmount) {
        if (bidDTO.getBidAmount().compareTo(minimumBidAmount) < 0) {
            throw new BidException("Новая сумма ставки должна быть больше или равна текущей сумме ставки плюс цена шага");
        }
    }

    private UserAccounts getCurrentUserOrThrow() {
        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        return userRepository.findByLogin(currentUserLogin);
    }

    private void updateLotAfterBidPlacement(Lot lot, BigDecimal newBidAmount, UserAccounts buyer) {
        lot.setCurrentPrice(newBidAmount);
        lot.setCurrentBuyerId(buyer);
        lotRepository.save(lot);
    }

    private List<BidDTO> mapBidListToDTOListWithLotDetails(List<Bid> bids) {
        return bids.stream().map(bid -> {
            BidDTO bidDTO = bidMapper.bidToBidDTO(bid);
            LotDTO lotDTO = lotService.getLotById(bid.getLot().getId());
            bidDTO.setLotDTO(lotDTO);
            return bidDTO;
        }).collect(Collectors.toList());
    }
}

