/*
 * Copyright (c) 1996, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.io;

import java.util.Objects;
import java.util.Formatter;
import java.util.Locale;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Prints formatted representations of objects to a text-output stream.  This
 * class implements all of the <tt>print</tt> methods found in {@link
 * PrintStream}.  It does not contain methods for writing raw bytes, for which
 * a program should use unencoded byte streams.
 *
 * <p> Unlike the {@link PrintStream} class, if automatic flushing is enabled
 * it will be done only when one of the <tt>println</tt>, <tt>printf</tt>, or
 * <tt>format</tt> methods is invoked, rather than whenever a newline character
 * happens to be output.  These methods use the platform's own notion of line
 * separator rather than the newline character.
 *
 * <p> Methods in this class never throw I/O exceptions, although some of its
 * constructors may.  The client may inquire as to whether any errors have
 * occurred by invoking {@link #checkError checkError()}.
 *
 * @author      Frank Yellin
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

/*
 * PrintWriter 是一个字符输出流，用于将格式化的对象表示打印到文本输出流。
 *
 * 主要特点：
 * 1. 继承自 Writer，是字符流（不是字节流）
 * 2. 实现了 PrintStream 中的所有 print 方法
 * 3. 支持自动刷新：仅在调用 println、printf 或 format 方法时触发
 * 4. 使用平台的行分隔符（而不是换行符 \n）
 * 5. 方法不会抛出 I/O 异常，可通过 checkError() 检查错误
 *
 * 与 PrintStream 的区别：
 * - PrintWriter 是字符流，PrintStream 是字节流
 * - PrintWriter 的自动刷新只在 println/printf/format 时触发
 * - PrintWriter 更适合处理文本输出
 *
 * 常见用途：
 * - 文件写入
 * - 网络输出
 * - Servlet 响应输出
 * - 日志记录
 *
 * 使用示例：
 *   PrintWriter writer = new PrintWriter(new FileWriter("output.txt"));
 *   writer.println("Hello, World!");
 *   writer.printf("Name: %s, Age: %d", "Alice", 25);
 *   writer.close();
 */
public class PrintWriter extends Writer {

    /**
     * The underlying character-output stream of this
     * <code>PrintWriter</code>.
     *
     * @since 1.2
     */
    /*
     * 底层字符输出流，PrintWriter 的所有写入操作最终都委托给这个流
     */
    protected Writer out;

    /*
     * 自动刷新标志：为 true 时，每次调用 println/printf/format 后自动刷新
     */
    private final boolean autoFlush;

    /*
     * 错误标志：当发生 I/O 异常时设置为 true，不会抛出异常
     */
    private boolean trouble = false;

    /*
     * 格式化器：用于 printf 和 format 方法的格式化输出
     */
    private Formatter formatter;

    /*
     * PrintStream 输出：用于 System.out 等场景
     */
    private PrintStream psOut = null;

    /**
     * Line separator string.  This is the value of the line.separator
     * property at the moment that the stream was created.
     */
    /*
     * 行分隔符：在流创建时从系统属性 line.separator 获取
     * Windows: \r\n, Linux/Mac: \n
     */
    private final String lineSeparator;

