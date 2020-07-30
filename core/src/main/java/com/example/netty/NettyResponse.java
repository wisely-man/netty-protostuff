package com.example.netty;

import com.wisely.core.exception.SystemException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NettyResponse<V> {

    private final static long TIME_OUT = 90 * 1000l;

    public NettyResponse() {
        this.timeout = TIME_OUT;
    }

    public NettyResponse(long timeout) {
        this.timeout = timeout;
    }

    private final ReentrantLock LOCK = new ReentrantLock(true);
    private final Condition condition = LOCK.newCondition();

    private long timeout;
    private V v;



    public void set(V v){
        System.out.println("received");
        LOCK.lock();
        try {
            this.v = v;
            condition.signal();

        } catch (Exception e) {
            throw e;
        } finally {
            LOCK.unlock();
        }
    }


    public V get(){
        if(!isDone()){
            Long start = System.currentTimeMillis();
            LOCK.lock();
            try {
                while(!isDone()) {
                    condition.await(timeout, TimeUnit.MILLISECONDS);
                    System.out.println("signal");
                    if(!isDone() || System.currentTimeMillis()-start > timeout){
                        break;
                    }
                }
            } catch (Exception e) {
                throw new SystemException("netty request waiting in error");
            } finally {
                LOCK.unlock();
            }

            if(!isDone()){
                throw new SystemException("netty request time out");
            }
        }

        return v;
    }


    private Boolean isDone(){
        return this.v != null;
    }

}
