package com.example.server.handler;

import com.example.core.annotation.RpcService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;

public class CtClassFactory {

    private final static String SERVICE_CLASS_PATH = "classpath*:com/example/server/service/impl/**/*.class";

    private static ConcurrentHashMap<Class, Object> CLASS_POOL = new ConcurrentHashMap<>();


    static {
        // 服务注册
        register(SERVICE_CLASS_PATH);
    }

    private static void register(String path){
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory metaReader = new CachingMetadataReaderFactory();
            Resource[] resources = resolver.getResources(path);
            MetadataReader reader;
            for (Resource resource : resources) {
                reader = metaReader.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                System.out.println(className);

                try {
                    Class clazz = Class.forName(className);

                    // 处理实现类
                    for (Annotation x : clazz.getAnnotations()) {
                        if (x instanceof RpcService) {
                            setInstance(clazz, clazz.newInstance());
                            break;
                        }
                    }

                    // 处理接口
                    for(Class interClazz : clazz.getInterfaces()){
                        for (Annotation x : interClazz.getAnnotations()) {
                            if (x instanceof RpcService) {
                                setInstance(interClazz, clazz.newInstance());
                                break;
                            }
                        }
                    }

                    // 处理父类
                    for (Annotation x : clazz.getSuperclass().getAnnotations()) {
                        if (x instanceof RpcService) {
                            setInstance(clazz, clazz.newInstance());
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setInstance(Class clazz, Object instance) throws IllegalAccessException {
        if(clazz.isInterface() && CLASS_POOL.contains(clazz)){
            throw new IllegalAccessException("duplicate class [" + clazz.toString() + "] register..");
        }
        CLASS_POOL.put(clazz, instance);
    }

    public static <T> T getInstance(Class<T> t){
        return (T) CLASS_POOL.get(t);
    }

}