    /**
     * Returns a charset object for the given charset name.
     * @throws NullPointerException          is csn is null
     * @throws UnsupportedEncodingException  if the charset is not supported
     */
    private static Charset toCharset(String csn)
        throws UnsupportedEncodingException
    {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        } catch (IllegalCharsetNameException|UnsupportedCharsetException unused) {
            // UnsupportedEncodingException should be thrown
            throw new UnsupportedEncodingException(csn);
        }
    }

    /**
     * Creates a new PrintWriter, without automatic line flushing.
     *
     * @param  out        A character-output stream
     */
    /*
     * 构造方法：创建一个不自动刷新的 PrintWriter
     * @param out 字符输出流
     */
    public PrintWriter (Writer out) {
        this(out, false);
    }

    /**
     * Creates a new PrintWriter.
     *
     * @param  out        A character-output stream
     * @param  autoFlush  A boolean; if true, the <tt>println</tt>,
     *                    <tt>printf</tt>, or <tt>format</tt> methods will
     *                    flush the output buffer
     */
    /*
     * 构造方法：创建一个 PrintWriter
     * @param out 字符输出流
     * @param autoFlush 是否自动刷新：true 时，println/printf/format 会自动刷新
     */
    public PrintWriter(Writer out,
                       boolean autoFlush) {
        super(out);
        this.out = out;
        this.autoFlush = autoFlush;
        lineSeparator = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));
    }

    /**
     * Creates a new PrintWriter, without automatic line flushing, from an
     * existing OutputStream.  This convenience constructor creates the
     * necessary intermediate OutputStreamWriter, which will convert characters
     * into bytes using the default character encoding.
     *
     * @param  out        An output stream
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    /*
     * 构造方法：从 OutputStream 创建不自动刷新的 PrintWriter
     * 内部会创建 OutputStreamWriter 进行字节到字符的转换
     * @param out 字节输出流
     */
    public PrintWriter(OutputStream out) {
        this(out, false);
    }

    /**
     * Creates a new PrintWriter from an existing OutputStream.  This
     * convenience constructor creates the necessary intermediate
     * OutputStreamWriter, which will convert characters into bytes using the
     * default character encoding.
     *
     * @param  out        An output stream
     * @param  autoFlush  A boolean; if true, the <tt>println</tt>,
     *                    <tt>printf</tt>, or <tt>format</tt> methods will
     *                    flush the output buffer
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    /*
     * 构造方法：从 OutputStream 创建 PrintWriter
     * 内部会创建 OutputStreamWriter + BufferedWriter 进行缓冲
     * 如果 out 是 PrintStream，会保存引用用于错误传播
     * @param out 字节输出流
     * @param autoFlush 是否自动刷新
     */
    public PrintWriter(OutputStream out, boolean autoFlush) {
        this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);

        // save print stream for error propagation
        if (out instanceof java.io.PrintStream) {
            psOut = (PrintStream) out;
        }
    }

    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file name.  This convenience constructor creates the necessary
     * intermediate {@link java.io.OutputStreamWriter OutputStreamWriter},
     * which will encode characters using the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this writer.
     *         If the file exists then it will be truncated to zero size;
     *         otherwise, a new file will be created.  The output will be
     *         written to the file and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given string does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @since  1.5
     */
    /*
     * 构造方法：从文件名创建不自动刷新的 PrintWriter
     * 使用平台默认字符集，如果文件存在则清空，不存在则创建
     * @param fileName 文件名
     */
    public PrintWriter(String fileName) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))),
             false);
    }

    /* 私有构造方法：从字符集和文件创建 PrintWriter */
    private PrintWriter(Charset charset, File file)
        throws FileNotFoundException
    {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)),
             false);
    }

    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file name and charset.  This convenience constructor creates
     * the necessary intermediate {@link java.io.OutputStreamWriter
     * OutputStreamWriter}, which will encode characters using the provided
     * charset.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this writer.
     *         If the file exists then it will be truncated to zero size;
     *         otherwise, a new file will be created.  The output will be
     *         written to the file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given string does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     *
     * @since  1.5
     */
    /*
     * 构造方法：从文件名和字符集创建不自动刷新的 PrintWriter
     * @param fileName 文件名
     * @param csn 字符集名称（如 "UTF-8", "GBK"）
     */
    public PrintWriter(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        this(toCharset(csn), new File(fileName));
    }

    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file.  This convenience constructor creates the necessary
     * intermediate {@link java.io.OutputStreamWriter OutputStreamWriter},
     * which will encode characters using the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * @param  file
     *         The file to use as the destination of this writer.  If the file
     *         exists then it will be truncated to zero size; otherwise, a new
     *         file will be created.  The output will be written to the file
     *         and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())}
     *          denies write access to the file
     *
     * @since  1.5
     */
    /*
     * 构造方法：从 File 对象创建不自动刷新的 PrintWriter
     * 使用平台默认字符集
     * @param file 文件对象
     */
    public PrintWriter(File file) throws FileNotFoundException {
        this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))),
             false);
    }

    /**
     * Creates a new PrintWriter, without automatic line flushing, with the
     * specified file and charset.  This convenience constructor creates the
     * necessary intermediate {@link java.io.OutputStreamWriter
     * OutputStreamWriter}, which will encode characters using the provided
     * charset.
     *
     * @param  file
     *         The file to use as the destination of this writer.  If the file
     *         exists then it will be truncated to zero size; otherwise, a new
     *         file will be created.  The output will be written to the file
     *         and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())}
     *          denies write access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     *
     * @since  1.5
     */
    /*
     * 构造方法：从 File 对象和字符集创建不自动刷新的 PrintWriter
     * @param file 文件对象
     * @param csn 字符集名称
     */
    public PrintWriter(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        this(toCharset(csn), file);
    }

    /** Checks to make sure that the stream has not been closed */
    /*
     * 检查流是否已关闭，如果已关闭则抛出 IOException
     */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }

    /**
     * Flushes the stream.
     * @see #checkError()
     */
    /*
     * 刷新流：将缓冲区的数据强制写入底层流
     * 如果发生异常，设置 trouble 标志而不抛出异常
     */
    public void flush() {
        try {
            synchronized (lock) {
                ensureOpen();
                out.flush();
            }
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /**
     * Closes the stream and releases any system resources associated
     * with it. Closing a previously closed stream has no effect.
     *
     * @see #checkError()
     */
    /*
     * 关闭流并释放相关系统资源
     * 关闭已关闭的流不会产生任何效果
     */
    public void close() {
        try {
            synchronized (lock) {
                if (out == null)
                    return;
                out.close();
                out = null;
            }
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /**
     * Flushes the stream if it's not closed and checks its error state.
     *
     * @return <code>true</code> if the print stream has encountered an error,
     *          either on the underlying output stream or during a format
     *          conversion.
     */
    /*
     * 检查流是否发生错误
     * 先刷新流，然后检查错误状态
     * @return true 表示发生了 I/O 错误
     */
    public boolean checkError() {
        if (out != null) {
            flush();
        }
        if (out instanceof java.io.PrintWriter) {
            PrintWriter pw = (PrintWriter) out;
            return pw.checkError();
        } else if (psOut != null) {
            return psOut.checkError();
        }
        return trouble;
    }

    /**
     * Indicates that an error has occurred.
     *
     * <p> This method will cause subsequent invocations of {@link
     * #checkError()} to return <tt>true</tt> until {@link
     * #clearError()} is invoked.
     */
    /*
     * 设置错误标志：标记发生了错误
     * 后续调用 checkError() 将返回 true
     */
    protected void setError() {
        trouble = true;
    }

    /**
     * Clears the error state of this stream.
     *
     * <p> This method will cause subsequent invocations of {@link
     * #checkError()} to return <tt>false</tt> until another write
     * operation fails and invokes {@link #setError()}.
     *
     * @since 1.6
     */
    /*
     * 清除错误标志：重置错误状态
     * 后续调用 checkError() 将返回 false，直到再次发生错误
     */
    protected void clearError() {
        trouble = false;
    }

    /*
     * 以下方法实现了 Writer 的 write() 方法
     * 所有写入操作都是同步的，并捕获 I/O 异常
     */

    /**
     * Writes a single character.
     * @param c int specifying a character to be written.
     */
    /*
     * 写入单个字符
     * @param c 要写入的字符（int 类型）
     */
    public void write(int c) {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(c);
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /**
     * Writes A Portion of an array of characters.
     * @param buf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    /*
     * 写入字符数组的一部分
     * @param buf 字符数组
     * @param off 起始偏移量
     * @param len 要写入的字符数
     */
    public void write(char buf[], int off, int len) {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(buf, off, len);
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /**
     * Writes an array of characters.  This method cannot be inherited from the
     * Writer class because it must suppress I/O exceptions.
     * @param buf Array of characters to be written
     */
    /*
     * 写入整个字符数组
     * @param buf 字符数组
     */
    public void write(char buf[]) {
        write(buf, 0, buf.length);
    }

    /**
     * Writes a portion of a string.
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    /*
     * 写入字符串的一部分
     * @param s 字符串
     * @param off 起始偏移量
     * @param len 要写入的字符数
     */
    public void write(String s, int off, int len) {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(s, off, len);
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /**
     * Writes a string.  This method cannot be inherited from the Writer class
     * because it must suppress I/O exceptions.
     * @param s String to be written
     */
    /*
     * 写入整个字符串
     * @param s 字符串
     */
    public void write(String s) {
        write(s, 0, s.length());
    }

    /*
     * 写入行分隔符（如 \r\n 或 \n）
     * 如果 autoFlush 为 true，写入后自动刷新
     */
    private void newLine() {
        try {
            synchronized (lock) {
                ensureOpen();
                out.write(lineSeparator);
                if (autoFlush)
                    out.flush();
            }
        }
        catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        }
        catch (IOException x) {
            trouble = true;
        }
    }

    /* Methods that do not terminate lines */

    /**
     * Prints a boolean value.  The string produced by <code>{@link
     * java.lang.String#valueOf(boolean)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      b   The <code>boolean</code> to be printed
     */
    public void print(boolean b) {
        write(b ? "true" : "false");
    }

    /**
     * Prints a character.  The character is translated into one or more bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      c   The <code>char</code> to be printed
     */
    public void print(char c) {
        write(c);
    }

    /**
     * Prints an integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(int)}</code> is translated into bytes according
     * to the platform's default character encoding, and these bytes are
     * written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      i   The <code>int</code> to be printed
     * @see        java.lang.Integer#toString(int)
     */
    public void print(int i) {
        write(String.valueOf(i));
    }

    /**
     * Prints a long integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(long)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      l   The <code>long</code> to be printed
     * @see        java.lang.Long#toString(long)
     */
    public void print(long l) {
        write(String.valueOf(l));
    }

    /**
     * Prints a floating-point number.  The string produced by <code>{@link
     * java.lang.String#valueOf(float)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      f   The <code>float</code> to be printed
     * @see        java.lang.Float#toString(float)
     */
    public void print(float f) {
        write(String.valueOf(f));
    }

    /**
     * Prints a double-precision floating-point number.  The string produced by
     * <code>{@link java.lang.String#valueOf(double)}</code> is translated into
     * bytes according to the platform's default character encoding, and these
     * bytes are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      d   The <code>double</code> to be printed
     * @see        java.lang.Double#toString(double)
     */
    public void print(double d) {
        write(String.valueOf(d));
    }

    /**
     * Prints an array of characters.  The characters are converted into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      s   The array of chars to be printed
     *
     * @throws  NullPointerException  If <code>s</code> is <code>null</code>
     */
    public void print(char s[]) {
        write(s);
    }

    /**
     * Prints a string.  If the argument is <code>null</code> then the string
     * <code>"null"</code> is printed.  Otherwise, the string's characters are
     * converted into bytes according to the platform's default character
     * encoding, and these bytes are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      s   The <code>String</code> to be printed
     */
    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    /**
     * Prints an object.  The string produced by the <code>{@link
     * java.lang.String#valueOf(Object)}</code> method is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      obj   The <code>Object</code> to be printed
     * @see        java.lang.Object#toString()
     */
    public void print(Object obj) {
        write(String.valueOf(obj));
    }

    /* Methods that do terminate lines */

    /**
     * Terminates the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public void println() {
        newLine();
    }

    /**
     * Prints a boolean value and then terminates the line.  This method behaves
     * as though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>boolean</code> value to be printed
     */
    public void println(boolean x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a character and then terminates the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then <code>{@link
     * #println()}</code>.
     *
     * @param x the <code>char</code> value to be printed
     */
    public void println(char x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints an integer and then terminates the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then <code>{@link
     * #println()}</code>.
     *
     * @param x the <code>int</code> value to be printed
     */
    public void println(int x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a long integer and then terminates the line.  This method behaves
     * as though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>long</code> value to be printed
     */
    public void println(long x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a floating-point number and then terminates the line.  This method
     * behaves as though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>float</code> value to be printed
     */
    public void println(float x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a double-precision floating-point number and then terminates the
     * line.  This method behaves as though it invokes <code>{@link
     * #print(double)}</code> and then <code>{@link #println()}</code>.
     *
     * @param x the <code>double</code> value to be printed
     */
    public void println(double x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints an array of characters and then terminates the line.  This method
     * behaves as though it invokes <code>{@link #print(char[])}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the array of <code>char</code> values to be printed
     */
    public void println(char x[]) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints a String and then terminates the line.  This method behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x the <code>String</code> value to be printed
     */
    public void println(String x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Prints an Object and then terminates the line.  This method calls
     * at first String.valueOf(x) to get the printed object's string value,
     * then behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     *
     * @param x  The <code>Object</code> to be printed.
     */
    public void println(Object x) {
        String s = String.valueOf(x);
        synchronized (lock) {
            print(s);
            println();
        }
    }

    /**
     * A convenience method to write a formatted string to this writer using
     * the specified format string and arguments.  If automatic flushing is
     * enabled, calls to this method will flush the output buffer.
     *
     * <p> An invocation of this method of the form <tt>out.printf(format,
     * args)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.format(format, args) </pre>
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>.
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     *
     * @return  This writer
     *
     * @since  1.5
     */
    public PrintWriter printf(String format, Object ... args) {
        return format(format, args);
    }

    /**
     * A convenience method to write a formatted string to this writer using
     * the specified format string and arguments.  If automatic flushing is
     * enabled, calls to this method will flush the output buffer.
     *
     * <p> An invocation of this method of the form <tt>out.printf(l, format,
     * args)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.format(l, format, args) </pre>
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If <tt>l</tt> is <tt>null</tt> then no localization
     *         is applied.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>.
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     *
     * @return  This writer
     *
     * @since  1.5
     */
    public PrintWriter printf(Locale l, String format, Object ... args) {
        return format(l, format, args);
    }

    /**
     * Writes a formatted string to this writer using the specified format
     * string and arguments.  If automatic flushing is enabled, calls to this
     * method will flush the output buffer.
     *
     * <p> The locale always used is the one returned by {@link
     * java.util.Locale#getDefault() Locale.getDefault()}, regardless of any
     * previous invocations of other formatting methods on this object.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>.
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          Formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     *
     * @return  This writer
     *
     * @since  1.5
     */
    public PrintWriter format(String format, Object ... args) {
        try {
            synchronized (lock) {
                ensureOpen();
                if ((formatter == null)
                    || (formatter.locale() != Locale.getDefault()))
                    formatter = new Formatter(this);
                formatter.format(Locale.getDefault(), format, args);
                if (autoFlush)
                    out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            trouble = true;
        }
        return this;
    }

    /**
     * Writes a formatted string to this writer using the specified format
     * string and arguments.  If automatic flushing is enabled, calls to this
     * method will flush the output buffer.
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If <tt>l</tt> is <tt>null</tt> then no localization
     *         is applied.
     *
     * @param  format
     *         A format string as described in <a
     *         href="../util/Formatter.html#syntax">Format string syntax</a>.
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *         The behaviour on a
     *         <tt>null</tt> argument depends on the <a
     *         href="../util/Formatter.html#syntax">conversion</a>.
     *
     * @throws  java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the <a
     *          href="../util/Formatter.html#detail">Details</a> section of the
     *          formatter class specification.
     *
     * @throws  NullPointerException
     *          If the <tt>format</tt> is <tt>null</tt>
     *
     * @return  This writer
     *
     * @since  1.5
     */
    public PrintWriter format(Locale l, String format, Object ... args) {
        try {
            synchronized (lock) {
                ensureOpen();
                if ((formatter == null) || (formatter.locale() != l))
                    formatter = new Formatter(this, l);
                formatter.format(l, format, args);
                if (autoFlush)
                    out.flush();
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            trouble = true;
        }
        return this;
    }

    /**
     * Appends the specified character sequence to this writer.
     *
     * <p> An invocation of this method of the form <tt>out.append(csq)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> Depending on the specification of <tt>toString</tt> for the
     * character sequence <tt>csq</tt>, the entire sequence may not be
     * appended. For instance, invoking the <tt>toString</tt> method of a
     * character buffer will return a subsequence whose content depends upon
     * the buffer's position and limit.
     *
     * @param  csq
     *         The character sequence to append.  If <tt>csq</tt> is
     *         <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *         appended to this writer.
     *
     * @return  This writer
     *
     * @since  1.5
     */
    public PrintWriter append(CharSequence csq) {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     *
     * <p> An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in
     * exactly the same way as the invocation
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *         will be appended as if <tt>csq</tt> contained the four
     *         characters <tt>"null"</tt>.
     *
     * @param  start
     *         The index of the first character in the subsequence
     *
     * @param  end
     *         The index of the character following the last character in the
     *         subsequence
     *
     * @return  This writer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *          is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *          <tt>csq.length()</tt>
     *
     * @since  1.5
     */
    public PrintWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * Appends the specified character to this writer.
     *
     * <p> An invocation of this method of the form <tt>out.append(c)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.write(c) </pre>
     *
     * @param  c
     *         The 16-bit character to append
     *
     * @return  This writer
     *
     * @since 1.5
     */
    public PrintWriter append(char c) {
        write(c);
        return this;
    }
}
