package top.xfunny.mod.client;

import org.mtr.core.servlet.MessageQueue;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mapping.holder.*;
import org.mtr.mod.render.MainRenderer;
import top.xfunny.mod.Init;
import top.xfunny.mod.client.client_data.DynamicResource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DynamicTextureCache {// 使用AI优化
    private static final int COOLDOWN_TIME = 300_000; // 5min，避免楼层号纹理频繁淘汰重建
    private static final int MAX_TEXTURES = 2000; // 动态纹理总数上限，超出时按插入序淘汰最旧条目
    private static final Identifier DEFAULT_TRANSPARENT_RESOURCE = new Identifier(Init.MOD_ID, "textures/block/transparent.png");
    public static DynamicTextureCache instance = new DynamicTextureCache();
    private final Object2ObjectLinkedOpenHashMap<String, DynamicResource> dynamicResources = new Object2ObjectLinkedOpenHashMap<>();

    // 强引用兜底
    private final Map<String, DynamicResource> lastSuccessfulResource = new ConcurrentHashMap<>();

    // 待删除队列：Key=资源对象本身 (确保对象存活), Value=物理销毁时间戳
    private final Map<DynamicResource, Long> resourcesToDispose = new ConcurrentHashMap<>();

    private final Set<String> generatingScreens = ConcurrentHashMap.newKeySet();
    private final MessageQueue<Runnable> resourceRegistryQueue = new MessageQueue<>();

    // 快查：当前有哪些资源被 lastSuccessfulResource 引用，替代 O(n) 的 containsValue
    private final Set<DynamicResource> aliveResources = ConcurrentHashMap.newKeySet();
    private long lastCleanupTime;

    public DynamicResource getResource(String textureId, Long blockPos, Supplier<NativeImage> supplier) {
        resourceRegistryQueue.process(Runnable::run);

        // 优化：screenUniqueId 计算
        int separatorIndex = textureId.indexOf('$');
        String screenUniqueId;
        if (separatorIndex != -1) {
            screenUniqueId = textureId.substring(0, separatorIndex) + "_" + blockPos;
        } else {
            screenUniqueId = textureId + "_" + blockPos;
        }

        // 1. 缓存命中
        DynamicResource currentRes = dynamicResources.get(textureId);
        if (currentRes != null && !currentRes.needsRefresh) {
            currentRes.expiryTime = System.currentTimeMillis() + COOLDOWN_TIME;
            lastSuccessfulResource.put(screenUniqueId, currentRes);
            aliveResources.add(currentRes);
            return currentRes;
        }

        // 2. 触发生成 (以 textureId 去重，同一文字内容只生成一次)
        if (!generatingScreens.contains(textureId) && generatingScreens.size() <= 1000) {
            registerResource(supplier, textureId, screenUniqueId);
        }

        // 3. 兜底逻辑 (修复透明问题的关键)
        // 只要这里取到的对象还没被 dispose()，它就是完全可用的
        DynamicResource fallback = lastSuccessfulResource.get(screenUniqueId);
        if (fallback != null) {
            fallback.expiryTime = System.currentTimeMillis() + COOLDOWN_TIME; // 续命
            aliveResources.add(fallback);
            return fallback;
        }

        // 4. 如果当前有资源但被标记刷新，强行暂用
        if (currentRes != null) {
            return currentRes;
        }

        // 5. 只有第一次加载才会运行到这里
        return DefaultRenderingColor.TRANSPARENT.dynamicResource;
    }

    private void registerResource(Supplier<NativeImage> supplier, String textureId, String screenUniqueId) {
        generatingScreens.add(textureId);

        MainRenderer.WORKER_THREAD.scheduleDynamicTextures(() -> {
            final NativeImage nativeImage = supplier.get();
            // 此时仍在异步线程

            resourceRegistryQueue.put(() -> {
                // 回到主线程
                try {
                    if (nativeImage != null) {
                        final NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
                        final Identifier identifier = new Identifier(Init.MOD_ID, "id_" + org.mtr.mod.Init.randomString());
                        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new AbstractTexture(nativeImageBackedTexture.data));

                        final DynamicResource dynamicResourceNew = new DynamicResource(identifier, nativeImageBackedTexture);

                        // 替换 Map 中的资源
                        DynamicResource oldRes = dynamicResources.put(textureId, dynamicResourceNew);

                        // 【修改】旧资源不要立即 dispose，而是放入死亡队列，给予 10s 缓冲期
                        // 这防止了如果 oldRes 恰好是当前的兜底资源，被瞬间销毁导致闪烁
                        if (oldRes != null && oldRes != dynamicResourceNew) {
                            resourcesToDispose.put(oldRes, System.currentTimeMillis() + COOLDOWN_TIME);
                        }

                        // 更新兜底
                        lastSuccessfulResource.put(screenUniqueId, dynamicResourceNew);
                        aliveResources.add(dynamicResourceNew);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    generatingScreens.remove(textureId);
                }
            });
        });
    }

    public void reload() {
        dynamicResources.values().forEach(dynamicResource -> dynamicResource.needsRefresh = true);
        generatingScreens.clear();
        // reload 不清空 lastSuccessfulResource，防止瞬间全屏透明
    }

    public void tick() {
        long now = System.currentTimeMillis();

        // 1. 清理过期资源（不再检查 lastSuccessfulResource，改用 aliveResources O(1) 判断）
        final ObjectArrayList<String> keysToRemove = new ObjectArrayList<>();
        dynamicResources.forEach((key, res) -> {
            if (res.expiryTime < now && !aliveResources.contains(res)) {
                keysToRemove.add(key);
                resourcesToDispose.put(res, now + COOLDOWN_TIME);
            }
        });
        keysToRemove.forEach(dynamicResources::remove);

        // 2. 物理销毁到期资源
        final ObjectArrayList<DynamicResource> toActuallyDispose = new ObjectArrayList<>();
        resourcesToDispose.forEach((res, disposeTime) -> {
            if (disposeTime < now && !aliveResources.contains(res)) {
                toActuallyDispose.add(res);
            }
        });
        toActuallyDispose.forEach(res -> {
            res.dispose();
            resourcesToDispose.remove(res);
            aliveResources.remove(res);
        });

        // 3. 大小上限淘汰
        while (dynamicResources.size() > MAX_TEXTURES) {
            final String oldestKey = dynamicResources.firstKey();
            final DynamicResource oldestRes = dynamicResources.remove(oldestKey);
            if (oldestRes != null && !aliveResources.contains(oldestRes)) {
                resourcesToDispose.put(oldestRes, now + COOLDOWN_TIME);
            } else {
                break;
            }
        }

        // 4. 定期清理 lastSuccessfulResource 中的过期条目（每 30 秒一次）
        if (now - lastCleanupTime > 30_000) {
            lastSuccessfulResource.entrySet().removeIf(entry -> {
                final DynamicResource res = entry.getValue();
                if (res.expiryTime + COOLDOWN_TIME * 2 < now) {
                    aliveResources.remove(res);
                    return true;
                }
                return false;
            });
            lastCleanupTime = now;
        }
    }

    private enum DefaultRenderingColor {
        TRANSPARENT(DEFAULT_TRANSPARENT_RESOURCE);
        private final DynamicResource dynamicResource;

        DefaultRenderingColor(Identifier identifier) {
            dynamicResource = new DynamicResource(identifier, null);
        }
    }
}