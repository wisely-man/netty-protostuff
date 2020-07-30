package com.example.netty;

import com.wisely.core.exception.SystemException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NettyResponse<V> {

    private final static long TIME_OUT = 10 * 1000l;

    public NettyResponse() {
        this(TIME_OUT);
    }

    public NettyResponse(long timeout) {
        this.timeout = timeout;
        this.LOCK = new ReentrantLock(true);
        this.condition = LOCK.newCondition();
    }

    private final ReentrantLock LOCK;
    private final Condition condition;

    private long timeout;
    private int status;
    private Throwable throwable;
    private V v;



    public void setSuccess(V v){
        System.out.println("received");
        LOCK.lock();
        try {
            this.status = 200;
            if(v == null){
                this.status = 500;
            }
            this.v = v;
            condition.signal();

        } catch (Exception e) {
            throw e;
        } finally {


            LOCK.unlock();
        }
    }

    public void setError(Throwable throwable){
        LOCK.lock();
        try {
            this.status = 500;
            this.throwable = throwable;
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
                    if(isDone() || System.currentTimeMillis()-start > timeout){
                        System.out.println("it break..");
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
        return this.status != 0;
    }

}
