package com.example.a3dcompare.web

import android.content.Context
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.io.File
import java.io.FileInputStream

/**
 * WebView 资源映射配置。
 *
 * 虚拟域名：https://appassets.androidplatform.net
 *
 * 映射规则：
 *  1. /assets/web3d/web3D/models/...  -> 应用私有目录 filesDir/web3d-models/...（GLB 模型）
 *  2. /assets/...                     -> APK assets 目录（HTML / JS / CSS / 参考图 / libs）
 *
 * 原 HTML 中的相对路径 "models/group-01/xxx.glb" 解析后正好落在
 *   /assets/web3d/web3D/models/group-01/xxx.glb
 * 因此原 HTML 的 models 相对路径不需要任何修改。
 *
 * 注意：addPathHandler 按注册顺序匹配前缀，更具体的 models 前缀必须先注册。
 */
object WebAssetConfig {

    const val DOMAIN = "appassets.androidplatform.net"

    /** 入口页面（大小写与 assets 实际目录一致：外层 web3d 小写，内层 web3D 大写） */
    const val ENTRY_URL =
        "https://$DOMAIN/assets/web3d/web3D/3d-model-comparison.html"

    /** 模型 URL 前缀（已去掉域名），与 HTML 相对路径 models/ 对应 */
    private const val MODELS_PREFIX = "/assets/web3d/web3D/models/"

    /** 私有目录中模型根目录名 */
    private const val MODELS_DIR_NAME = "web3d-models"

    /** 私有模型根目录，不存在则创建 */
    fun modelsRoot(context: Context): File =
        File(context.filesDir, MODELS_DIR_NAME).apply { mkdirs() }

    /** 构建 WebViewAssetLoader（模型 handler 必须先于 assets handler 注册） */
    fun create(context: Context): WebViewAssetLoader {
        val appContext = context.applicationContext
        return WebViewAssetLoader.Builder()
            .setDomain(DOMAIN)
            .addPathHandler(MODELS_PREFIX, ModelsPathHandler(modelsRoot(appContext)))
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(appContext))
            .build()
    }
}

/**
 * 处理 models/ 下的 GLB 请求。
 *
 * handle() 收到的 path 已经：
 *  - 去掉了注册的前缀（MODELS_PREFIX），例如 "group-01/pixal3d.glb"
 *  - 做过 URL 解码，中文、空格、'+' 已还原为原始字符
 *
 * 因此直接拼接到私有目录即可。这里仍做路径穿越校验。
 */
class ModelsPathHandler(private val root: File) : WebViewAssetLoader.PathHandler {

    private val rootCanonical: String by lazy { root.canonicalPath + File.separator }

    override fun handle(path: String): WebResourceResponse? {
        val target = File(root, path)
        val targetCanonical = try {
            target.canonicalPath
        } catch (_: java.io.IOException) {
            return forbidden()
        }

        // 越界访问（例如 ../../）直接拒绝
        if (!targetCanonical.startsWith(rootCanonical)) {
            return forbidden()
        }
        // 私有目录没有该文件时返回 null，让 WebViewAssetLoader 继续尝试下一个处理器（如 assets 中的 reference.png）
        if (!target.isFile) {
            return null
        }

        return WebResourceResponse(
            mimeFor(target.name),
            null,
            200,
            "OK",
            mapOf("Cache-Control" to "no-cache"),
            FileInputStream(target)
        )
    }

    private fun forbidden() =
        WebResourceResponse("text/plain", "utf-8", 403, "Forbidden", emptyMap(), null)

    private fun mimeFor(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "glb" -> "model/gltf-binary"
            "gltf" -> "model/gltf+json"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                ?: "application/octet-stream"
        }
    }
}
