package com.example.inventorysystem.service;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    public OptimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
    @Transactional
    public void decrease(Long id, Long quantity){
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        stock.decrease(quantity);

        stockRepository.save(stock);
    }
}
