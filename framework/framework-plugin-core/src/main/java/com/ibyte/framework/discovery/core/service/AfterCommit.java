package com.ibyte.framework.discovery.core.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.Set;

/**
 * 事务提交后触发动作
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Component
public class AfterCommit extends TransactionSynchronizationAdapter {
    private static final ThreadLocal<Set<Runnable>> RUNNABLES = new ThreadLocal<Set<Runnable>>();

    public void execute(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runnable.run();
            return;
        }
        Set<Runnable> threadRunnables = RUNNABLES.get();
        if (threadRunnables == null) {
            threadRunnables = new HashSet<Runnable>();
            RUNNABLES.set(threadRunnables);
            TransactionSynchronizationManager.registerSynchronization(this);
        }
        threadRunnables.add(runnable);
    }

    @Override
    public void afterCommit() {
        Set<Runnable> threadRunnables = RUNNABLES.get();
        for (Runnable runnable : threadRunnables) {
            runnable.run();
        }
    }

    @Override
    public void afterCompletion(int status) {
        RUNNABLES.remove();
    }
}
