package com.aionemu.commons.configuration;

import com.aionemu.commons.configuration.transformers.BooleanTransformer;
import com.aionemu.commons.configuration.transformers.ByteTransformer;
import com.aionemu.commons.configuration.transformers.CharTransformer;
import com.aionemu.commons.configuration.transformers.ClassTransformer;
import com.aionemu.commons.configuration.transformers.DoubleTransformer;
import com.aionemu.commons.configuration.transformers.EnumTransformer;
import com.aionemu.commons.configuration.transformers.FileTransformer;
import com.aionemu.commons.configuration.transformers.FloatTransformer;
import com.aionemu.commons.configuration.transformers.InetSocketAddressTransformer;
import com.aionemu.commons.configuration.transformers.IntegerTransformer;
import com.aionemu.commons.configuration.transformers.LongTransformer;
import com.aionemu.commons.configuration.transformers.PatternTransformer;
import com.aionemu.commons.configuration.transformers.ShortTransformer;
import com.aionemu.commons.configuration.transformers.StringTransformer;
import com.aionemu.commons.utils.ClassUtils;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

/**
 * 属性转换器工厂类，用于创建不同类型的属性转换器实例
 * Property transformer factory class for creating different types of property transformer instances
 * <p>
 * 该类负责根据目标类型创建对应的属性转换器，支持基本数据类型、String、Enum、File、InetSocketAddress等类型的转换
 * This class is responsible for creating corresponding property transformers based on target types,
 * supporting conversion of primitive types, String, Enum, File, InetSocketAddress and other types
 */
public class PropertyTransformerFactory {

    /**
     * 创建新的属性转换器实例
     * Create a new property transformer instance
     *
     * @param clazzToTransform 需要转换的目标类型 / Target class type to transform
     * @param tc 指定的转换器类型，如果为null则根据目标类型自动选择 / Specified transformer class, if null will auto select based on target type
     * @return 对应的属性转换器实例 / Corresponding property transformer instance
     * @throws TransformationException 当无法创建转换器时抛出 / Thrown when transformer cannot be created
     */
    public static PropertyTransformer newTransformer(Class clazzToTransform, Class<? extends PropertyTransformer> tc) throws TransformationException {
        if (tc == PropertyTransformer.class) {
            tc = null;
        }

        if (tc != null) {
            try {
                return (PropertyTransformer)tc.newInstance();
            } catch (Exception var3) {
                throw new TransformationException("Can't instantiate property transformer", var3);
            }
        } else if (clazzToTransform != Boolean.class && clazzToTransform != Boolean.TYPE) {
            if (clazzToTransform != Byte.class && clazzToTransform != Byte.TYPE) {
                if (clazzToTransform != Character.class && clazzToTransform != Character.TYPE) {
                    if (clazzToTransform != Double.class && clazzToTransform != Double.TYPE) {
                        if (clazzToTransform != Float.class && clazzToTransform != Float.TYPE) {
                            if (clazzToTransform != Integer.class && clazzToTransform != Integer.TYPE) {
                                if (clazzToTransform != Long.class && clazzToTransform != Long.TYPE) {
                                    if (clazzToTransform != Short.class && clazzToTransform != Short.TYPE) {
                                        if (clazzToTransform == String.class) {
                                            return StringTransformer.SHARED_INSTANCE;
                                        } else if (clazzToTransform.isEnum()) {
                                            return EnumTransformer.SHARED_INSTANCE;
                                        } else if (clazzToTransform == File.class) {
                                            return FileTransformer.SHARED_INSTANCE;
                                        } else if (ClassUtils.isSubclass(clazzToTransform, InetSocketAddress.class)) {
                                            return InetSocketAddressTransformer.SHARED_INSTANCE;
                                        } else if (clazzToTransform == Pattern.class) {
                                            return PatternTransformer.SHARED_INSTANCE;
                                        } else if (clazzToTransform == Class.class) {
                                            return ClassTransformer.SHARED_INSTANCE;
                                        } else {
                                            throw new TransformationException("Transformer not found for class " + clazzToTransform.getName());
                                        }
                                    } else {
                                        return ShortTransformer.SHARED_INSTANCE;
                                    }
                                } else {
                                    return LongTransformer.SHARED_INSTANCE;
                                }
                            } else {
                                return IntegerTransformer.SHARED_INSTANCE;
                            }
                        } else {
                            return FloatTransformer.SHARED_INSTANCE;
                        }
                    } else {
                        return DoubleTransformer.SHARED_INSTANCE;
                    }
                } else {
                    return CharTransformer.SHARED_INSTANCE;
                }
            } else {
                return ByteTransformer.SHARED_INSTANCE;
            }
        } else {
            return BooleanTransformer.SHARED_INSTANCE;
        }
    }
}
