package com.example.inventorysystem.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService StockService;

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
    public void 재고감소() {
        StockService.decrease(1L, 1L);

        //100 - 1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99, stock.getQuantity());
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 멀티 스레드 환경 구현
        CountDownLatch latch = new CountDownLatch(threadCount); //총 100개 요청

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try { // 예외가 발생할 수 있음
                    StockService.decrease(1L, 1L);
                } finally { // 예외 발생해도 무조건 실행
                    latch.countDown(); // 이 스레드 작업 끝 -> 카운트 감소
                }
            });
        }

        latch.await(); // 100개 모두 끝날 때까지 기다림

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 100 - ( 1 * 100 ) = 0
        assertEquals(0,stock.getQuantity());
    }
}