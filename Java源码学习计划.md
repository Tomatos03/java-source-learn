# Java源码学习计划

> 本计划基于项目中的Java标准库源码，按照从基础到高级的顺序编排，共30周。

---

## 第一阶段：Java语言基础（第1-4周）

**核心目标**：理解Java语言最基础的设计

### 第1周：Object类和基本类型

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | Object.java | equals(), hashCode(), toString(), clone(), wait(), notify() |
| Day 3-4 | Integer.java & Long.java | 自动装箱/拆箱、缓存机制（-128~127） |
| Day 5-7 | String.java | 不可变性、常量池、intern()方法 |

### 第2周：字符串相关

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | AbstractStringBuilder.java | 可变字符序列的实现 |
| Day 3-4 | StringBuilder.java & StringBuffer.java | 线程安全差异、扩容策略 |
| Day 5-7 | CharSequence.java & Comparable.java | 接口设计、函数式接口 |

### 第3周：异常体系

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | Throwable.java | 异常链、堆栈跟踪、序列化 |
| Day 3-4 | Exception.java & RuntimeException.java | 受检异常 vs 非受检异常 |
| Day 5-7 | 常用异常类 | NullPointerException, IllegalArgumentException, IllegalStateException, IndexOutOfBoundsException |

### 第4周：枚举和注解

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | Enum.java | 枚举的本质、values()、valueOf() |
| Day 4-5 | 注解相关 | Annotation.java, Override.java, Deprecated.java, FunctionalInterface.java |
| Day 6-7 | Iterable.java & Iterator.java | 迭代器模式、forEach方法 |

---

## 第二阶段：集合框架（第5-10周）

**核心目标**：掌握Java集合的设计哲学

### 第5周：集合接口

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | Collection.java & List.java | 接口方法设计、默认实现 |
| Day 3-4 | Set.java & Map.java | Set与Map的关系、接口设计 |
| Day 5-7 | Queue.java & Deque.java | 队列操作、双端队列 |

### 第6周：抽象实现类

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | AbstractCollection.java & AbstractList.java | 骨架实现、模板方法模式 |
| Day 3-4 | AbstractSet.java & AbstractMap.java | Set与Map的实现关联 |
| Day 5-7 | AbstractQueue.java & AbstractSequentialList.java | 队列和链表的抽象实现 |

### 第7周：List实现

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | ArrayList.java | 动态数组、扩容策略（1.5倍）、fail-fast |
| Day 4-5 | LinkedList.java | 双向链表、List和Deque的双重实现 |
| Day 6-7 | Vector.java & Stack.java | 线程安全、历史遗留问题 |

### 第8周：Map实现

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | **HashMap.java** ⭐⭐⭐ | 数组+链表+红黑树结构、hash()方法、put()/get()流程、扩容机制、树化条件 |
| Day 4-5 | LinkedHashMap.java | 维护插入顺序、LRU缓存实现 |
| Day 6-7 | TreeMap.java | 红黑树实现、Comparator |

### 第9周：Set实现

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | HashSet.java & LinkedHashSet.java | 基于HashMap实现、组合优于继承 |
| Day 3-4 | TreeSet.java | 基于TreeMap实现、NavigableSet接口 |
| Day 5-7 | EnumSet.java & EnumMap.java | 枚举的特殊优化 |

### 第10周：其他集合类

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | Arrays.java & Collections.java | 工具类设计、不可变集合 |
| Day 3-4 | Hashtable.java & Properties.java | 线程安全、历史遗留 |
| Day 5-7 | Optional.java | 空值处理、函数式风格 |

---

## 第三阶段：IO流（第11-14周）

**核心目标**：理解IO的设计模式

### 第11周：IO基础接口

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | InputStream.java & OutputStream.java | 抽象方法、read()的语义 |
| Day 3-4 | Reader.java & Writer.java | 字符流、编码处理 |
| Day 5-7 | Closeable.java & Flushable.java & AutoCloseable.java | 资源管理、try-with-resources |

### 第12周：文件IO

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | File.java | 文件抽象、路径处理 |
| Day 3-4 | FileInputStream.java & FileOutputStream.java | native方法、文件描述符 |
| Day 5-7 | FileReader.java & FileWriter.java | 字符编码、默认编码 |

### 第13周：缓冲流

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | BufferedInputStream.java & BufferedOutputStream.java | 缓冲区设计、批量操作 |
| Day 3-4 | BufferedReader.java & BufferedWriter.java | readLine()实现、行处理 |
| Day 5-7 | PrintWriter.java & PrintStream.java | 格式化输出、自动刷新 |

### 第14周：高级IO

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | DataInputStream.java & DataOutputStream.java | 基本类型的序列化 |
| Day 3-5 | ObjectInputStream.java & ObjectOutputStream.java | 对象序列化、serialVersionUID |
| Day 6-7 | RandomAccessFile.java | 随机访问、文件指针 |

---

## 第四阶段：并发编程（第15-20周）

**核心目标**：掌握Java并发的核心机制

