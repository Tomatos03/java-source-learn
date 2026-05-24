/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Serializability of a class is enabled by the class implementing the
 * java.io.Serializable interface. Classes that do not implement this
 * interface will not have any of their state serialized or
 * deserialized.  All subtypes of a serializable class are themselves
 * serializable.  The serialization interface has no methods or fields
 * and serves only to identify the semantics of being serializable. <p>
 *
 * To allow subtypes of non-serializable classes to be serialized, the
 * subtype may assume responsibility for saving and restoring the
 * state of the supertype's public, protected, and (if accessible)
 * package fields.  The subtype may assume this responsibility only if
 * the class it extends has an accessible no-arg constructor to
 * initialize the class's state.  It is an error to declare a class
 * Serializable if this is not the case.  The error will be detected at
 * runtime. <p>
 *
 * During deserialization, the fields of non-serializable classes will
 * be initialized using the public or protected no-arg constructor of
 * the class.  A no-arg constructor must be accessible to the subclass
 * that is serializable.  The fields of serializable subclasses will
 * be restored from the stream. <p>
 *
 * When traversing a graph, an object may be encountered that does not
 * support the Serializable interface. In this case the
 * NotSerializableException will be thrown and will identify the class
 * of the non-serializable object. <p>
 *
 * Classes that require special handling during the serialization and
 * deserialization process must implement special methods with these exact
 * signatures:
 *
 * <PRE>
 * private void writeObject(java.io.ObjectOutputStream out)
 *     throws IOException
 * private void readObject(java.io.ObjectInputStream in)
 *     throws IOException, ClassNotFoundException;
 * private void readObjectNoData()
 *     throws ObjectStreamException;
 * </PRE>
 *
 * <p>The writeObject method is responsible for writing the state of the
 * object for its particular class so that the corresponding
 * readObject method can restore it.  The default mechanism for saving
 * the Object's fields can be invoked by calling
 * out.defaultWriteObject. The method does not need to concern
 * itself with the state belonging to its superclasses or subclasses.
 * State is saved by writing the individual fields to the
 * ObjectOutputStream using the writeObject method or by using the
 * methods for primitive data types supported by DataOutput.
 *
 * <p>The readObject method is responsible for reading from the stream and
 * restoring the classes fields. It may call in.defaultReadObject to invoke
 * the default mechanism for restoring the object's non-static and
 * non-transient fields.  The defaultReadObject method uses information in
 * the stream to assign the fields of the object saved in the stream with the
 * correspondingly named fields in the current object.  This handles the case
 * when the class has evolved to add new fields. The method does not need to
 * concern itself with the state belonging to its superclasses or subclasses.
 * State is saved by writing the individual fields to the
 * ObjectOutputStream using the writeObject method or by using the
 * methods for primitive data types supported by DataOutput.
 *
 * <p>The readObjectNoData method is responsible for initializing the state of
 * the object for its particular class in the event that the serialization
 * stream does not list the given class as a superclass of the object being
 * deserialized.  This may occur in cases where the receiving party uses a
 * different version of the deserialized instance's class than the sending
 * party, and the receiver's version extends classes that are not extended by
 * the sender's version.  This may also occur if the serialization stream has
 * been tampered; hence, readObjectNoData is useful for initializing
 * deserialized objects properly despite a "hostile" or incomplete source
 * stream.
 *
 * <p>Serializable classes that need to designate an alternative object to be
 * used when writing an object to the stream should implement this
 * special method with the exact signature:
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
 * </PRE><p>
 *
 * This writeReplace method is invoked by serialization if the method
 * exists and it would be accessible from a method defined within the
 * class of the object being serialized. Thus, the method can have private,
 * protected and package-private access. Subclass access to this method
 * follows java accessibility rules. <p>
 *
 * Classes that need to designate a replacement when an instance of it
 * is read from the stream should implement this special method with the
 * exact signature.
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
 * </PRE><p>
 *
 * This readResolve method follows the same invocation rules and
 * accessibility rules as writeReplace.<p>
 *
 * The serialization runtime associates with each serializable class a version
 * number, called a serialVersionUID, which is used during deserialization to
 * verify that the sender and receiver of a serialized object have loaded
 * classes for that object that are compatible with respect to serialization.
 * If the receiver has loaded a class for the object that has a different
 * serialVersionUID than that of the corresponding sender's class, then
 * deserialization will result in an {@link InvalidClassException}.  A
 * serializable class can declare its own serialVersionUID explicitly by
 * declaring a field named <code>"serialVersionUID"</code> that must be static,
 * final, and of type <code>long</code>:
 *
 * <PRE>
 * ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;
 * </PRE>
 *
 * If a serializable class does not explicitly declare a serialVersionUID, then
 * the serialization runtime will calculate a default serialVersionUID value
 * for that class based on various aspects of the class, as described in the
 * Java(TM) Object Serialization Specification.  However, it is <em>strongly
 * recommended</em> that all serializable classes explicitly declare
 * serialVersionUID values, since the default serialVersionUID computation is
 * highly sensitive to class details that may vary depending on compiler
 * implementations, and can thus result in unexpected
 * <code>InvalidClassException</code>s during deserialization.  Therefore, to
 * guarantee a consistent serialVersionUID value across different java compiler
 * implementations, a serializable class must declare an explicit
 * serialVersionUID value.  It is also strongly advised that explicit
 * serialVersionUID declarations use the <code>private</code> modifier where
 * possible, since such declarations apply only to the immediately declaring
 * class--serialVersionUID fields are not useful as inherited members. Array
 * classes cannot declare an explicit serialVersionUID, so they always have
 * the default computed value, but the requirement for matching
 * serialVersionUID values is waived for array classes.
 *
 * @author  unascribed
 * @see java.io.ObjectOutputStream
 * @see java.io.ObjectInputStream
 * @see java.io.ObjectOutput
 * @see java.io.ObjectInput
 * @see java.io.Externalizable
 * @since   JDK1.1
 */

 /**
  * - 序列化接口没有方法或字段，仅用于标识可序列化的语义。
  * - 实现 Serializable 接口即可启用序列化，未实现则不参与序列化。
  * - 可序列化类的所有子类型本身都是可序列化的。
  * - 遍历对象图时遇到不可序列化的对象将抛出 NotSerializableException。
  *
  * 非序列化父类的要求：父类未实现 Serializable 时，必须提供无参构造函数（public/protected），
  * 否则反序列化时抛出 InvalidClassException。反序列化时父类字段通过无参构造函数初始化，
  * 子类字段则从流中恢复。
  *
  * 在序列化和反序列化过程中需要特殊处理的类必须实现具有以下精确签名的特殊方法：
  *
  * 自定义序列化
  * private void writeObject(java.io.ObjectOutputStream out) throws IOException
  * 自定义反序列化
  * private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException;
  * 处理不完整流
  * private void readObjectNoData() throws ObjectStreamException;
  *
  * writeObject：自定义序列化逻辑，通过 out.defaultWriteObject() 写入默认字段，
  * 再手动写入需要特殊处理的字段（如 transient 敏感数据加密后写入）。
  *
  * 例：
  * public class User {
  *   private String name;
  *   private int age;
  *   private transient String password;
  *   private void writeObject(ObjectOutputStream oos) throws IOException {
  *     oos.defaultWriteObject();           // 先自动写 name、age
  *     oos.writeUTF(encrypt(password));    // 再手动写加密后的 password
  *   }
  * }
  *
  * readObject：自定义反序列化逻辑，通过 in.defaultReadObject() 恢复默认字段，
  * 可处理类新增字段（流中无该字段时使用默认值）。
  *
  * 例：
  * class User implements Serializable {
  *    private String name;
  *    private int age;
  *    private String email;  // 新版本新增的字段, 流中没有，使用默认值
  *
  *    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
  *       ois.defaultReadObject();  // 自动恢复 name、age、email
  *    }
  * }
  *
  * readObjectNoData：当序列化流中找不到该类的数据时（如版本不匹配或流被篡改），
  * 用于兜底初始化该类对象的状态。
  *
  * 例：
  * class Animal implements Serializable {
  *     private String name;
  *
  *     // 流中找不到 Animal 数据时，兜底初始化
  *     private void readObjectNoData() throws ObjectStreamException {
  *         this.name = "unknown";  // 安全的默认值
  *     }
  * }
  *
  * class Dog extends Animal implements Serializable {
  *     private String breed;
  * }
  *
  *
  * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException;
  * writeReplace：序列化时替换对象，可返回代理/DTO 替代原对象写入流。支持 private/protected/包私有访问。
  *
  * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
  * readResolve：反序列化时替换对象，常用于单例模式防止反序列化破坏唯一性。调用规则同 writeReplace。
  *
  * 方法调用时机（序列化流程）：
  * ┌─────────────────────────────────────────────────────────────────┐
  * │                    ObjectOutputStream.writeObject()             │
  * │                                                                 │
  * │  1. 调用 writeReplace() ──→ 返回替代对象(或原对象自身)          │
  * │         │                                                       │
  * │         ▼                                                       │
  * │  2. 检查替代对象是否实现 Serializable                           │
  * │         │                                                       │
  * │         ▼                                                       │
  * │  3. 调用 writeObject(默认实现)                                  │
  * │         │                                                       │
  * │         ▼                                                       │
  * │  4. 递归处理对象图中的其他引用对象 ──→ 对每个重复步骤 1-4       │
  * │                                                                 │
  * └─────────────────────────────────────────────────────────────────┘
  *
  * 方法调用时机（反序列化流程）：
  * ┌─────────────────────────────────────────────────────────────────┐
  * │                  ObjectInputStream.readObject()                 │
  * │                                                                 │
  * │  1. 从流中读取字节 → 创建对象实例                               │
  * │         │                                                       │
  * │         ▼                                                       │
  * │  2. 调用 readObject(默认实现)                                   │
  * │         │                                                       │
  * │         ▼                                                       │
  * │  3. 调用 readResolve() ──→ 返回替代对象(或原对象自身)          │
  * │         │                                                       │
  * │         ▼                                                       │
  * │  4. 递归处理对象图中的其他引用对象 ──→ 对每个重复步骤 1-4       │
  * │                                                                 │
  * └─────────────────────────────────────────────────────────────────┘
  *
  * 注意：writeReplace 发生在实际序列化之前，readResolve 发生在实际反序列化之后。
  * 这两个方法允许在不改变外部序列化机制的情况下替换/校正对象。
  *
  * 例（writeReplace - 序列化时替换对象）：
  * class User implements Serializable {
  *     private String username;
  *     private transient String password;  // transient关键字修饰字段不序列化
  *
  *     public User(String username, String password) {
  *         this.username = username;
  *         this.password = password;
  *     }
  *
  *     // 序列化时用安全的代理对象替换原对象
  *     private Object writeReplace() throws ObjectStreamException {
  *         return new UserDTO(this.username);  // 只序列化必要信息
  *     }
  * }
  *
  * class UserDTO implements Serializable {
  *     private String username;
  *     public UserDTO(String username) { this.username = username; }
  * }
  *
  * 例（readResolve - 反序列化时替换对象，常用于单例模式）：
  * class Singleton implements Serializable {
  *     private static final Singleton INSTANCE = new Singleton();
  *     private String data;
  *
  *     private Singleton() { this.data = "default"; }
  *
  *     public static Singleton getInstance() { return INSTANCE; }
  *
  *     // 反序列化时返回已有的单例实例，避免创建新对象
  *     private Object readResolve() throws ObjectStreamException {
  *         return INSTANCE;  // 始终返回同一个实例
  *     }
  * }
  *
  * serialVersionUID：每个可序列化类关联的版本号，用于反序列化时校验发送方和接收方的类是否兼容。
  * 不匹配时抛出 InvalidClassException。
  *
  * ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;
  *
  * - 未显式声明时，运行时会根据类细节自动计算出seriaVersionUID，但强烈建议显式声明（推荐 private 修饰符），
  *   因为默认值对编译器实现敏感，可能导致意外的 InvalidClassException。
  * - 数组类始终使用默认计算值，无需显示申明声明seriaVersionUID。
  *
  */
public interface Serializable {
}
