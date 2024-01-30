package com.haishinkit.util

data class EdgeInsets(var top: Int, var left: Int, var bottom: Int, var right: Int) {
    fun set(
        top: Int,
        left: Int,
        bottom: Int,
        right: Int,
    ) {
        this.top = top
        this.left = left
        this.bottom = bottom
        this.right = right
    }
}