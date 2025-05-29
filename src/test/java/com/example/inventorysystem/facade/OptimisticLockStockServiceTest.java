package com.example.inventorysystem.facade;

import static org.junit.jupiter.api.Assertions.*;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;
import com.example.inventorysystem.service.OptimisticLockStockService;
import com.example.inventorysystem.service.PessimisticLockStockService;
import com.example.inventorysystem.service.StockService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class OptimisticLockStockServiceTest {

    @Autowired
    private com.example.inventorysystem.service.StockService StockService;

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void 동시에_100개의_요청_낙관락() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 100개 모두 끝날 때까지 기다림

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 100 - ( 1 * 100 ) = 0
        assertEquals(0, stock.getQuantity());
    }
}