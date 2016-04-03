package se.bitcraze.crazyflie.client.weather;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Lukas on 4/20/14.
 */
public class WeatherService implements Runnable {
    CyclicBarrier barrier1 = null;
    CyclicBarrier barrier2 = null;
    CyclicBarrier barrier3 = null;

    public WeatherService(
            CyclicBarrier barrier1,
            CyclicBarrier barrier2,
            CyclicBarrier barrier3) {

        this.barrier1 = barrier1;
        this.barrier2 = barrier2;
        this.barrier3 = barrier3;
    }

    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() +
                    " waiting at barrier 1");
            this.barrier1.await();

            System.out.println(Thread.currentThread().getName() +
                    " waiting at barrier 2");
            this.barrier2.await();

            System.out.println(Thread.currentThread().getName() +
                    " waiting at barrier 3");
            this.barrier3.await();

            System.out.println(Thread.currentThread().getName() +
                    " done!");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
