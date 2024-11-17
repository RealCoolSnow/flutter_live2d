package com.plugin.flutter_live2d

import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel

object LAppDefine {
    enum class Scale(val value: Float) {
        DEFAULT(1.0f),
        MAX(2.0f),
        MIN(0.8f)
    }

    enum class LogicalView(val value: Float) {
        LEFT(-1.0f),
        RIGHT(1.0f),
        BOTTOM(-1.0f),
        TOP(1.0f)
    }

    enum class MaxLogicalView(val value: Float) {
        LEFT(-2.0f),
        RIGHT(2.0f),
        BOTTOM(-2.0f),
        TOP(2.0f)
    }

    enum class ResourcePath(val path: String) {
        ROOT(""),
        SHADER_ROOT("Shaders"),
        BACK_IMAGE("back_class_normal.png"),
        GEAR_IMAGE("icon_gear.png"),
        POWER_IMAGE("close.png"),
        VERT_SHADER("VertSprite.vert"),
        FRAG_SHADER("FragSprite.frag")
    }

    enum class MotionGroup(val id: String) {
        IDLE("Idle"),
        TAP_BODY("TapBody")
    }

    enum class HitAreaName(val id: String) {
        HEAD("Head"),
        BODY("Body")
    }

    enum class Priority(val priority: Int) {
        NONE(0),
        IDLE(1),
        NORMAL(2),
        FORCE(3)
    }

    const val MOC_CONSISTENCY_VALIDATION_ENABLE = true
    const val DEBUG_LOG_ENABLE = true
    const val DEBUG_TOUCH_LOG_ENABLE = true
    val cubismLoggingLevel = LogLevel.VERBOSE
    const val PREMULTIPLIED_ALPHA_ENABLE = true
    const val USE_RENDER_TARGET = false
    const val USE_MODEL_RENDER_TARGET = false
} 