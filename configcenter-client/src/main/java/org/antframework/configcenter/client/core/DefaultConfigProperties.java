/*
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2017-09-11 13:56 创建
 */
package org.antframework.configcenter.client.core;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置属性默认实现
 */
public class DefaultConfigProperties implements ConfigurableConfigProperties {
    // 属性value为null时的占位符
    private static final String NULL_VALUE = DefaultConfigProperties.class.getName() + "#NULL_VALUE";

    // 属性
    private Map<String, String> properties = new ConcurrentHashMap<>();

    @Override
    public String getProperty(String key) {
        return toOriginal(properties.get(key));
    }

    @Override
    public String[] getPropertyKeys() {
        Set<String> keys = properties.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    @Override
    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    @Override
    public synchronized List<ModifiedProperty> replaceProperties(Map<String, String> newProperties) {
        List<ModifiedProperty> modifiedProperties = analyseModify(properties, newProperties);
        applyModify(properties, modifiedProperties);
        return modifiedProperties;
    }

    // 转换为原始value
    private static String toOriginal(String savable) {
        return StringUtils.equals(savable, NULL_VALUE) ? null : savable;
    }

    // 转换为可保存value
    private static String toSavable(String original) {
        return original == null ? NULL_VALUE : original;
    }

    // 分析被修改的属性
    private static List<ModifiedProperty> analyseModify(Map<String, String> oldProperties, Map<String, String> newProperties) {
        List<ModifiedProperty> modifiedProperties = new ArrayList<>();
        // 分析删除和修改的属性
        for (String key : oldProperties.keySet()) {
            if (!newProperties.containsKey(key)) {
                modifiedProperties.add(new ModifiedProperty(ModifiedProperty.ModifyType.REMOVE, key, toOriginal(oldProperties.get(key)), null));
            } else if (!StringUtils.equals(newProperties.get(key), toOriginal(oldProperties.get(key)))) {
                modifiedProperties.add(new ModifiedProperty(ModifiedProperty.ModifyType.UPDATE, key, toOriginal(oldProperties.get(key)), newProperties.get(key)));
            }
        }
        // 分析新增的属性
        for (String key : newProperties.keySet()) {
            if (!oldProperties.containsKey(key)) {
                modifiedProperties.add(new ModifiedProperty(ModifiedProperty.ModifyType.ADD, key, null, newProperties.get(key)));
            }
        }

        return modifiedProperties;
    }

    // 应用被修改的属性
    private static void applyModify(Map<String, String> properties, List<ModifiedProperty> modifiedProperties) {
        for (ModifiedProperty modifiedProperty : modifiedProperties) {
            switch (modifiedProperty.getType()) {
                case ADD:
                case UPDATE:
                    properties.put(modifiedProperty.getKey(), toSavable(modifiedProperty.getNewValue()));
                    break;
                case REMOVE:
                    properties.remove(modifiedProperty.getKey());
                    break;
                default:
                    throw new IllegalArgumentException("无法识别的修改类型");
            }
        }
    }
}
