import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 参考 Netty HashedWheelTimer 实现的时间轮定时器
 *
 * 时间轮是一种高效的定时任务调度算法，适用于大量定时任务的场景
 * 核心思想：将时间划分为固定的时间片，使用环形数组和链表存储任务
 */
public class ThreadJoinInterruptDemo {

    /**
     * 时间轮定时器
     */
    static class HashedWheelTimer {
        // 时间轮的轮盘（桶数组）
        private final HashedWheelBucket[] wheel;
        // 轮盘大小（必须是2的幂）
        private final int mask;
        // 每个时间片的持续时间（纳秒）
        private final long tickDuration;
        // 工作线程
        private final Thread workerThread;
        // 当前时间片索引
        private long tick;
        // 启动时间
        private volatile long startTime;
        // 待添加的任务队列（使用链表实现无锁队列）
        private volatile HashedWheelTimeout[] timeouts = new HashedWheelTimeout[0];
        private volatile int timeoutCount = 0;

        /**
         * 创建时间轮定时器
         * @param tickDuration 每个时间片的时长
         * @param unit 时间单位
         * @param ticksPerWheel 轮盘大小
         */
        public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel) {
            this.tickDuration = unit.toNanos(tickDuration);

            // 确保轮盘大小是2的幂
            int normalizedTicks = normalizeTicksPerWheel(ticksPerWheel);
            this.wheel = new HashedWheelBucket[normalizedTicks];
            this.mask = normalizedTicks - 1;

            // 初始化桶
            for (int i = 0; i < normalizedTicks; i++) {
                wheel[i] = new HashedWheelBucket();
            }

            // 创建工作线程（守护线程）
            workerThread = new Thread(() -> {
                // 等待启动
                long currentTime = System.nanoTime();
                startTime = currentTime;
                System.out.println("时间轮守护线程启动，启动时间: " + startTime);

                // 主循环
                while (true) {
                    // 等待下一个时间片
                    long sleepTimeMs = waitForNextTick();
                    if (sleepTimeMs > 0) {
                        try {
                            Thread.sleep(sleepTimeMs);
                        } catch (InterruptedException e) {
                            System.out.println("时间轮线程被中断");
                            break;
                        }
                    }

                    // 处理当前时间片
                    processTimeouts();
                }
            }, "HashedWheelTimer-Worker");

            // 设置为守护线程
            workerThread.setDaemon(true);
        }

        /**
         * 将 ticksPerWheel 规范化为2的幂
         */
        private int normalizeTicksPerWheel(int ticksPerWheel) {
            int normalized = 1;
            while (normalized < ticksPerWheel) {
                normalized <<= 1;
            }
            return normalized;
        }

        /**
         * 启动时间轮
         */
        public void start() {
            switch (workerThread.getState()) {
                case NEW:
                    workerThread.start();
                    break;
                case TERMINATED:
                    throw new IllegalStateException("Timer has been stopped");
                default:
                    break;
            }
        }

