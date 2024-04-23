package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.BidDTO;
import com.onlineauction.OnlineAuction.entity.Bid;
import com.onlineauction.OnlineAuction.entity.Lot;
import com.onlineauction.OnlineAuction.entity.UserAccounts;
import com.onlineauction.OnlineAuction.mapper.BidMapper;
import com.onlineauction.OnlineAuction.repository.BidRepository;
import com.onlineauction.OnlineAuction.repository.LotRepository;
import com.onlineauction.OnlineAuction.repository.UserRepository;
import com.onlineauction.OnlineAuction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;

    @Autowired
    public BidServiceImpl(BidRepository bidRepository, LotRepository lotRepository,
                          UserRepository userRepository, BidMapper bidMapper) {
        this.bidRepository = bidRepository;
        this.lotRepository = lotRepository;
        this.userRepository = userRepository;
        this.bidMapper = bidMapper;
    }

    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Override
    public List<BidDTO> getAllBids() {
        List<Bid> bids = bidRepository.findAll();
        return bids.stream()
                .map(bidMapper::bidToBidDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BidDTO> getBidsByLotId(Long lotId) {
        List<Bid> bids = bidRepository.findByLotId(lotId);
        return bids.stream()
                .map(bidMapper::bidToBidDTO)
                .collect(Collectors.toList());
    }


    @Override
    public BidDTO getBidById(Long id) {
        return bidRepository.findById(id)
                .map(bidMapper::bidToBidDTO)
                .orElse(null);
    }

    @Override
    public BidDTO updateBid(Long id, BigDecimal newBidAmount) {
        Bid existingBid = bidRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ставка с таким id не найдена: " + id));

        Lot lot = existingBid.getLot();
        BigDecimal currentPrice = lot.getCurrentPrice();
        BigDecimal stepPrice = lot.getStepPrice();

        BigDecimal newBidTotalAmount = currentPrice.add(stepPrice);

        if (newBidAmount.compareTo(newBidTotalAmount) < 0) {
            throw new IllegalArgumentException("Новая сумма ставки должна быть больше или равна текущей сумме ставки плюс цена шаг");
        }

        existingBid.setBidAmount(newBidTotalAmount);
        existingBid = bidRepository.save(existingBid);

        return bidMapper.bidToBidDTO(existingBid);
    }

    @Override
    public void deleteBid(Long id) {
        bidRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ставка с таким id не найдена: " + id));

        bidRepository.deleteById(id);
    }


    @Transactional
    @Override
    public BidDTO placeBid(BidDTO bidDTO) {
        Optional<Lot> lotOptional = lotRepository.findById(bidDTO.getLotId());
        if (lotOptional.isEmpty()) {
            throw new IllegalArgumentException("Лот не найден");
        }
        Lot lot = lotOptional.get();

        BigDecimal minimumBidAmount = lot.getCurrentPrice() != null ? lot.getCurrentPrice().add(lot.getStepPrice()) : lot.getStartPrice().add(lot.getStepPrice());
        if (bidDTO.getBidAmount().compareTo(minimumBidAmount) < 0) {
            throw new IllegalArgumentException("Новая сумма ставки должна быть больше или равна текущей сумме ставки плюс цена шага");
        }

        String currentUserLogin = customUserDetailsServiceImpl.getCurrentUserLogin();
        UserAccounts buyer = userRepository.findByLogin(currentUserLogin);
        if (buyer == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        // Создаем объект ставки и устанавливаем соответствующие значения
        Bid bid = bidMapper.BidDTOtoBid(bidDTO);
        bid.setLot(lot);
        bid.setBuyer(buyer);

        // Сохраняем ставку
        bid = bidRepository.save(bid);

        // Обновляем текущую цену и идентификатор текущего покупателя для лота
        lot.setCurrentPrice(bidDTO.getBidAmount());
        lot.setCurrentBuyerId(buyer);
        lotRepository.save(lot);

        // Возвращаем информацию о размещенной ставке
        return bidMapper.bidToBidDTO(bid);
    }


}