### 第15周：Thread基础

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | **Thread.java** ⭐⭐⭐ | 线程状态、start()/run()/sleep()/join()/interrupt()、线程组、守护线程 |
| Day 4-5 | Runnable.java & Callable.java | 任务抽象、返回值差异 |
| Day 6-7 | ThreadLocal.java & InheritableThreadLocal.java | 线程局部变量、内存泄漏 |

### 第16周：同步机制

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | 锁相关（java.util.concurrent.locks包） | Lock.java, ReentrantLock.java, ReadWriteLock.java, ReentrantReadWriteLock.java |
| Day 4-5 | Condition.java | 等待/通知机制、与Object.wait()的区别 |
| Day 6-7 | **AbstractQueuedSynchronizer.java (AQS)** ⭐⭐⭐ | 状态管理（state）、独占模式 vs 共享模式、CLH队列 |

### 第17周：线程池

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | **ThreadPoolExecutor.java** ⭐⭐⭐ | 核心参数、线程池状态、任务提交流程、拒绝策略 |
| Day 4-5 | Executors.java | 工厂方法、各种线程池配置 |
| Day 6-7 | ExecutorService.java & Future.java & FutureTask.java | 任务提交、异步结果 |

### 第18周：并发集合

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | **ConcurrentHashMap.java** ⭐⭐⭐ | 分段锁（JDK7）→ CAS+synchronized（JDK8）、数组+链表+红黑树 |
| Day 4-5 | CopyOnWriteArrayList.java & CopyOnWriteArraySet.java | 写时复制、读多写少场景 |
| Day 6-7 | ConcurrentLinkedQueue.java & ConcurrentLinkedDeque.java | 无锁队列、CAS操作 |

### 第19周：阻塞队列

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | BlockingQueue.java & BlockingDeque.java | 阻塞操作、生产者-消费者模式 |
| Day 3-4 | ArrayBlockingQueue.java & LinkedBlockingQueue.java | 有界队列、锁的选择 |
| Day 5-7 | SynchronousQueue.java & PriorityBlockingQueue.java & DelayQueue.java | 特殊队列、优先级、延迟 |

### 第20周：并发工具

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | CountDownLatch.java & CyclicBarrier.java | 倒计时 vs 屏障、使用场景 |
| Day 3-4 | Semaphore.java & Phaser.java | 信号量、灵活的同步 |
| Day 5-7 | **CompletableFuture.java** ⭐⭐ | 链式调用、异常处理、组合多个Future |

---

## 第五阶段：NIO与网络（第21-24周）

**核心目标**：理解高性能IO的设计

### 第21周：NIO基础

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | **Buffer.java** ⭐⭐ | position, limit, capacity, mark |
| Day 3-4 | ByteBuffer.java | 直接缓冲区、堆缓冲区 |
| Day 5-7 | ByteOrder.java & MappedByteBuffer.java | 字节序、内存映射文件 |

### 第22周：NIO通道

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | Channel.java (interface) | 通道接口设计 |
| Day 4-7 | channels包 | FileChannel.java, SocketChannel.java, ServerSocketChannel.java |

### 第23周：NIO选择器

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | Selector相关 | Selector.java, SelectionKey.java, 多路复用、事件驱动 |
| Day 4-7 | 字符集（charset包） | Charset.java, CharsetEncoder.java, CharsetDecoder.java |

### 第24周：网络编程

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | java.net包 | Socket.java, ServerSocket.java, URL.java, URLConnection.java, InetAddress.java |
| Day 4-7 | 高级网络 | DatagramSocket.java, DatagramPacket.java, Proxy.java, CookieHandler.java |

---

## 第六阶段：高级主题（第25-30周）

**核心目标**：深入理解JVM和高级特性

### 第25周：反射机制

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | **Class.java** ⭐⭐ | 类加载、获取构造器/方法/字段、泛型处理 |
| Day 4-5 | Method.java & Field.java & Constructor.java | 方法调用、字段访问、对象创建 |
| Day 6-7 | Proxy.java & InvocationHandler.java | 动态代理、AOP基础 |

### 第26周：类加载机制

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | **ClassLoader.java** ⭐⭐ | 双亲委派模型、findClass() vs loadClass()、自定义类加载器 |
| Day 4-7 | 其他类加载相关 | SecureClassLoader.java, URLClassLoader.java |

### 第27周：泛型

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | 泛型相关 | TypeVariable.java, ParameterizedType.java, GenericArrayType.java, WildcardType.java |
| Day 4-7 | 泛型应用 | Collections中的泛型方法、Optional的泛型设计 |

### 第28周：注解处理

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | 注解相关（lang.annotation包） | Annotation.java, AnnotatedElement.java |
| Day 4-7 | 自定义注解 | 创建自定义注解、运行时注解处理 |

### 第29周：安全管理

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-3 | java.security包 | SecurityManager.java, Permission.java |
| Day 4-7 | 加密相关 | MessageDigest.java, Signature.java |

### 第30周：其他重要类

| 天数 | 学习内容 | 重点内容 |
|------|----------|----------|
| Day 1-2 | Math.java & StrictMath.java | 数学运算、精度控制 |
| Day 3-4 | System.java & Runtime.java | 系统操作、垃圾回收 |
| Day 5-7 | Process.java & ProcessBuilder.java | 进程创建、命令执行 |

---
