package com.chuan.controller;

import com.chuan.lock.redis.DistributedLockWithRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author hechuan
 */
@RestController
public class LockController {

    @Autowired
    private DistributedLockWithRedis redisLock;

    private int ticketCount = 20;
    private CyclicBarrier barrier = new CyclicBarrier(5);

    @GetMapping("/sale")
    public long sale() {
        ticketCount = 20;
        barrier = new CyclicBarrier(5);

        System.out.println("=============Total 20 tickets, sold by 5 windows");
        new TicketWindow().start();
        new TicketWindow().start();
        new TicketWindow().start();
        new TicketWindow().start();
        new TicketWindow().start();

        return ticketCount;
    }

    public class TicketWindow extends Thread {
        // amount of ticket which has been sold
        private int amount;

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " start to sale tickets!");
            try {
                if (barrier.getNumberWaiting() == 4) {
                    System.out.println("============Tickets sold result===========");
                }
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

            while (ticketCount > 0) {
                redisLock.lock();
                if (ticketCount > 0) {
                    ticketCount--;
                    amount++;
                }
                redisLock.unlock();
            }

            System.out.println(Thread.currentThread().getName() + " sold " + amount + " tickets!");
        }
    }
}