        /**
         * 等待下一个时间片
         * @return 需要休眠的毫秒数
         */
        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);

            while (true) {
                final long currentTime = System.nanoTime() - startTime;
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;

                if (sleepTimeMs <= 0) {
                    tick++;
                    return currentTime - deadline > tickDuration ? (currentTime - deadline) / tickDuration : 0;
                }

                return sleepTimeMs;
            }
        }

        /**
         * 处理当前时间片的超时任务
         */
        private void processTimeouts() {
            // 处理待添加的任务
            transferTimeoutsToBuckets();

            // 获取当前桶
            HashedWheelBucket bucket = wheel[(int) (tick & mask)];
            // 处理桶中的超时任务
            bucket.expireTimeouts();
        }

        /**
         * 将待添加的任务转移到对应的桶中
         */
        private void transferTimeoutsToBuckets() {
            for (int i = 0; i < timeoutCount; i++) {
                HashedWheelTimeout timeout = timeouts[i];
                if (timeout.state() == HashedWheelTimeout.ST_CANCELLED) {
                    continue;
                }

                // 计算任务应该在第几个时间片执行
                long calculated = timeout.deadline / tickDuration;
                timeout.remainingRounds = (calculated - tick) / wheel.length;

                final long ticks = Math.max(calculated, tick);
                int stopIndex = (int) (ticks & mask);

                // 将任务添加到对应的桶
                HashedWheelBucket bucket = wheel[stopIndex];
                bucket.addTimeout(timeout);
            }
            // 清空待添加队列
            timeouts = new HashedWheelTimeout[0];
            timeoutCount = 0;
        }

        /**
         * 添加定时任务
         * @param task 要执行的任务
         * @param delay 延迟时间
         * @param unit 时间单位
         * @return 超时对象，可用于取消任务
         */
        public HashedWheelTimeout newTimeout(Runnable task, long delay, TimeUnit unit) {
            long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
            HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);

            // 将任务添加到待处理队列
            addTimeout(timeout);
            return timeout;
        }

        /**
         * 添加超时任务到待处理队列
         */
        private void addTimeout(HashedWheelTimeout timeout) {
            synchronized (this) {
                HashedWheelTimeout[] newTimeouts = new HashedWheelTimeout[timeoutCount + 1];
                System.arraycopy(timeouts, 0, newTimeouts, 0, timeoutCount);
                newTimeouts[timeoutCount] = timeout;
                timeouts = newTimeouts;
                timeoutCount++;
            }
        }

        /**
         * 停止时间轮
         */
        public void stop() {
            workerThread.interrupt();
        }

        /**
         * 获取待处理的任务数量
         */
        public int pendingTimeouts() {
            return timeoutCount;
        }
    }

    /**
     * 桶：存储同一时间片的任务链表
     */
    static class HashedWheelBucket {
        // 链表头节点
        private HashedWheelTimeout head;
        // 链表尾节点
        private HashedWheelTimeout tail;

        /**
         * 添加超时任务到桶中
         */
        public void addTimeout(HashedWheelTimeout timeout) {
            if (head == null) {
                head = tail = timeout;
            } else {
                tail.next = timeout;
                timeout.prev = tail;
                tail = timeout;
            }
        }

        /**
         * 处理桶中已超时的任务
         */
        public void expireTimeouts() {
            HashedWheelTimeout timeout = head;

            while (timeout != null) {
                HashedWheelTimeout next = timeout.next;
                if (timeout.remainingRounds <= 0) {
                    // 移除并执行任务
                    next = remove(timeout);
                    if (timeout.state() != HashedWheelTimeout.ST_CANCELLED) {
                        try {
                            timeout.run();
                        } catch (Throwable t) {
                            System.err.println("任务执行异常: " + t.getMessage());
                        }
                    }
                } else {
                    timeout.remainingRounds--;
                }
                timeout = next;
            }
        }

        /**
         * 从桶中移除任务
         */
        public HashedWheelTimeout remove(HashedWheelTimeout timeout) {
            HashedWheelTimeout next = timeout.next;

            // 更新前驱节点
            if (timeout.prev != null) {
                timeout.prev.next = next;
            }

            // 更新后继节点
            if (timeout.next != null) {
                timeout.next.prev = timeout.prev;
            }

            // 如果是头节点
            if (timeout == head) {
                if (timeout == tail) {
                    tail = null;
                    head = null;
                } else {
                    head = next;
                }
            } else if (timeout == tail) {
                tail = timeout.prev;
            }

            // 清空节点的指针
            timeout.prev = null;
            timeout.next = null;

            return next;
        }
    }

    /**
     * 超时任务节点
     */
    static class HashedWheelTimeout {
        // 状态常量
        static final int ST_INIT = 0;
        static final int ST_CANCELLED = 1;
        static final int ST_EXPIRED = 2;

        // 状态更新器
        private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");

        // 时间轮引用
        private final HashedWheelTimer timer;
        // 要执行的任务
        private final Runnable task;
        // 截止时间（纳秒）
        private final long deadline;
        // 剩余轮数
        long remainingRounds;
        // 链表指针
        HashedWheelTimeout next;
        HashedWheelTimeout prev;
        // 状态
        private volatile int state = ST_INIT;

        HashedWheelTimeout(HashedWheelTimer timer, Runnable task, long deadline) {
            this.timer = timer;
            this.task = task;
            this.deadline = deadline;
        }

        /**
         * 获取当前状态
         */
        public int state() {
            return state;
        }

        /**
         * 取消任务
         * @return 是否成功取消
         */
        public boolean cancel() {
            if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
                return false;
            }
            return true;
        }

        /**
         * 判断任务是否已取消
         */
        public boolean isCancelled() {
            return state() == ST_CANCELLED;
        }

        /**
         * 判断任务是否已过期
         */
        public boolean isExpired() {
            return state() == ST_EXPIRED;
        }

        /**
         * 执行任务
         */
        public void run() {
            if (compareAndSetState(ST_INIT, ST_EXPIRED)) {
                task.run();
            }
        }

        /**
         * CAS更新状态
         */
        private boolean compareAndSetState(int expected, int update) {
            return STATE_UPDATER.compareAndSet(this, expected, update);
        }
    }

    /**
     * 测试时间轮定时任务
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 参考 Netty 实现的时间轮定时器演示 ===\n");

        // 创建时间轮：每个时间片100毫秒，共16个槽位
        HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 16);
        timer.start();

        System.out.println("时间轮已启动\n");

        // 添加多个定时任务
        System.out.println("添加定时任务...");

        // 任务1：200毫秒后执行
        HashedWheelTimeout timeout1 = timer.newTimeout(() -> {
            System.out.println("[任务1] 执行时间: " + System.currentTimeMillis() + " - 打印消息");
        }, 200, TimeUnit.MILLISECONDS);

        // 任务2：500毫秒后执行
        HashedWheelTimeout timeout2 = timer.newTimeout(() -> {
            System.out.println("[任务2] 执行时间: " + System.currentTimeMillis() + " - 计算任务");
            int sum = 0;
            for (int i = 0; i < 1000000; i++) {
                sum += i;
            }
            System.out.println("[任务2] 计算结果: " + sum);
        }, 500, TimeUnit.MILLISECONDS);

        // 任务3：1秒后执行
        HashedWheelTimeout timeout3 = timer.newTimeout(() -> {
            System.out.println("[任务3] 执行时间: " + System.currentTimeMillis() + " - 长延迟任务");
        }, 1000, TimeUnit.MILLISECONDS);

        // 任务4：2秒后执行（用于测试取消）
        HashedWheelTimeout timeout4 = timer.newTimeout(() -> {
            System.out.println("[任务4] 这个任务应该被取消，不会执行");
        }, 2000, TimeUnit.MILLISECONDS);

        // 取消任务4
        System.out.println("取消任务4: " + (timeout4.cancel() ? "成功" : "失败"));
        System.out.println("任务4是否已取消: " + timeout4.isCancelled());

        System.out.println("\n当前待处理任务数: " + timer.pendingTimeouts());

        // 主线程等待，观察任务执行
        System.out.println("\n主线程等待任务执行...");
        Thread.sleep(3000);

        // 停止时间轮
        timer.stop();
        System.out.println("\n时间轮已停止");
        System.out.println("=== 演示结束 ===");
    }
}
