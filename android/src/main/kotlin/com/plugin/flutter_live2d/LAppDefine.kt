package com.plugin.flutter_live2d

import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel

/**
 * 应用程序中使用的常量定义
 */
object LAppDefine {
    /** MOC3一致性验证开关 */
    const val MOC_CONSISTENCY_VALIDATION_ENABLE = true

    /** 调试日志开关 */
    const val DEBUG_LOG_ENABLE = true

    /** 触摸信息调试日志开关 */
    const val DEBUG_TOUCH_LOG_ENABLE = true

    /** Framework日志输出级别设置 */
    val cubismLoggingLevel = LogLevel.VERBOSE

    /** 预乘alpha开关 */
    const val PREMULTIPLIED_ALPHA_ENABLE = true

    /** 
     * 是否绘制到LAppView持有的目标
     * (如果USE_RENDER_TARGET和USE_MODEL_RENDER_TARGET都为true，此变量优先于USE_MODEL_RENDER_TARGET)
     */
    const val USE_RENDER_TARGET = false

    /** 是否绘制到每个LAppModel拥有的目标 */
    const val USE_MODEL_RENDER_TARGET = false

    /**
     * 缩放比例定义
     */
    enum class Scale(val value: Float) {
        /** 默认缩放比例 */
        DEFAULT(1.0f),
        /** 最大缩放比例 */
        MAX(2.0f),
        /** 最小缩放比例 */
        MIN(0.8f)
    }

    /**
     * 逻辑视图坐标系统
     */
    enum class LogicalView(val value: Float) {
        /** 左端 */
        LEFT(-1.0f),
        /** 右端 */
        RIGHT(1.0f),
        /** 底端 */
        BOTTOM(-1.0f),
        /** 顶端 */
        TOP(1.0f)
    }

    /**
     * 最大逻辑视图坐标系统
     */
    enum class MaxLogicalView(val value: Float) {
        /** 最大左端 */
        LEFT(-2.0f),
        /** 最大右端 */
        RIGHT(2.0f),
        /** 最大底端 */
        BOTTOM(-2.0f),
        /** 最大顶端 */
        TOP(2.0f)
    }

    /**
     * 资源路径定义
     */
    enum class ResourcePath(val path: String) {
        /** 资源根目录相对路径 */
        ROOT(""),
        /** 着色器目录相对路径 */
        SHADER_ROOT("Shaders"),
        /** 背景图片文件 */
        // BACK_IMAGE("flutter_assets/assets/live2d/back_class_normal.png"),
        /** 齿轮图片文件 */
        // GEAR_IMAGE("flutter_assets/assets/live2d/icon_gear.png"),
        /** 电源按钮图片文件 */
        // POWER_IMAGE("flutter_assets/assets/live2d/close.png"),
        /** 顶点着色器文件 */
        VERT_SHADER("VertSprite.vert"),
        /** 片段着色器文件 */
        FRAG_SHADER("FragSprite.frag")
    }

    /**
     * 动作组定义
     */
    enum class MotionGroup(val id: String) {
        /** 待机动作ID */
        IDLE("Idle"),
        /** 点击身体动作ID */
        TAP_BODY("TapBody")
    }

    /**
     * 点击区域名称定义
     * (与外部定义文件(json)匹配)
     */
    enum class HitAreaName(val id: String) {
        HEAD("Head"),
        BODY("Body")
    }

    /**
     * 动作优先级定义
     */
    enum class Priority(val priority: Int) {
        NONE(0),
        IDLE(1),
        NORMAL(2),
        FORCE(3)
    }

    /**
     * 模型相关路径定义
     */
    object ModelPath {
        const val ROOT = "flutter_assets/assets/live2d/"
        const val HARU = "${ROOT}Haru/"
        const val HIYORI = "${ROOT}Hiyori/"
        const val MARK = "${ROOT}Mark/"
        const val NATORI = "${ROOT}Natori/"
        const val RICE = "${ROOT}Rice/"
    }

    /**
     * 模型文件相关定义
     */
    object ModelFile {
        const val MOTION3_PREFIX = "_motion3.json"
        const val EXPRESSION_PREFIX = "_exp3.json"
        const val MODEL_SUFFIX = ".model3.json"
        const val PHYSICS_SUFFIX = ".physics3.json"
        const val USER_DATA_SUFFIX = ".userdata3.json"
        const val MOC3_SUFFIX = ".moc3"
    }

    /**
     * 着色器相关定义
     */
    object ShaderNames {
        const val VERT_SHADER = "VertShader.vert"
        const val FRAG_SHADER = "FragShader.frag"
        const val SPRITE_VERT_SHADER = "SpriteVertShader.vert"
        const val SPRITE_FRAG_SHADER = "SpriteFragShader.frag"
    }

    /**
     * 错误消息定义
     */
    object ErrorMessages {
        const val NO_MODEL_LOADED = "No model is loaded"
        const val FAILED_TO_LOAD_MODEL = "Failed to load model"
        const val FAILED_TO_START_MOTION = "Failed to start motion"
        const val FAILED_TO_SET_EXPRESSION = "Failed to set expression"
    }

    /**
     * 路径处理工具
     */
    object PathUtils {
        private const val FLUTTER_ASSETS_PREFIX = "flutter_assets/"

        /**
         * 确保路径包含 flutter_assets 前缀
         */
        fun ensureFlutterAssetsPath(path: String): String {
            return if (!path.startsWith(FLUTTER_ASSETS_PREFIX)) {
                FLUTTER_ASSETS_PREFIX + path
            } else {
                path
            }
        }

        /**
         * 获取完整的资源路径
         */
        fun getFullResourcePath(basePath: String, fileName: String): String {
            return ensureFlutterAssetsPath("$basePath$fileName")
        }

        /**
         * 获取完整的模型路径
         */
        fun getFullModelPath(modelName: String): String {
            return ensureFlutterAssetsPath("${ModelPath.ROOT}$modelName/")
        }
    }
} 